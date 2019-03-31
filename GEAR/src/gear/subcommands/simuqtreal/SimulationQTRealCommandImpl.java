package gear.subcommands.simuqtreal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.StatUtils;

import gear.ConstValues;
import gear.family.GenoMatrix.GenotypeMatrix;
import gear.family.pedigree.Hukou;
import gear.family.pedigree.file.SNP;
import gear.family.plink.PLINKParser;
import gear.qc.sampleqc.SampleFilter;
import gear.subcommands.CommandArguments;
import gear.subcommands.CommandImpl;
import gear.util.FileUtil;
import gear.util.Logger;
import gear.util.NewIt;
import gear.util.Sample;

public class SimulationQTRealCommandImpl extends CommandImpl
{
	private SimulationQTRealCommandArguments qtArgs;
	private SampleFilter sf;
	private GenotypeMatrix pGM;

	private RandomDataImpl rnd = new RandomDataImpl();
	private long seed;

	private int nullM;
	private int rep;

	private PLINKParser pp = null;
	private double[][] genotype;
	private double[] BV;
	private double[][] phenotype;

	private double[] effect;
	private double h2;

	private HashMap<String, Double> RefEffectMap = null;
	@Override
	public void execute(CommandArguments cmdArgs)
	{
		qtArgs = (SimulationQTRealCommandArguments) cmdArgs;

		pp = PLINKParser.parse(this.qtArgs);
		sf = new SampleFilter(pp.getPedigreeData(), cmdArgs);
		sf.qualification();
		pGM = new GenotypeMatrix(sf.getSample(), pp.getMapData(), cmdArgs);

		h2 = qtArgs.getHsq();
		nullM = 0;
		seed = qtArgs.getSeed();
		rnd.reSeed(seed);
		rep = qtArgs.getRep();

		getEffect();
		generateSampleNoSelection();
		writePhenoFile();
	}

	private void generateSampleNoSelection()
	{
		DecimalFormat fmt = new DecimalFormat("#.###E0");

		RealMatrix Meffect = new Array2DRowRealMatrix(effect);
		genotype = new double[pGM.getNumIndivdial()][pGM.getNumMarker()];
		phenotype = new double[pGM.getNumIndivdial()][rep];
		BV = new double[pGM.getNumIndivdial()];

		for(int i = 0; i < pGM.getNumIndivdial(); i++)
		{
			RealMatrix chr = SampleRealChromosome(i);
			RealMatrix genoEff = chr.transpose().multiply(Meffect);

			double bv = genoEff.getEntry(0, 0);
			BV[i] = bv;
			genotype[i] = chr.getColumn(0);
		}
		if (h2 == 0)
		{
			Arrays.fill(BV, 0);
		}

		double vg = StatUtils.variance(BV);
		if (vg == 0)
		{
			Logger.printUserLog("Vg=0, heritability is scale to zero." );
		}
		//rescale the phenotype to get the heritability and residual
		double ve = (h2 == 0 || vg == 0) ? 1:vg * (1 - h2) / h2;
		double E = Math.sqrt(ve);
		Logger.printUserLog("Vg=" + fmt.format(vg));
		for (int i = 0; i < rep; i++)
		{
			double[] pv=new double[pGM.getNumIndivdial()];
			for (int j = 0; j < pGM.getNumIndivdial(); j++)
			{
				phenotype[j][i] = BV[j] + rnd.nextGaussian(0, E);
				pv[j] = phenotype[j][i];
			}
			double Vp=StatUtils.variance(pv);
			Logger.printUserLog("Vp=" + fmt.format(Vp) + "; hsq=" + fmt.format(vg/Vp) + " for replicate " + (i+1));
		}
		Logger.printUserLog("Total individuals visited (no selection): "
				+ BV.length + "\n");
	}

	private RealMatrix SampleRealChromosome(int sIdx)
	{
		double[] gn = new double[pGM.getNumMarker()];
		for (int i = 0; i < gn.length; i++)
		{
			int g = pGM.getAdditiveScoreOnFirstAllele(sIdx, i);
			gn[i] = (g == ConstValues.MISSING_GENOTYPE)? 0:g;
		}
		return new Array2DRowRealMatrix(gn);
	}

