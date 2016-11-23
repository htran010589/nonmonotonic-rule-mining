package com.mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mpii.saarland.germany.utils.TextFileReader;

public class KgCombiner {

	public static void main1(String[] args) throws Exception {
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/yago.ideal.data.txt"));
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/yagoFacts.tsv"));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length()  - 1);
			String[] parts = line.split(">\t<");
			if (parts.length != 4) {
				continue;
			}
			wr.write("<" + parts[1] + ">\t<" + parts[2] + ">\t<" + parts[3] + ">\n");
		}
		br.close();

		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/yagoSimpleTypes.tsv"));
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length() - 1);
			line = line.replace("rdf:type", "<type>");
			String[] parts = line.split(">\t<");
			if (parts.length != 3) {
				continue;
			}
			wr.write("<" + parts[0] + ">\t<" + parts[1] + ">\t<" + parts[2] + ">\n");
		}
		br.close();
		wr.close();
	}

	public static void main2(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/YAGO_large/ideal.data.txt"));
		Set<String> ss = new HashSet<>();
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length()  - 1);
			String[] parts = line.split(">\t<");
			if (parts[1].equals("type")) continue;
			ss.add(parts[0]);
			ss.add(parts[2]);
		}
		br.close();
		System.out.println(ss.size());
	}

	public static void generateTable1() throws Exception {
		int[] tops = {5, 30, 50, 60, 70, 80, 100};
		String[] domains = {"yago", "imdb"};
		for (int i = 0; i < tops.length; ++i) {
			int top = tops[i];
			List<Double> avgConvictions = new ArrayList<>(); 
			for (int j = 0; j < domains.length; ++j) {
				String domain = domains[j];
				for (int k = 0; k < 3; ++k) {
					String fileName = "/home/htran/Research_Work/Code/nonmonotonic-rule-mining/" + domain + "." + k + "." + top + "." + 0;
//					System.out.println(fileName);
					List<Double> nums = new ArrayList<>();
					List<String> lines = TextFileReader.readLines(fileName);
					for (String line : lines) {
						if (!line.startsWith("Average conviction = ")) continue;
						line = line.substring("Average conviction = ".length());
						nums.add(Double.parseDouble(line));
					}
					if (k == 0) {
						avgConvictions.add(nums.get(0));
					}
					avgConvictions.add(nums.get(1));
				}
			}
			System.out.print("\\multicolumn{1}{|l|}{" + top + "}  ");
			for (int j = 0; j < avgConvictions.size(); ++j) {
				System.out.printf(" & %.4f", avgConvictions.get(j));
			}
			System.out.println(" \\\\ \\hline");
		}
	}

	public static void generateTable2(String domain) throws Exception {
		int nRow = 0;
		for (int i = 0; i < 3; ++i) {
			String fileName = "/home/htran/Research_Work/Code/nonmonotonic-rule-mining/selected." + domain + "." + i + ".100.1";
			List<String> lines = TextFileReader.readLines(fileName);
			List<String> newLines = new ArrayList<>();
			for (String line : lines) {
				if (!line.startsWith("Predicted facts: ")) continue;
				line = line.substring("Predicted facts: ".length());
				newLines.add(line);
			}
			nRow = newLines.size() / 2;
		}
//		System.out.println(nRow);
		String[][] cells = new String[nRow][13];
		for (int i = 0; i < 3; ++i) {
			String fileName = "/home/htran/Research_Work/Code/nonmonotonic-rule-mining/selected." + domain + "." + i + ".100.1";
			List<String> lines = TextFileReader.readLines(fileName);
			List<String> newLines = new ArrayList<>();
			for (String line : lines) {
				if (!line.startsWith("Predicted facts: ")) continue;
				line = line.substring("Predicted facts: ".length());
				newLines.add(line);
			}
			for (int j = 0; j < newLines.size(); ++j) {
				String line = newLines.get(j);
				String[] parts = line.split("\t");
				if (j < nRow) {
					cells[j][0] = parts[0];
					for (int k = 0; k < 3; ++k) {
						cells[j][k * 4 + 1] = parts[k + 1];
					}
				} else {
					for (int k = 0; k < 3; ++k) {
						cells[j - nRow][k * 4 + 2 + i] = parts[k + 1];
					}
				}
			}
		}
		for (int i = 0; i < nRow; ++i) {
			if (i == 0) {
				System.out.print("\\multirow{" + nRow + "}{*}{\\rotatebox{90}{" + domain.toUpperCase() + "}} & ");
			} else {
				System.out.print(" & ");
			}
			for (int j = 0; j < 13; ++j) {
				System.out.print(cells[i][j]);
				if (j  + 1 < 13) {
					System.out.print(" & ");
				} else {
					if (i + 1 < nRow) {
						System.out.print(" \\\\ \\cline{2-14}");
					} else {
						System.out.print(" \\\\ \\cline{1-14}");
					}
				}
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws Exception {
		generateTable2(args[0]);
	}

}
