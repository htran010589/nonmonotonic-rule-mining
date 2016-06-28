package com.mpii.saarland.germany.rulemining;

import java.util.List;
import com.mpii.saarland.germany.utils.Constants;
import com.mpii.saarland.germany.utils.TextFileReader;

public abstract class InstanceSetMiner {

	public void loadPositiveRules() {
		List<String> lines = TextFileReader.readLines(Constants.YAGO_FORM3_POSITIVE_RULE_FILE_NAME);
		for (String line : lines) {
			System.out.println(line);
		}
	}

}
