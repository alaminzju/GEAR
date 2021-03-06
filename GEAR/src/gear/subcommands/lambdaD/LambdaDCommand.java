package gear.subcommands.lambdaD;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gear.subcommands.Command;
import gear.subcommands.CommandArgumentException;
import gear.subcommands.CommandArguments;
import gear.subcommands.CommandImpl;

public class LambdaDCommand extends Command {
	public LambdaDCommand() {
		addAlias("lam");
	}

	@Override
	public String getName() {
		return "lambdad";
	}

	@Override
	public String getDescription() {
		return "Calculate Lambda deflation parameter for a pair of meta-analysis";
	}

	@SuppressWarnings("static-access")
	@Override
	public void prepareOptions(Options options) {
		options.addOption(
				OptionBuilder.withDescription(OPT_META_DESC).withLongOpt(OPT_META_LONG).hasArgs().create(OPT_META));
		options.addOption(OptionBuilder.withDescription(OPT_META_BATCH_DESC).withLongOpt(OPT_META_BATCH_LONG).hasArg()
				.create(OPT_META_BATCH));
		options.addOption(OptionBuilder.withDescription(OPT_META_GZ_DESC).withLongOpt(OPT_META_GZ_LONG).hasArgs()
				.create(OPT_META_GZ));
		options.addOption(OptionBuilder.withDescription(OPT_META_GZ_BATCH_DESC).withLongOpt(OPT_META_GZ_BATCH_LONG)
				.hasArg().create(OPT_META_GZ_BATCH));

		options.addOption(OptionBuilder.withDescription(OPT_ME_DESC).hasArgs(1).create(OPT_ME));
		options.addOption(OptionBuilder.withDescription(OPT_ME_FRAC_LONG_DESC).withLongOpt(OPT_ME_FRAC_LONG)
				.hasOptionalArg().create());

		options.addOption(OptionBuilder.withDescription(OPT_CC_DESC).hasArgs().create(OPT_CC));
		options.addOption(
				OptionBuilder.withDescription(OPT_CC_SIZE_LONG_DESC).withLongOpt(OPT_CC_SIZE_LONG).hasArg().create());
		options.addOption(OptionBuilder.withDescription(OPT_QT_DESC).hasArgs().create(OPT_QT));
		options.addOption(
				OptionBuilder.withDescription(OPT_QT_SIZE_LONG_DESC).withLongOpt(OPT_QT_SIZE_LONG).hasArg().create());

		options.addOption(OptionBuilder.withDescription(OPT_KEY_DESC).hasArgs().create(OPT_KEY));
		options.addOption(
				OptionBuilder.withDescription(OPT_VERBOSE_DESC).withLongOpt(OPT_VERBOSE_LONG).create(OPT_VERBOSE));

		options.addOption(OptionBuilder.withDescription(OPT_CHR_DESC).withLongOpt(OPT_CHR_LONG).hasArgs().create());
		options.addOption(
				OptionBuilder.withDescription(OPT_NOT_CHR_DESC).withLongOpt(OPT_NOT_CHR_LONG).hasArgs().create());

		// options.addOption(OptionBuilder.withDescription(OPT_FST_DESC).create(OPT_FST));
		options.addOption(OptionBuilder.withDescription(OPT_TOP_DESC).hasArg().create(OPT_TOP));
		options.addOption(OptionBuilder.withDescription(OPT_RAPID_DESC).create(OPT_RAPID));

		options.addOption(OptionBuilder.withDescription(OPT_RCM_DESC).create(OPT_RCM));
		options.addOption(OptionBuilder.withDescription(OPT_TRIM_DESC).hasOptionalArg().create(OPT_TRIM));

	}

