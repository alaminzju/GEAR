package gear.subcommands.hereg;

import java.util.ArrayList;

import gear.subcommands.CommandArguments;
import gear.util.BufferedReader;
import gear.util.FileUtil;
import gear.util.Logger;
import gear.util.NewIt;

public class HERegCommandArguments extends CommandArguments 
{
	public void setSD()
	{
		isSD = true;
		isSS = false;
		isCP = false;
	}
	
	public boolean isSD()
	{
		return isSD;
	}

	public void setSS()
	{
		isSD = false;
		isSS = true;
		isCP = false;
	}

	public boolean isSS()
	{
		return isSS;
	}
	
	public void setCP()
	{
		isSD = false;
		isSS = false;
		isCP = true;
	}
	
	public boolean isCP()
	{
		return isCP;
	}

	public String getGrmBin()
	{
		return grmBin;
	}
	
	public void setGrmBin(String grmBin)
	{
		this.grmBin = grmBin;
	}

	public String getGrmText()
	{
		return grmText;
	}
	
	public void setGrmText(String grmText)
	{
		this.grmText = grmText;
	}
	
	public String getGrmGZ()
	{
		return grmGZ;
	}
	
	public void setGrmGZ(String grmGZ)
	{
		this.grmGZ = grmGZ;
	}
	
	public String getGrmID()
	{
		return grmID;
	}
	
	public void setGrmID(String grmID)
	{
		this.grmID = grmID;
	}
	

	public String getPhenotypeFile()
	{
		return pheFile;
	}

	public void setPhenotypeFile(String pheFile)
	{
		this.pheFile = pheFile;
	}

	public void setPhenotypeIdx(String pIdx)
	{
		this.pheIdx[0] = Integer.parseInt(pIdx);
		if (this.pheIdx[0] < 1)
		{
			Logger.printUserLog("Phenotype index should be greater than 1.\nGEAR quit.");
			System.exit(1);
		}
		this.pheIdx[0]--;
	}

	public int[] getPhenotypeIdx()
	{
		return pheIdx;
	}

	public void setGrmList(String grmL) 
	{
		grmList = grmL;
		FileUtil.exists(grmList);
		ArrayList<String> gFile = NewIt.newArrayList();
		BufferedReader reader = BufferedReader.openTextFile(grmList, "GRM-list");

		String[] tokens = null;
		while((tokens = reader.readTokens())!=null)
		{
			gFile.add(tokens[0]);
		}
		grmGZList = gFile.toArray(new String[0]);
		isGRMList = true;
	}

	public boolean isGRMList()
	{
		return isGRMList;
	}
	
	public String[] getGrmList()
	{
		return grmGZList;
	}
	
	public void setScale(boolean flag) 
	{
		isScale = flag;
	}

	public boolean isScale()
	{
		return isScale;
	}

	public void setCovFile(String cFile) 
	{
		FileUtil.exists(cFile);
		covFile = cFile;
	}

	public String getCovFile()
	{
		return covFile;
	}

	public void setCovNumber(String[] cIdx) 
	{
		covIdx = new int[cIdx.length];
		for (int i = 0; i < covIdx.length; i++)
		{
			covIdx[i] = Integer.parseInt(cIdx[i]);
			if (covIdx[i] < 1)
			{
				Logger.printUserLog(covIdx[i] +"< 1. Incorrect index for covar-number.");
				Logger.printUserLog("GEAR quittted.");
				System.exit(1);
			}
			covIdx[i]--;
		}
	}
	
	public int[] getCovNumber()
	{
		return covIdx;
	}

	public void setKeepFile(String kFile) 
	{
		FileUtil.exists(kFile);
		keepFile = kFile;
	}
	
	public String getKeepFile()
	{
		return keepFile;
	}

	public void setGRMcutoff(String gc) 
	{
		this.isGRMCut = true;
		this.grmCutoff = Double.parseDouble(gc);
	}

	public double getGRMcutoff()
	{
		return grmCutoff;
	}

	public boolean isGRMcut()
	{
		return isGRMCut;
	}

	public void setJackknife() 
	{
		isJackknife = true;
	}

	public boolean isJackknife()
	{
		return isJackknife;
	}

	public void setGrmDom()
	{
		isDom = true;
	}
	
	public boolean isDom()
	{
		return isDom;
	}

	public void setFam()
	{
		isFam = true;
	}
	
	public boolean isFam()
	{
		return isFam;
	}

	private boolean isJackknife = false;
	private String covFile = null;
	private int[] covIdx = {0};
	private String grmBin;
	private String grmText;  // root name of the GRM files
	private String grmGZ;  // root name of the GRM files
	private String grmID;
	private String grmList;
	private boolean isGRMList;
	private String[] grmGZList;
	private boolean isScale;

	private String pheFile = null;
	private int[] pheIdx = {0};
	private String keepFile = null;

	private boolean isSD = true;
	private boolean isSS = false;
	private boolean isCP = false;
	
	private boolean isDom = false;

	private boolean isGRMCut = false;
	private double grmCutoff = 0;

	private boolean isFam = false;
}
