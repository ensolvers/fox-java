package com.ensolvers.fox.location;

public class FoxStringUtils {

    private FoxStringUtils() {
    }

    /**
     * Concatenate Strings
     * 
     * @param strings a var-arg of strings
     * @return the strings concatenated
     */
    public static String concat(String... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (String s : strings) {
            if (s != null) {
                builder.append(s);
            }
        }

        return builder.toString();
    }
}