	@Override
	public CommandArguments parse(CommandLine cmdLine) throws CommandArgumentException {
		LambdaDCommandArguments lamD = new LambdaDCommandArguments();

		parseSNPFilterChromosomeArguments((CommandArguments) lamD, cmdLine);
		lamD.setMe(OPT_ME_DEFAULT);

		// manual text meta files
		if (cmdLine.hasOption(OPT_META)) {
			lamD.setMetaFile(cmdLine.getOptionValues(OPT_META));
			lamD.setGZ(false);
			if (cmdLine.hasOption(OPT_QT)) {
				lamD.setQT(cmdLine.getOptionValues(OPT_QT));
			}
			if (cmdLine.hasOption(OPT_CC)) {
				lamD.setCC(cmdLine.getOptionValues(OPT_CC));
			}
		}

		// batch text meta batch
		if (cmdLine.hasOption(OPT_META_BATCH)) {
			lamD.setMetaBatch(cmdLine.getOptionValue(OPT_META_BATCH));
			lamD.setGZ(false);
			if (cmdLine.hasOption(OPT_QT_SIZE_LONG)) {
				lamD.setQTbatch(cmdLine.getOptionValue(OPT_QT_SIZE_LONG));
			}
			if (cmdLine.hasOption(OPT_CC_SIZE_LONG)) {
				lamD.setCCbatch(cmdLine.getOptionValue(OPT_CC_SIZE_LONG));
			}
		}

		// me
		if (cmdLine.hasOption(OPT_ME)) {
			lamD.setMe(cmdLine.getOptionValue(OPT_ME));
		}

		// manual gzip meta files
		if (cmdLine.hasOption(OPT_META_GZ)) {
			lamD.setMetaFile(cmdLine.getOptionValues(OPT_META_GZ));
			lamD.setGZ(true);
			if (cmdLine.hasOption(OPT_QT)) {
				lamD.setQT(cmdLine.getOptionValues(OPT_QT));
			}
			if (cmdLine.hasOption(OPT_CC)) {
				lamD.setCC(cmdLine.getOptionValues(OPT_CC));
			}
		}

		// batch gzip meta files
		if (cmdLine.hasOption(OPT_META_GZ_BATCH)) {
			lamD.setMetaBatch(cmdLine.getOptionValue(OPT_META_GZ_BATCH));
			lamD.setGZ(true);
			if (cmdLine.hasOption(OPT_QT_SIZE_LONG)) {
				lamD.setQTbatch(cmdLine.getOptionValue(OPT_QT_SIZE_LONG));
			}
			if (cmdLine.hasOption(OPT_CC_SIZE_LONG)) {
				lamD.setCCbatch(cmdLine.getOptionValue(OPT_CC_SIZE_LONG));
			}
		}

		if (cmdLine.hasOption(OPT_KEY)) {
			lamD.setKey(cmdLine.getOptionValues(OPT_KEY));
		}

		if (cmdLine.hasOption(OPT_VERBOSE)) {
			lamD.setVerbose();
		}

		// if (cmdLine.hasOption(OPT_VERBOSE_GZ))
		// {
		// lamD.setVerboseGZ();
		// }

		// if (cmdLine.hasOption(OPT_FST))
		// {
		// lamD.setFst();
		// }

		if (cmdLine.hasOption(OPT_TOP)) {
			lamD.setTop(cmdLine.getOptionValue(OPT_TOP));
		}

		// if (cmdLine.hasOption(OPT_CLEAN))
		// {
		// lamD.setClean();
		// }

		if (cmdLine.hasOption(OPT_RAPID)) {
			lamD.setRapid();
		}

		if (cmdLine.hasOption(OPT_RCM)) {
			lamD.setRandom();
		}

		if (cmdLine.hasOption(OPT_TRIM)) {
			lamD.setTrim(parseDoubleOptionValueInRange(cmdLine, OPT_TRIM, OPT_TRIM_DEFAULT, 0, 0.45));
		}

		lamD.setMeFrac(parseDoubleOptionValueInRange(cmdLine, OPT_ME_FRAC_LONG, OPT_ME_FRAC_LONG_DEFAULT, 0.01, 1));

		return lamD;
	}

	@Override
	protected CommandImpl createCommandImpl() {
		return new LambdaDCommandImpl();
	}

	private final static String OPT_META = "m";
	private final static String OPT_META_LONG = "meta";
	private final static String OPT_META_DESC = "The summary statistic files";

	private final static String OPT_META_GZ = "mg";
	private final static String OPT_META_GZ_LONG = "meta-gz";
	private final static String OPT_META_GZ_DESC = "The summary statistic files in gz format";

	private final static String OPT_CC = "cc";
	private final static String OPT_CC_DESC = "Case-control study: #case 1, #ctrl 1, #case 2, #ctrl 2";

	private final static String OPT_QT = "qt";
	private final static String OPT_QT_DESC = "Quantitative trait: #sample size 1, #sample size 2";

	private final static String OPT_META_BATCH = "mb";
	private final static String OPT_META_BATCH_LONG = "meta-batch";
	private final static String OPT_META_BATCH_DESC = "The summary statistic batch";

	private final static String OPT_META_GZ_BATCH = "mgb";
	private final static String OPT_META_GZ_BATCH_LONG = "meta-gz-batch";
	private final static String OPT_META_GZ_BATCH_DESC = "The summary statistic files in gz format";

	private final static String OPT_CC_SIZE_LONG = "cc-size";
	private final static String OPT_CC_SIZE_LONG_DESC = "Case-control study: #case 1, #ctrl 1, #case 2, #ctrl 2";

	private final static String OPT_QT_SIZE_LONG = "qt-size";
	private final static String OPT_QT_SIZE_LONG_DESC = "Quantitative trait: #sample size 1, #sample size 2";

	private final static String OPT_KEY = "key";
	private final static String OPT_KEY_DESC = "Self defined key workds: snp, beta, se, a1, a2, chr, bp, p";

	private final static String OPT_VERBOSE = "v";
	private final static String OPT_VERBOSE_LONG = "verbose";
	private final static String OPT_VERBOSE_DESC = "Print test statistic for every pair of meta files.";

	private final static String OPT_ME = "me";
	private final static String OPT_ME_DEFAULT = "30000";
	private final static String OPT_ME_DESC = "effective number of markers.";

	private final static String OPT_ME_FRAC_LONG = "me-frac";
	private final static String OPT_ME_FRAC_LONG_DEFAULT = "0.05";
	private final static String OPT_ME_FRAC_LONG_DESC = "Fraction of minumal markers required, at least 5% by default.";

	private final static String OPT_TOP = "top";
	private final static String OPT_TOP_DESC = "Top x files as the reference.";

	private final static String OPT_RAPID = "rapid";
	private final static String OPT_RAPID_DESC = "Rapid method to sample snps.";

	private final static String OPT_RCM = "rcm";
	private final static String OPT_RCM_DESC = "Random model to estimate correlation.";

	private final static String OPT_TRIM = "trim";
	private final static String OPT_TRIM_DEFAULT = "0.001";
	private final static String OPT_TRIM_DESC = "trim off the top and the bottom 5% of markers in calculating lambda_meta and its derived statistics.";
}
