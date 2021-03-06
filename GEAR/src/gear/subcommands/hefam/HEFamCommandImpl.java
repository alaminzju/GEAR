package gear.subcommands.hefam;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math.stat.regression.SimpleRegression;

import gear.data.InputDataSet2;
import gear.subcommands.CommandArguments;
import gear.subcommands.CommandImpl;
import gear.util.BufferedReader;
import gear.util.FileUtil;
import gear.util.Logger;

public class HEFamCommandImpl extends CommandImpl
{

	@Override
	public void execute(CommandArguments cmdArgs)
	{
		heArgs = (HEFamCommandArguments) cmdArgs;

		data = new InputDataSet2();

		if (heArgs.isIBDList())
		{
			data.addFile(heArgs.getIBDList()[0]+".ibd.id");
			data.addFile(heArgs.getPhenotypeFile(), heArgs.getPhenotypeIdx());

			if (heArgs.getCovFile() != null)
			{
				data.addFile(heArgs.getCovFile(), heArgs.getCovNumber());
			}
			if (heArgs.getKeepFile() != null)
			{
				data.addFile(heArgs.getKeepFile());
			}
			data.LineUpFiles();

			Y = readPhenotype();
			A3 = readIBDList(data.getFileSampleSize(ibdFileIdx));
			Logger.printUserLog(A3.length + " variance components included.");
			Logger.printUserLog("");

			if (heArgs.getCovFile() != null)
			{
				X = readCovar();
				adjustY();
			}

			if (heArgs.isScale())
			{
				Y = StatUtils.normalize(Y);
			}
			HEMultple();
		}
		else
		{
			data.addFile(heArgs.getIBDID());
			data.addFile(heArgs.getPhenotypeFile(), heArgs.getPhenotypeIdx());

			if (heArgs.getCovFile() != null)
			{
				data.addFile(heArgs.getCovFile(), heArgs.getCovNumber());
			}
			if (heArgs.getKeepFile() != null)
			{
				data.addFile(heArgs.getKeepFile());
			}
			data.LineUpFiles();

			Y = readPhenotype();

			readIBD(data.getFileSampleSize(ibdFileIdx));
			Logger.printUserLog("1 variance component included.");
			Logger.printUserLog("");

			if (heArgs.getCovFile() != null)
			{
				X = readCovar();
				adjustY();
			}

			if (heArgs.isScale())
			{
				Y = StatUtils.normalize(Y);
			}
			HESingle();

			if (heArgs.isJackknife())
			{
				HESingleJackknife();
			}
		}

		SummaryA3();
		printout();
	}

	private void printout()
	{
		String Fout = heArgs.getOutRoot()  + ".he";
		PrintStream HE = FileUtil.CreatePrintStream(Fout);
		HE.println();
		HE.println("GRM\tEffective samples\tEffective markers");
		HE.println("-------------------------------------------------------------------");

		Logger.printUserLog("");
		Logger.printUserLog("GRM\tEffective samples\tEffective markers");
		Logger.printUserLog("-------------------------------------------------------------------");

		double mA_= 0;
		double vA_= 0;
		for (int i = 0; i < mA.length; i++)
		{
			HE.println("GRM" + (i+1) + "\t" + fmt.format(-1/mA[i]) + "\t" + fmt.format(1/vA[i][i]));
			Logger.printUserLog("GRM" + (i+1) + "\t" + fmt.format(-1/mA[i]) + "\t" + fmt.format(1/vA[i][i]));

			mA_ += -1/mA[i];
			vA_ = 1/vA[i][i];
		}

		if (mA.length > 1)
		{
			HE.println("Ave.\t" + fmt.format(mA_) + "\t" + fmt.format(vA_));
			Logger.printUserLog("Ave.\t" + fmt.format(mA_) + "\t" + fmt.format(vA_));
		}

		HE.println("");
		HE.println("Covariance structure for GRM");
		HE.println("-------------------------------------------------------------------");
		
		Logger.printUserLog("");
		Logger.printUserLog("Covariance structure for GRM");
		Logger.printUserLog("-------------------------------------------------------------------");

		for (int i = 0; i < vA.length; i++)
		{
			String s = new String();
			for (int j = 0; j <= i; j++)
			{
				s += fmtE.format(vA[i][j]) + "\t";
			}
			HE.println(s);
			Logger.printUserLog(s);
		}

		HE.println("-------------------------------------------------------------------");
		HE.println("");
		HE.println("-------------------------------------------------------------------");
		HE.println("HE regression");
		HE.println("Para\tEstimate\tSE\tPROP");
		HE.println("-------------------------------------------------------------------");

		Logger.printUserLog("-------------------------------------------------------------------");
		Logger.printUserLog("");
		Logger.printUserLog("-------------------------------------------------------------------");
		Logger.printUserLog("HE regression");
		Logger.printUserLog("Para\tEstimate\tSE\tPROP");
		Logger.printUserLog("-------------------------------------------------------------------");

		for (int i = 0; i < beta.length; i++)
		{
			if (i == 0)
			{
				HE.println("Mean\t" + fmt.format(beta[i]) + "\t" + fmt.format( Math.sqrt(beta_v[i][i])));
				Logger.printUserLog("Mean\t" + fmt.format(beta[i]) + "\t" + fmt.format( Math.sqrt(beta_v[i][i])));
			}
			else
			{
				HE.println("Beta" + i +"\t" + fmt.format(beta[i]) + "\t" + fmt.format( Math.sqrt(beta_v[i][i])) + "\t" + fmt.format(-1*beta[i]/beta[0]));
				Logger.printUserLog("Beta" + i +"\t" + fmt.format(beta[i]) + "\t" + fmt.format( Math.sqrt(beta_v[i][i])) + "\t" + fmt.format(-1*beta[i]/beta[0]));
			}
		}
		HE.println("-------------------------------------------------------------------");
		Logger.printUserLog("-------------------------------------------------------------------");

		HE.println("Covariance structure for HE");
		Logger.printUserLog("Covariance structure for HE");

		for (int i = 0; i < beta_v.length; i++)
		{
			String s = new String();
			for (int j = 0; j <= i; j++)
			{
				s += fmtE.format(beta_v[i][j]) + "\t";
			}
			HE.println(s);
			Logger.printUserLog(s);
		}
		HE.close();
	}
	
