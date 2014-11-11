package de.hpi.semrecsys.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Namespacer {

	private final List<Descriptor> descriptors = new ArrayList<Descriptor>();

	public static Namespacer createDefaultNamespacer() {
		Namespacer instance = new Namespacer();
		instance.add("http://dbpedia.org/resource/", "");
		return instance;
	}

	public void add(String uri, String abbrevation) {
		Descriptor descriptor = new Descriptor();
		descriptor.uri = uri;
		descriptor.pattern = Pattern.compile(Pattern.quote(uri) + "(.*)");
		descriptor.abbrevation = abbrevation + ":";
		descriptors.add(descriptor);
	}

	public String process(String resource) {
		resource = resource.replace("<", "").replace(">", "");
		for (Descriptor descriptor : descriptors) {
			Matcher matcher = descriptor.pattern.matcher(resource);
			if (matcher.matches()) {
				return descriptor.abbrevation + matcher.group(1);
			} else if (resource.startsWith(descriptor.abbrevation)) {
				break;
			}
		}
		return resource;
	}

	public String restore(String resource) {
		for (Descriptor descriptor : descriptors) {
			String abbrevation = descriptor.abbrevation;
			Pattern pattern = Pattern.compile(Pattern.quote(abbrevation) + "(.*)");
			Matcher matcher = pattern.matcher(resource);
			if (matcher.matches()) {
				String replacement = descriptor.uri;
				String result = replacement + normalizeUri(matcher.group(1));
				return result;
			}
		}
		return resource;
	}

	private String normalizeUri(String uri) {
		StringBuilder builder = new StringBuilder();
		int idx = 0;
		for (String part : uri.split(" ")) {
			if (idx > 0) {
				builder.append("_");
			}
			builder.append(capitalizeFirstLetter(part));
			idx++;
		}
		;
		return builder.toString().replace("\"", "");
	}

	private String capitalizeFirstLetter(String original) {
		if (original.length() == 0)
			return original;
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	public String getIdentifier(String resource) {
		for (Descriptor descriptor : descriptors) {
			Matcher matcher = descriptor.pattern.matcher(resource);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		throw new RuntimeException("Unknown namespace: \"" + resource + "\"");
	}

	private static class Descriptor {
		String uri;
		Pattern pattern;
		String abbrevation;

		@Override
		public String toString() {
			return "Descriptor[" + pattern + "->" + abbrevation + "]";
		}
	}

}
