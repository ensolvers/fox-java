package com.ensolvers.fox.location;

public class NetworkUtils {
    private NetworkUtils() {
    }

    private static final String IPV4_PATTERN = "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)){3}";
    private static final String HEX_PATTERN = "[A-Fa-f\\d]{1,4}";
    private static final String IPV6_PATTERN = 
    "^(?:" +
    "(?:" + HEX_PATTERN + ":){7}(?:" + HEX_PATTERN + "|:)|" + // 1:2:3:4:5:6:7::  1:2:3:4:5:6:7:8
    "(?:" + HEX_PATTERN + ":){6}(?:" + IPV4_PATTERN + "|:" + HEX_PATTERN + "|:)|" + // 1:2:3:4:5:6::    1:2:3:4:5:6::8   1:2:3:4:5:6::8  1:2:3:4:5:6::1.2.3.4
    "(?:" + HEX_PATTERN + ":){5}(?::" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,2}|:)|" + // 1:2:3:4::        1:2:3:4::6:7:8   1:2:3:4::8      1:2:3:4::6:7:1.2.3.4
    "(?:" + HEX_PATTERN + ":){4}(?:(?::" + HEX_PATTERN + "){0,1}:" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,3}|:)|" + // 1:2:3:4::        1:2:3:4::6:7:8   1:2:3:4::8      1:2:3:4::6:7:1.2.3.4
    "(?:" + HEX_PATTERN + ":){3}(?:(?::" + HEX_PATTERN + "){0,2}:" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,4}|:)|" + // 1:2:3::          1:2:3::5:6:7:8   1:2:3::8        1:2:3::5:6:7:1.2.3.4
    "(?:" + HEX_PATTERN + ":){2}(?:(?::" + HEX_PATTERN + "){0,3}:" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,5}|:)|" + // 1:2::            1:2::4:5:6:7:8   1:2::8          1:2::4:5:6:7:1.2.3.4
    "(?:" + HEX_PATTERN + ":){1}(?:(?::" + HEX_PATTERN + "){0,4}:" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,6}|:)|" + // 1::              1::3:4:5:6:7:8   1::8            1::3:4:5:6:7:1.2.3.4
    "(?::(?:(?::" + HEX_PATTERN + "){0,5}:" + IPV4_PATTERN + "|(?::" + HEX_PATTERN + "){1,7}|:)))"; // ::2:3:4:5:6:7:8  ::2:3:4:5:6:7:8  ::8             ::1.2.3.4

    
    /**
     * Check if string is a valid IPv4 address
     * @param ip a ip address in string form
     * @return true if a ip is a valid IPv4 address, false otherwise
     */
    public static boolean isValidIPv4Address(String ip) {
        return (ip.matches(IPV4_PATTERN));
    }

    /**
     * <p>Check if string is a valid IPv6 address</p>
     * 
     * <p>While faster, this one may not check if IPv4 addresses are embedded on IPv6 ones</p>
     * 
     * <p>ie: 2001:db8:122:344::192.0.2.33</p>  
     * 
     * @param ip a ip address in string form
     * @return true if a ip is a valid IPv6 address, false otherwise
     */

    public static boolean isValidIPv6Address(String ip) {
        return isValidIPv6Addressv1(ip);
    }
    
    /**
     * <p>Check if string is a valid IPv6 address</p>
     * 
     * <p>Slower, but will check if IPv4 addresses are embedded in IPv6 ones</p>   
     * 
     * <p>ie: 2001:db8:122:344::192.0.2.33</p>  
     * 
     * @param ip a ip address in string form
     * @return true if a ip is a valid IPv6 address, false otherwise
     * @see {@link https://www.juniper.net/documentation/us/en/software/junos/interfaces-next-gen-services/topics/concept/ipv4-address-embedded-ipv6.html}
     */
    public static boolean isValidIPv6Addressv2(String ip) {
        return ip.matches(IPV6_PATTERN);
    }

    private static Boolean isValidIPv6Addressv1(String ip) {
        int simplification = 0;
        int partCounter = 0;
        StringBuilder hexadecimalValue = new StringBuilder();

        // Check if the first and last characters are ::, otherwise the ip is invalid
        if ((ip.charAt(0) == ':' && ip.charAt(1) != ':') || (ip.charAt(ip.length() - 1) == ':' && ip.charAt(ip.length() - 2) != ':')) {
            return false;
        }

        // Check that every hexadecimal value of the ip is valid, when reaching a ':'
        // will check if the value is a valid one.
        // If valid, it will also check if the following value is another ':'
        for (int i = 0; i < ip.length(); i++) {
            if (ip.charAt(i) == ':') {
                // check ip ip part is valid
                if (hexadecimalValue.length() > 0) {
                    if (Boolean.FALSE.equals(checkIfHexadecimalValueIsValid(hexadecimalValue.toString()))) {
                        return false;
                    } else {
                        partCounter = partCounter + 1;
                        hexadecimalValue.setLength(0);
                    }
                }
                if (ip.length() > (i + 1) && (ip.charAt(i + 1) == ':')) {
                    simplification = simplification + 1;
                }
            } else {
                hexadecimalValue.append(ip.charAt(i));
            }
            // If there are more than one simplification (::) the ip address is not valid
            if (simplification > 1) {
                return false;
            }
        } // end of for cycle

        // Check last value
        if (Boolean.FALSE.equals(checkIfHexadecimalValueIsValid(hexadecimalValue.toString()))) {
            return false;
        } else {
            partCounter = partCounter + 1;
        }
        // PartCounter should be 8 without simplification
        // Or less than 8 with 1 simplification
        return ((simplification == 1 && partCounter < 8) || (simplification == 0 && partCounter == 8));
    }

    private static Boolean checkIfHexadecimalValueIsValid(String hexadecimal) {
        return hexadecimal.matches(HEX_PATTERN);
    }
}