	private void getEffect()
	{
		effect = new double[pGM.getNumMarker()];
		Sample.setSeed(seed);
		int[] idx = Sample.SampleIndex(0, pGM.getNumMarker()-1, pGM.getNumMarker()-nullM);
		Arrays.sort(idx);

		if(qtArgs.isPlainEffect())
		{
			for(int i = 0; i < idx.length; i++) effect[idx[i]] = qtArgs.getPolyEffect();
		}
		else if (qtArgs.isPolyEffect())
		{
			for (int i = 0; i < idx.length; i++)
			{
				effect[idx[i]] = rnd.nextGaussian(0, 1);
			}
		}
		else if (qtArgs.isPolyEffectSort())
		{
			NormalDistributionImpl ndImpl = new NormalDistributionImpl();
			ndImpl.reseedRandomGenerator(qtArgs.getSeed());
			for (int i = 0; i < idx.length; i++)
			{
				try
				{
					effect[idx[i]] = ndImpl.inverseCumulativeProbability((i+0.5)/(idx.length));
				}
				catch (MathException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (qtArgs.isPolyEffectFile())
		{
			BufferedReader reader = FileUtil.FileOpen(qtArgs.getPolyEffectFile());
			int c = 0;
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null)
				{
					if(c >= pGM.getNumMarker())
					{
						Logger.printUserLog("Have already read " + pGM.getNumMarker() + " allelic effects. Ignore the rest of the content in '" + qtArgs.getPolyEffectFile() + "'.");
						break;
					}

					line.trim();
					String[] l = line.split(ConstValues.WHITESPACE_DELIMITER);
					if (l.length < 1) continue;
					effect[c++] = Double.parseDouble(l[0]);
				}
				reader.close();
			}
			catch (IOException e)
			{
				Logger.handleException(e,
						"An exception occurred when reading the frequency file '"
								+ qtArgs.getPolyEffectFile() + "'.");
			}
		}
		else if (qtArgs.isRefEffectFile())
		{
			RefEffectMap = NewIt.newHashMap();

			BufferedReader reader = FileUtil.FileOpen(qtArgs.getRefEffectFile());
			int c = 0;
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null)
				{
					if(c >= pGM.getNumMarker())
					{
						Logger.printUserLog("Have already read " + pGM.getNumMarker() + " allelic effects.  Ignore the rest of the content in '" + qtArgs.getRefEffectFile() + "'.");
						break;
					}

					line.trim();
					String[] l = line.split(ConstValues.WHITESPACE_DELIMITER);
					if (l.length < 3) continue;
					RefEffectMap.put(l[0]+"_"+l[1], Double.parseDouble(l[2]));
				}
				reader.close();
			}
			catch (IOException e)
			{
				Logger.handleException(e,
						"An exception occurred when reading the frequency file '"
								+ qtArgs.getPolyEffectFile() + "'.");
			}

			Logger.printUserLog("Read " + RefEffectMap.size() + " referenced effects from '" + qtArgs.getRefEffectFile() + "'.");
			for(int i = 0; i < pGM.getSNPList().size(); i++)
			{
				SNP snp = pGM.getSNPList().get(i);
				String snp1 = snp.getName() + "_" + snp.getFirstAllele();
				String snp2 = snp.getName() + "_" + snp.getSecAllele();
				if (RefEffectMap.containsKey(snp1))
				{
					effect[i] = RefEffectMap.get(snp1);
					continue;
				}
				if (RefEffectMap.containsKey(snp2))
				{
					effect[i] = -1 * RefEffectMap.get(snp2);
				}
			}
		}

		if (h2 == 0)
		{
			Arrays.fill(effect, 0);
		}
	}

	public void writePhenoFile()
	{
		PrintWriter phe = null;
		PrintWriter eff = null;
		PrintWriter breed = null;

		try
		{
			phe = new PrintWriter(new BufferedWriter(new FileWriter(qtArgs.getOutRoot()
					+ ".phe")));
			eff = new PrintWriter(new BufferedWriter(new FileWriter(qtArgs.getOutRoot() + ".rnd")));
			breed = new PrintWriter(new BufferedWriter(new FileWriter(qtArgs.getOutRoot() + ".breed")));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		ArrayList<Hukou> hukouBook = pp.getPedigreeData().getHukouBook();
		for (int i = 0; i < genotype.length; i++)
		{
			Hukou hk = hukouBook.get(i);
			String fid = hk.getFamilyID();
			String iid = hk.getIndividualID();
			phe.print(fid + "\t" + iid);
			breed.println(fid + "\t" + iid + "\t" + BV[i]);

			for (int j = 0; j < rep; j++)
			{
				phe.print("\t" + phenotype[i][j]);
			}
			phe.println();
		}

		for (int i = 0; i < pGM.getSNPList().size(); i++)
		{
			eff.println(pGM.getSNPList().get(i).getName() + "\t" +  String.valueOf(pGM.getSNPList().get(i).getFirstAllele())  + "\t" + effect[i]);
		}
		phe.close();
		eff.close();
		breed.close();
	}

}
