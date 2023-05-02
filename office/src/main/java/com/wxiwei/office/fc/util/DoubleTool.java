package com.wxiwei.office.fc.util;

public class DoubleTool {
    public static double parseDoubleWithoutException(String input) {
        int result = 0;
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return result;
        }
    }
}
