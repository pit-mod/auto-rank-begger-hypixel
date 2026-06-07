package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class NumberSetting extends Setting {
    private double value, min, max;
    private int decimalPlaces;

    public NumberSetting(String name, Module parent, double value, double min, double max, int decimalPlaces) {
        super(name, parent);
        this.value = value;
        this.min = min;
        this.max = max;
        this.decimalPlaces = decimalPlaces;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getMinValue() {
        return min;
    }

    public double getMaxValue() {
        return max;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }
}
