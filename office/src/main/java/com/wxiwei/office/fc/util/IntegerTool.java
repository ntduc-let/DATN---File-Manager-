package com.wxiwei.office.fc.util;

public class IntegerTool {
    public static int parseIntWithoutException(String input) {
        int result = 0;
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return result;
        }
    }

    public static int parseIntWithoutException(String input, int radix) {
        int result = 0;
        try {
            return Integer.parseInt(input, radix);
        } catch (Exception e) {
            return result;
        }
    }
}
