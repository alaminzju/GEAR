package gear.family.qc.colqc;

import gear.subcommands.CommandArguments;

public interface SNPFilterInterface
{

	public void SelectSNP();

	public void SelectSNP(CommandArguments cmdArgs);

	public int[] getWorkingSNP();

	public int[] getBgSeq();

	public int[] getWSeq();

	public int[][] getWSeq2();

}