	private void HEMultple()
	{
		OLSMultipleLinearRegression mod = new OLSMultipleLinearRegression();

		double[] yy = new double[Y.length * (Y.length-1)/2];
		double[][] xx = new double[Y.length * (Y.length -1)/2][A3.length];
		int cnt = 0;
		for (int i = 0; i < Y.length; i++)
		{
			for (int j = 0; j < i; j++)
			{
				if (heArgs.isSD())
				{
					yy[cnt] = (Y[i] - Y[j]) * (Y[i] - Y[j]);
				}
				else if (heArgs.isCP())
				{
					yy[cnt] = Y[i]*Y[j];
				}
				else
				{
					yy[cnt] = (Y[i] + Y[j]) * (Y[i] + Y[j]);
				}
				if (A3[0][i][j] == ibd_default)
				{
					continue;
				}
				for (int k = 0; k < A3.length; k++)
				{
					xx[cnt][k] = A3[k][i][j];				
				}
				cnt++;
			}
		}

		Logger.printUserLog(cnt + " observations for HE regression.");

		double[] y = new double[cnt];
		double[][] x = new double[cnt][A3.length];
		System.arraycopy(yy, 0, y, 0, cnt);
		for (int i = 0; i < cnt; i++)
		{
			System.arraycopy(xx[i], 0, x[i], 0, A3.length);
		}
		mod.newSampleData(y, x);
		beta = mod.estimateRegressionParameters();
		beta_v = mod.estimateRegressionParametersVariance();
		for(int i = 0; i < beta_v.length; i++)
		{
			for (int j = 0; j < beta_v[i].length; j++)
			{
				beta_v[i][j] *= mod.estimateErrorVariance();
			}
		}

	}

	private void HESingle()
	{
//		SimpleRegression sReg = new SimpleRegression();
		OLSMultipleLinearRegression mod = new OLSMultipleLinearRegression();

		double[] yy = new double[Y.length * (Y.length-1)/2];
		double[][] xx = new double[Y.length * (Y.length -1)/2][A3.length];
		int cnt = 0;

		for (int i = 0; i < Y.length; i++)
		{
			for (int j = 0; j < i; j++)
			{
				if (heArgs.isSD())
				{
					yy[cnt] = (Y[i] - Y[j]) * (Y[i] - Y[j]);
				}
				else if (heArgs.isCP())
				{
					yy[cnt] = Y[i]*Y[j];
				}
				else
				{
					yy[cnt] = (Y[i] + Y[j]) * (Y[i] + Y[j]);
				}
				if ((heArgs.isIBDcut() && A3[0][i][j] > heArgs.getIBDcutoff()) || (A3[0][i][j] == ibd_default) ) continue;
				if (A3[0][i][j] == ibd_default)
				{
					continue;
				}
				for (int k = 0; k < A3.length; k++)
				{
					xx[cnt][k] = A3[k][i][j];				
				}
				cnt++;
			}
		}

		Logger.printUserLog(cnt + " observations for HE regression.");

		double[] y = new double[cnt];
		double[][] x = new double[cnt][A3.length];
		System.arraycopy(yy, 0, y, 0, cnt);
		for (int i = 0; i < cnt; i++)
		{
			System.arraycopy(xx[i], 0, x[i], 0, A3.length);
		}
		mod.newSampleData(y, x);
		beta = mod.estimateRegressionParameters();
		beta_v = mod.estimateRegressionParametersVariance();
		for(int i = 0; i < beta_v.length; i++)
		{
			for (int j = 0; j < beta_v[i].length; j++)
			{
				beta_v[i][j] *= mod.estimateErrorVariance();
			}
		}
	}

