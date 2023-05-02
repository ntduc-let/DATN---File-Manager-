package com.wxiwei.office.fc.util;

public class FloatTool {
    public static float parseFloatWithoutException(String input) {
        int result = 0;
        try {
            return Float.parseFloat(input);
        } catch (Exception e) {
            return result;
        }
    }
}
