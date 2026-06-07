package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;
import com.example.begger.ui.clickGui.ColorPicker;
import java.awt.Color;

public class ColorSetting extends Setting {
    private Color color;
    private ColorPicker picker;

    public ColorSetting(String name, Module parent, Color defaultValue) {
        super(name, parent);
        this.color = defaultValue;
        this.picker = new ColorPicker(50, defaultValue, this::setColor);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ColorPicker getPicker() {
        return picker;
    }
}
