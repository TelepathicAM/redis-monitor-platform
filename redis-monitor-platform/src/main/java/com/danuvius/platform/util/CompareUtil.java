package com.danuvius.platform.util;

public class CompareUtil {
    /**
     * 通用比较：支持 >, <, >=, <=, =
     */
    public static boolean compareValue(double value, String compareType, double threshold) {
        if (compareType == null) {
            return false;
        }
        switch (compareType) {
            case ">":
                return value > threshold;
            case "<":
                return value < threshold;
            case ">=":
                return value >= threshold;
            case "<=":
                return value <= threshold;
            case "=":
                return Math.abs(value - threshold) < 1e-6;
            default:
                return false;
        }
    }
}
