package com.ensolvers.fox.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {

	private static Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

	public static boolean isValidIPv4Address(String ip) {
		return (ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"));
	}

	public static Boolean isValidIPv6Address(String ip) {
		int simplification = 0;
		String hexadecimalValue = "";
		int partCounter = 0;
		// Check if the first and last characters are valid
		if (ip.charAt(0) == ':' && ip.charAt(1) != ':') {
			return false;
		} else {
			if (ip.charAt(ip.length() - 1) == ':' && ip.charAt(ip.length() - 2) != ':') {
				return false;
			}
		}

		// Check that every hexadecimal value of the ip is valid, when reaching a ':'
		// will check if the
		// value is a valid one, if valid, it will also check if the following value is
		// another ':'
		for (int i = 0; i < ip.length(); i++) {
			if (ip.charAt(i) == ':') {
				try {
					if (!checkIfHexadecimalValueIsValid(hexadecimalValue)) {
						return false;
					} else {
						partCounter = partCounter + 1;
					}
				} catch (Exception e) {
					return false;
				}
				if (!(ip.length() == (i + 1))) {
					if (ip.charAt(i + 1) == ':') {
						simplification = simplification + 1;
						// If there are more that one simplification (::) the ip address is not valid
						if (simplification > 1) {
							return false;
						}
					} else {
						hexadecimalValue = "";
					}
				}
			} else {
				hexadecimalValue = hexadecimalValue + String.valueOf(ip.charAt(i));
			}
		}
		// Check last value
		if (!checkIfHexadecimalValueIsValid(hexadecimalValue)) {
			return false;
		} else {
			partCounter = partCounter + 1;
		}

		// Invalidad case with no :: and more or less than 8 partes
		if ((simplification == 0) && ((partCounter != 8))) {
			return false;
		}
		if (partCounter > 8) {
			return false;
		}
		return true;
	}

	private static Boolean checkIfHexadecimalValueIsValid(String hexadecimal) {
		try {
			if (hexadecimal.length() > 4) {
				return false;
			}
			Long.valueOf(hexadecimal, 16);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