	private void HESingleJackknife()
	{
		double[][] jBeta_ = new double[Y.length][2];
		double[] jBeta = new double[2];
		double[] jBetaSe = new double[2];
		for (int jk = 0; jk < Y.length; jk++)
		{
			SimpleRegression sReg = new SimpleRegression();

			for (int i = 0; i < Y.length; i++)
			{
				if (i == jk)
				{
					continue;
				}
				for (int j = 0; j < i; j++)
				{
					double yy = 0;
					if (j == jk)
					{
						continue;
					}
					if (heArgs.isSD())
					{
						yy = (Y[i] - Y[j]) * (Y[i] - Y[j]);
					}
					else if (heArgs.isCP())
					{
						yy = Y[i]*Y[j];
					}
					else
					{
						yy = (Y[i] + Y[j]) * (Y[i] + Y[j]);
					}
					if ( (heArgs.isIBDcut() && A3[0][i][j] > heArgs.getIBDcutoff()) || (A3[0][i][j] == ibd_default)) continue;
					sReg.addData(A3[0][i][j], yy);
				}
			}
//			Logger.printUserLog(cnt + " observations for HE regression -> Jackknife " + (jk+1) + " " + sReg.getSlope());
			jBeta_[jk][0] = Y.length * beta[0] - (Y.length-1) * sReg.getIntercept();
			jBeta_[jk][1] = Y.length * beta[1] - (Y.length-1) * sReg.getSlope();
		}

		double s1 = 0, s2 = 0;
		for (int jk = 0; jk < jBeta_.length; jk++)
		{
			s1 += jBeta_[jk][0];
			s2 += jBeta_[jk][1];
		}
		jBeta[0] = s1/jBeta_.length;
		jBeta[1] = s2/jBeta_.length;
		double ss1 = 0, ss2 = 0;
		for (int jk = 0; jk < jBeta_.length; jk++)
		{
			ss1 += (jBeta_[jk][0] - jBeta[0]) * (jBeta_[jk][0] - jBeta[0]);
			ss2 += (jBeta_[jk][1] - jBeta[1]) * (jBeta_[jk][1] - jBeta[1]);
		}
		jBetaSe[0] = Math.sqrt(ss1/(jBeta_.length * (jBeta_.length - 1)));
		jBetaSe[1] = Math.sqrt(ss2/(jBeta_.length * (jBeta_.length - 1)));

		Logger.printUserLog("-------------------------------------------------------------------");
		Logger.printUserLog("Jackknife results: ");
		Logger.printUserLog("JK_Mean\t"+jBeta[0] + "\t" + jBetaSe[0]);
		Logger.printUserLog("JK_Beta1\t"+jBeta[1] + "\t" + jBetaSe[1]);
		Logger.printUserLog("-------------------------------------------------------------------");

	}

	private double[][] readCovar()
	{
		int[] covIdx = data.getMatchedSubjectIdx(covFileIdx);
		double[][] x = new double[covIdx.length][heArgs.getCovNumber().length];
		for (int subjectIdx = 0; subjectIdx < covIdx.length; subjectIdx++)
		{
			x[subjectIdx] = data.getVariable(covFileIdx, covIdx[subjectIdx], heArgs.getCovNumber());
		}
		return x;
	}

	private double[] readPhenotype()
	{
		int[] pheIdx = data.getMatchedSubjectIdx(pheFileIdx);
		double[] y = new double[pheIdx.length];
		for (int subjectIdx = 0; subjectIdx < y.length; subjectIdx++)
		{
			y[subjectIdx] = data.getVariable(pheFileIdx, pheIdx[subjectIdx], heArgs.getPhenotypeIdx()[0]);
		}
		return y;
	}

	private void adjustY()
	{		
		OLSMultipleLinearRegression mod = new OLSMultipleLinearRegression();
		mod.newSampleData(Y, X);
		double[] res = mod.estimateResiduals();
		System.arraycopy(res, 0, Y, 0, Y.length);
	}

