package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.mpii.saarland.germany.experiment.Conductor;
import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rulemining.patternmining.PatternForm1Miner;
import com.mpii.saarland.germany.utils.TextFileReader;

public class MainCLI {

	public static void main(String[] args) throws ParseException {
//		String arg = " -e=pos -l=data/experiment/IMDB/training.data.txt ";
//		String arg = " -e=neg -p=data/experiment/IMDB/patterns.txt -t=data/experiment/IMDB/training.data.txt -r=0";
//		String arg = " -e=exp -f=data/experiment/IMDB -r=0";

		for (int i = 0; i < args.length; ++i) {
			System.out.println("i = '" + args[i] + "'");
		}

		Option helpOption = Option.builder("h").longOpt("help").required(false)
				.desc("command line interface description.").build();

		Option exeOption = Option.builder("e").longOpt("exe").numberOfArgs(1).required(false).type(String.class)
				.desc("this requires class for execution, i.e., pos, neg, exp for positve rule mining, nonmonotonic rule mining, experiment, resp.")
				.build();

		Option posOption = Option.builder("p").longOpt("pos").numberOfArgs(1).required(false).type(String.class)
				.desc("this requires a tab-seperated file for positive rules.").build();

		Option trainOption = Option.builder("l").longOpt("learn").numberOfArgs(1).required(false).type(String.class)
				.desc("this requires a learning graph file path in SPO format.").build();

		Option idealOption = Option.builder("f").longOpt("folder").numberOfArgs(1).required(false).type(String.class)
				.desc("this requires an experiment folder.").build();

		Option rankOption = Option.builder("r").longOpt("rank").numberOfArgs(1).required(false).type(Number.class)
				.desc("this requires a ranking type, i.e., 0, 1, 2 for naive, pm, opm ranking, resp.").build();

		Option sampleOption = Option.builder("s").longOpt("sample").required(false)
				.desc("this flag is for sampling positive rules.").build();

		Option dlvOption = Option.builder("d").longOpt("dlv").required(false)
				.desc("this flag is to enable DLV in order to extend knowledge graph.").build();

		Option topOption = Option.builder("t").longOpt("top").numberOfArgs(1).required(false).type(Number.class)
				.desc("this requires number of positive rules with top absolute support.").build();

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(exeOption);
		options.addOption(posOption);
		options.addOption(trainOption);
		options.addOption(idealOption);
		options.addOption(rankOption);
		options.addOption(sampleOption);
		options.addOption(topOption);
		options.addOption(dlvOption);

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(options, args);

		if (args.length == 0 || commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RUMIS", options);
			return;
		}

		if (!commandLine.hasOption("exe")) {
			System.out.println("Arguments should have exe option.");
			return;
		}
		String execution = commandLine.getOptionValue("exe");
		if (execution.equals("pos")) {
			String trainFileName = commandLine.getOptionValue("learn");
			PatternForm1Miner.minePatterns(trainFileName);
			return;
		}
		if (execution.equals("neg")) {
			String patternFileName = commandLine.getOptionValue("pos");
			String trainFileName = commandLine.getOptionValue("learn");
			int rank = Integer.parseInt(commandLine.getOptionValue("rank"));
			FactIndexer facts = new FactIndexer(trainFileName);
			int topRuleCount = TextFileReader.readLines(patternFileName).size();
			if (commandLine.hasOption("top")) {
				topRuleCount = Integer.parseInt(commandLine.getOptionValue("top"));
			}
			ExceptionRanker ranker = new ExceptionRanker(patternFileName, null, facts, topRuleCount);
			ranker.rankRulesWithExceptions(RankingType.values()[rank]);
			return;
		}
		if (execution.equals("exp")) {
			File workingFolder = new File(commandLine.getOptionValue("folder"));
			if (!workingFolder.exists()) {
				System.out.println("Working folder does not exist.");
				return;
			}
			int rank = 2;
			try {
				rank = Integer.parseInt(commandLine.getOptionValue("rank"));
			} catch (NumberFormatException ex) {
				System.out.println("Parameter 2 should be from 0 to 2.");
				return;
			}
			if (rank < 0 || rank > 2) {
				System.out.println("Parameter 2 should be from 0 to 2.");
				return;
			}
			String workingPath = workingFolder.getAbsolutePath();
			File dlvFolder = new File(workingPath + "/DLV");
			if (!dlvFolder.exists()) {
				dlvFolder.mkdir();
			}
			String dlvPath = dlvFolder.getAbsolutePath();
			String rankName = RankingType.values()[rank].toString().toLowerCase();

			Conductor.idealDataFileName = workingPath + "/ideal.data.txt";
			Conductor.encodeFileName = workingPath + "/encode.txt";
			Conductor.patternFileName = workingPath + "/patterns.txt";
			Conductor.selectedPatternFileName = workingPath + "/selected.patterns.txt";
			Conductor.trainingDataFileName = workingPath + "/training.data.txt";
			Conductor.trainingDataDlvFileName = dlvPath + "/training.data.kg";
			Conductor.choosenRuleFileName = dlvPath + "/chosen.rules." + rankName + ".txt";
			Conductor.dlvBinaryFileName = workingPath + "/dlv.bin";
			Conductor.extensionPrefixFileName = dlvPath + "/extension." + rankName + ".kg";

			Conductor.topRuleCount = TextFileReader.readLines(Conductor.patternFileName).size();
			if (commandLine.hasOption("top")) {
				Conductor.topRuleCount = Integer.parseInt(commandLine.getOptionValue("top"));
			}
			Conductor.withDlv = 0;
			if (commandLine.hasOption("dlv")) {
				Conductor.withDlv = 1;
			}

			Conductor.execute(RankingType.values()[rank]);
			return;
		}
	}
}
