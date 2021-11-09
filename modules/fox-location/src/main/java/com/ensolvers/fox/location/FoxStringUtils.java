package com.ensolvers.fox.location;

public class FoxStringUtils {

	public static String concat(String... strings) {
		if (strings == null) {
			return "";
		}

		if (strings.length == 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		for (String s : strings) {
			if (s != null) {
				builder.append(s);
			}
		}

		return strings.toString();
	}
}
