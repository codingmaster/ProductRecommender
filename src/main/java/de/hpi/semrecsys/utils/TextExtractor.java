package de.hpi.semrecsys.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import opennlp.tools.util.InvalidFormatException;
import de.abelssoft.wordtools.jwordsplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.hpi.semrecsys.config.SemRecSysConfiguratorData.LanguageCode;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.Product;

public class TextExtractor {

	LanguageCode languageCode;
	private static final String STRING_SEPARATOR = " ";
	private final AbstractWordSplitter splitter;

	public TextExtractor(LanguageCode languageCode) throws InvalidFormatException, FileNotFoundException, IOException {
		this.languageCode = languageCode;
		splitter = getSplitter();
	}

	public String getSplittedText(String text) {
		if (text.isEmpty()) {
			return text;
		}

		StringBuilder builder = new StringBuilder();
		String[] splitted = text.split(STRING_SEPARATOR);
		StringBuilder splittedBuilder = new StringBuilder();
		for (String string : splitted) {
			string = string.replace(",", "").replace("\\.", "").replace("-", "").trim();
			String splittedString = getSplittedString(string);

			if (!splittedString.isEmpty()) {
				splittedBuilder.append(splittedString + STRING_SEPARATOR);
			}
			if (!string.isEmpty()) {
				builder.append(string + STRING_SEPARATOR);
			}
		}
		return builder.append(splittedBuilder).toString();
	}

	private String getSplittedString(String string) {
		StringBuilder builder = new StringBuilder();
		Collection<String> parts = splitter.splitWord(string);
		if (parts.size() > 1) {
			for (String part : parts) {
				builder.append(part + STRING_SEPARATOR);
			}

			// System.out.println("Word " + string + " was splitted to " +
			// parts);
		}
		return builder.toString();
	}

	private AbstractWordSplitter getSplitter() {
		AbstractWordSplitter splitter = null;
		try {
			splitter = new GermanWordSplitter(true);
			splitter.setStrictMode(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return splitter;
	}

	public String getProductAsString(Product product) {
		StringBuilder builder = new StringBuilder();
		for (List<Attribute> attributeList : product.getAttributes().values()) {
			for (Attribute attribute : attributeList) {
				String value = attribute.getValue();
				String text = String.valueOf(value);

				// System.out.println(attribute + " : " + text);
				builder.append(text).append(" ");
			}

		}

		return builder.toString();
	}

}
