package com.mapbox.mapboxsdk.utils;

public class MathUtils {

    /**
     * Test a value in specified range, returning minimum if it's below, and maximum if it's above
     *
     * @param value Value to test
     * @param min   Minimum value of range
     * @param max   Maximum value of range
     * @return value if it's between min and max, min if it's below, max if it's above
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Test a value in specified range, returning minimum if it's below, and maximum if it's above
     *
     * @param value Value to test
     * @param min   Minimum value of range
     * @param max   Maximum value of range
     * @return value if it's between min and max, min if it's below, max if it's above
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Constrains value to the given range (including min, excluding max) via modular arithmetic.
     * <p>
     * Same formula as used in Core GL (wrap.hpp)
     * std::fmod((std::fmod((value - min), d) + d), d) + min;
     *
     * @param value Value to wrap
     * @param min   Minimum value
     * @param max   Maximum value
     * @return Wrapped value
     */
    public static double wrap(double value, double min, double max) {
        double delta = max - min;

        double firstMod = (value - min) % delta;
        double secondMod = (firstMod + delta) % delta;

        return secondMod + min;
    }

    /**
     * Round a value to a specific amount.
     * <p>
     * Is used to round latitude and longitude values
     * </p>
     * <p>
     * This method is should onlybe used with small double values, overflow is possible.
     * </p>
     *
     * @param value  Value to round
     * @param amount Amount of decimals to round to
     * @return Rounded value
     */
    public static double round(double value, int amount) {
        double amountMultiplier = Math.pow(10, amount);
        value = value * amountMultiplier;
        value = Math.round(value);
        value = value / amountMultiplier;
        return value;
    }
}