	private double[][][] lineUpGenotype(double[][][] B)
	{
		int[] subIdx = data.getMatchedSubjectIdx(ibdFileIdx);
		double[][][] b = new double[B.length][subIdx.length][subIdx.length];
		
		for (int i = 0; i < b.length; i++)
		{
			b[i] = lineUpMatrix(B[i], subIdx);
		}
		return b;
	}

	private double[][] lineUpMatrix(double[][] B, int[] subIdx)
	{
		double[][] a = new double[subIdx.length][subIdx.length];

		for (int i = 0; i < subIdx.length; i++)
		{
			for (int j = 0; j < subIdx.length; j++)
			{
				a[i][j] = a[j][i] = B[subIdx[i]][subIdx[j]];
			}
		}
		return a;
	}

	private double[][][] readIBDList(int numSubjects)
	{
		double[][][] B = new double[heArgs.getIBDList().length][numSubjects][numSubjects];

		for (int i = 0; i < B.length; i++)
		{
			for (int j = 0; j < B[i].length; j++)
			{
				Arrays.fill(B[i][j], ibd_default);
			}
		}

		String[] tokens = null;
		for (int z = 0; z < heArgs.getIBDList().length; z++)
		{
			BufferedReader reader = BufferedReader.openGZipFile(heArgs.getIBDList()[z] + ".ibd.gz", "IBD (gzip)");

			while ((tokens = reader.readTokens(4)) != null)
			{
				int i1 = Integer.parseInt(tokens[0]);
				int i2 = Integer.parseInt(tokens[1]);
				B[z][i1-1][i2-1] = B[z][i2-1][i1-1] = Double.parseDouble(tokens[3]);
			}
			reader.close();
		}
		return lineUpGenotype(B);
	}

	private void readIBD(int numSubjects)
	{
		BufferedReader reader = BufferedReader.openGZipFile(heArgs.getIBDGZ(), "IBD (gzip)");
		makeIBDMatrix(reader, numSubjects);
	}

	private void makeIBDMatrix(BufferedReader reader, int numSubjects)
	{
		double[][] B = new double[numSubjects][numSubjects];
		for(int i = 0; i < B.length; i++)
		{
			Arrays.fill(B[i], ibd_default);
		}
		String[] tokens = null;
		while( (tokens = reader.readTokens(4) ) != null )
		{
			int i1 = Integer.parseInt(tokens[0]);
			int i2 = Integer.parseInt(tokens[1]);
			B[i1-1][i2-1] = B[i2-1][i1-1] = Double.parseDouble(tokens[3]);
		}
		reader.close();
		A3 = new double[1][data.getMatchedSubjectIdx(ibdFileIdx).length][data.getMatchedSubjectIdx(ibdFileIdx).length];
		A3[0] = lineUpMatrix(B, data.getMatchedSubjectIdx(ibdFileIdx));
	}

	private void SummaryA3()
	{
		mA = new double[A3.length];
		vA = new double[A3.length][A3.length];
		for (int i = 0; i < A3.length; i++)
		{
			for (int j = 0; j <=i; j++)
			{
				double[] a = calA2(A3[i], A3[j]);
				if (i == j) mA[j] = a[0];
				vA[i][j] = vA[j][i] = a[1];
			}
		}
	}

	private double[] calA2(double[][] b1, double[][] b2)
	{
		double Sx1 = 0;
		double Sx2 = 0;
		double Sxx = 0;

		double N = 0;
		for (int i = 1; i < b1.length; i++)
		{
			for (int j = 0; j < i; j++)
			{
				if (b1[i][j] == ibd_default) continue;
				Sx1 += b1[i][j];
				Sx2 += b2[i][j];
				Sxx += b1[i][j] * b2[i][j];
				N++;
			}
		}

		double[] br = {Sx1/N, (Sxx/N - Sx1/N*Sx2/N)};
		return br;
	}

	private int ibdFileIdx = 0;
	private int pheFileIdx = 1;
	private int covFileIdx = 2;

	private double[][][] A3;
	private final static double ibd_default = -100;

	private double[] Y = null;
	private double[][] X = null;
	private double[] beta = null;
	private double[][] beta_v = null;
//	private double[] jbeta = null;
	private double[] jBeta = null;
	private double[] jBetaSe = null;

	private HEFamCommandArguments heArgs = null;
	private InputDataSet2 data = null;

	private double[] mA;
	private double[][] vA;

	private DecimalFormat fmt = new DecimalFormat("0.0000");
	private DecimalFormat fmtE = new DecimalFormat("0.00E0");
}
