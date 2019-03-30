package gear.subcommands.bluppca;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gear.subcommands.Command;
import gear.subcommands.CommandArgumentException;
import gear.subcommands.CommandArguments;
import gear.subcommands.CommandImpl;

public class BlupPcaCommand extends Command {
	@Override
	public String getName() {
		return "bluppca";
	}

	@Override
	public String getDescription() {
		return "Calculate the SNP effects with BLUP-PCA";
	}

	@SuppressWarnings("static-access")
	@Override
	public void prepareOptions(Options options) {
		options.addOption(
				OptionBuilder.withDescription(OPT_GRM_BIN_DESC).withLongOpt(OPT_GRM_BIN_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_GRM_TXT_DESC).withLongOpt(OPT_GRM_TXT_LONG).hasArg().create());
		options.addOption(OptionBuilder.withDescription(OPT_GRM_DESC).hasArg().create(OPT_GRM));
		options.addOption(OptionBuilder.withDescription(OPT_BFILE_DESC).withLongOpt(OPT_BFILE_LONG).hasArg().isRequired().create());
//		options.addOption(OptionBuilder.withDescription(OPT_FILE_DESC).withLongOpt(OPT_FILE_LONG).hasArg().create());
		options.addOption(OptionBuilder.withDescription(OPT_PHE_DESC).hasArg().isRequired().create(OPT_PHE));
		options.addOption(OptionBuilder.withDescription(OPT_MPHE_DESC).hasArgs().create(OPT_MPHE));
		
		options.addOption(
				OptionBuilder.withDescription(OPT_THREAD_NUM_LONG_DESC).withLongOpt(OPT_THREAD_NUM_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_THREAD_GREEDY_LONG_DESC).withLongOpt(OPT_THREAD_GREEDY_LONG).hasArg().create());

	}

	@Override
	public CommandArguments parse(CommandLine cmdLine) throws CommandArgumentException {
		BlupPcaCommandArguments blupArgs = new BlupPcaCommandArguments();
		parseFileArguments((CommandArguments) blupArgs, cmdLine);
		parsePhenoFileArguments((CommandArguments) blupArgs, cmdLine);
		parsePhenoIndexArguments((CommandArguments) blupArgs, cmdLine);
		parseThreadNumArguments((CommandArguments) blupArgs, cmdLine);

		parseGRMArguments(blupArgs, cmdLine);
		return blupArgs;
	}

	private void parseGRMArguments(BlupPcaCommandArguments blupArgs, CommandLine cmdLine)
			throws CommandArgumentException {
		String grmBin = cmdLine.getOptionValue(OPT_GRM_BIN_LONG);
		String grmText = cmdLine.getOptionValue(OPT_GRM_TXT_LONG);
		String grmGZ = cmdLine.getOptionValue(OPT_GRM);

		int numFiles = 0;

		if (grmBin != null) {
			blupArgs.setGrmBin(grmBin + ".grm.bin");
			blupArgs.setGrmID(grmBin + ".grm.id");
			++numFiles;
		}

		if (grmText != null) {
			blupArgs.setGrmText(grmText + ".grm");
			blupArgs.setGrmID(grmText + ".grm.id");
			++numFiles;
		}

		if (grmGZ != null) {
			blupArgs.setGrmGZ(grmGZ + ".grm.gz");
			blupArgs.setGrmID(grmGZ + ".grm.id");
			++numFiles;
		}

		if (numFiles == 0) {
			throw new CommandArgumentException("No GRM is provided. One of --" + OPT_GRM_BIN_LONG + ", "
					+ OPT_GRM_TXT_LONG + " or --" + OPT_GRM + " must be set.");
		}

		if (numFiles > 1) {
			throw new CommandArgumentException("At most one of --" + OPT_GRM_BIN_LONG + ", --" + OPT_GRM_TXT_LONG
					+ " and --" + OPT_GRM + " can be set.");
		}
	}

	@Override
	protected CommandImpl createCommandImpl() {
		return new BlupPcaCommandImpl();
	}

	private final static String OPT_GRM_BIN_LONG = "grm-bin";
	private final static String OPT_GRM_BIN_DESC = "Specify the .grm.bin and .grm.id files";

	private final static String OPT_GRM = "grm";
	private final static String OPT_GRM_DESC = "Specify the .grm and .grm.id files";

	private final static String OPT_GRM_TXT_LONG = "grm-txt";
	private final static String OPT_GRM_TXT_DESC = "Specify the .grm.txt and .grm.id files";

}
