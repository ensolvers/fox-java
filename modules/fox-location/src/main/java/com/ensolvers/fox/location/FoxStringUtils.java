package com.ensolvers.fox.location;

import java.util.Arrays;

public class FoxStringUtils {

	private FoxStringUtils() {}

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

		return Arrays.toString(strings);
	}
}
