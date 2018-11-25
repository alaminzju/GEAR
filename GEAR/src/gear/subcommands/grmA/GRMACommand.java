package gear.subcommands.grmA;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gear.subcommands.Command;
import gear.subcommands.CommandArgumentException;
import gear.subcommands.CommandArguments;
import gear.subcommands.CommandImpl;

public class GRMACommand extends Command {

	@Override
	public String getName() {
		return "grm";
	}

	@Override
	public String getDescription() {
		return "Constructing whole-genome ibs matrix";
	}

	@SuppressWarnings("static-access")
	@Override
	public void prepareOptions(Options options) {
		options.addOption(OptionBuilder.withDescription(OPT_BFILE_DESC).withLongOpt(OPT_BFILE_LONG).hasArg().isRequired().create());
//		options.addOption(OptionBuilder.withDescription(OPT_FILE_DESC).withLongOpt(OPT_FILE_LONG).hasArg().create());

		options.addOption(OptionBuilder.withDescription(OPT_GZ_DESC).create(OPT_GZ));
		options.addOption(OptionBuilder.withDescription(OPT_TXT_DESC).create(OPT_TXT));
		options.addOption(OptionBuilder.withDescription(OPT_DOM_DESC).create(OPT_DOM));
		options.addOption(OptionBuilder.withDescription(OPT_GUI_DESC).withLongOpt(OPT_GUI_LONG).create());

		options.addOption(OptionBuilder.withDescription(OPT_ADJ_VAR_DESC).withLongOpt(OPT_ADJ_VAR_LONG).create());
		options.addOption(OptionBuilder.withDescription(OPT_INBRED_DESC).withLongOpt(OPT_INBRED_LONG).create());

		options.addOption(OptionBuilder.withDescription(OPT_INBRED_LIST_DESC).withLongOpt(OPT_INBRED_LIST_LONG).hasArgs().create());

		options.addOption(OptionBuilder.withDescription(OPT_KEEP_DESC).withLongOpt(OPT_KEEP_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_REMOVE_DESC).withLongOpt(OPT_REMOVE_LONG).hasArg().create());
		options.addOption(OptionBuilder.withDescription(OPT_KEEP_FAM_DESC).withLongOpt(OPT_KEEP_FAM_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_REMOVE_FAM_DESC).withLongOpt(OPT_REMOVE_FAM_LONG).hasArg().create());

		options.addOption(
				OptionBuilder.withDescription(OPT_EXTRACT_DESC).withLongOpt(OPT_EXTRACT_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_EXCLUDE_DESC).withLongOpt(OPT_EXCLUDE_LONG).hasArg().create());

		options.addOption(OptionBuilder.withDescription(OPT_CHR_DESC).withLongOpt(OPT_CHR_LONG).hasArgs().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_NOT_CHR_DESC).withLongOpt(OPT_NOT_CHR_LONG).hasArgs().create());

		options.addOption(OptionBuilder.withDescription(OPT_MAF_DESC).withLongOpt(OPT_MAF_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_MAX_MAF_DESC).withLongOpt(OPT_MAX_MAF_LONG).hasArg().create());
		options.addOption(OptionBuilder.withDescription(OPT_GENO_DESC).withLongOpt(OPT_GENO_LONG).hasArg().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_ZERO_VAR_DESC).withLongOpt(OPT_ZERO_VAR_LONG).create());
		options.addOption(
				OptionBuilder.withDescription(OPT_MAF_RANGE_DESC).withLongOpt(OPT_MAF_RANGE_LONG).hasArgs().create());

	}

	@Override
	public CommandArguments parse(CommandLine cmdLine) throws CommandArgumentException {
		GRMACommandArguments grmArgs = new GRMACommandArguments();

		parseFileArguments((CommandArguments) grmArgs, cmdLine);

		parseSampleFilterArguments((CommandArguments) grmArgs, cmdLine);
		parseFamilyFilterArguments((CommandArguments) grmArgs, cmdLine);

		parseSNPFilterFileArguments((CommandArguments) grmArgs, cmdLine);
		parseSNPFilterChromosomeArguments((CommandArguments) grmArgs, cmdLine);

		parseMAFArguments((CommandArguments) grmArgs, cmdLine);
		parseMAXMAFArguments((CommandArguments) grmArgs, cmdLine);
		parseGENOArguments((CommandArguments) grmArgs, cmdLine);
		parseZeroVarArguments((CommandArguments) grmArgs, cmdLine);

		parseMAFRangeArguments((CommandArguments) grmArgs, cmdLine);

		if (cmdLine.hasOption(OPT_GZ)) {
			grmArgs.setGZ();
		}
		if (cmdLine.hasOption(OPT_TXT)) {
			grmArgs.setTxt();
		}
		if (cmdLine.hasOption(OPT_ADJ_VAR_LONG)) {
			grmArgs.setAdjVar();
		}
		if (cmdLine.hasOption(OPT_INBRED_LIST_LONG)) {
			grmArgs.setInbedList(cmdLine.getOptionValue(OPT_INBRED_LIST_LONG));
		}
		if (cmdLine.hasOption(OPT_INBRED_LONG)) {
			grmArgs.setInbred();
		} else {
			if (cmdLine.hasOption(OPT_DOM)) {
				grmArgs.setDom();
			}
		}
		if (cmdLine.hasOption(OPT_GUI_LONG)) {
			grmArgs.setGUI();
		}
		return grmArgs;
	}

	@Override
	protected CommandImpl createCommandImpl() {
		return new GRMACommandImpl();
	}

	private static final String OPT_GZ = "gz";
	private static final String OPT_GZ_DESC = "make gz format for grm.";

	private static final String OPT_TXT = "txt";
	private static final String OPT_TXT_DESC = "make text format for grm.";

	private static final String OPT_ADJ_VAR_LONG = "adj-var";
	private static final String OPT_ADJ_VAR_DESC = "denominator is real variance of the locus";

	private static final String OPT_INBRED_LONG = "inbred";
	private static final String OPT_INBRED_DESC = "denominator is 4pq";

	private static final String OPT_DOM = "dom";
	private static final String OPT_DOM_DESC = "dominance relationship";
	
	private static final String OPT_INBRED_LIST_LONG = "inbred-list";
	private static final String OPT_INBRED_LIST_DESC = "inbred individual list";	
	
	private final static String OPT_GUI_LONG = "gui";
	private final static String OPT_GUI_DESC = "GUI";
}
