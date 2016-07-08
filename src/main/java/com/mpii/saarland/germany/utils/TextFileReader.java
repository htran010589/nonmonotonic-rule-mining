package com.mpii.saarland.germany.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Hai Dang Tran
 *
 */
public class TextFileReader {

	private static final Logger LOG = LoggerFactory.getLogger(TextFileReader.class);

	public static List<String> readLines(String filePath) {
		List<String> lines = new ArrayList<String>();
		BufferedReader dataReader = null;
		try {
			dataReader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = dataReader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				dataReader.close();
			} catch (IOException ex) {
				LOG.error(ex.getMessage());
			}
		}
		return lines;
	}

}
