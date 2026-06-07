package com.example.begger.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        } else {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

}