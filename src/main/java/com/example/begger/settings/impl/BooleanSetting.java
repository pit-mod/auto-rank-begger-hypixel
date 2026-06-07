package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class BooleanSetting extends Setting {
    private boolean enabled;

    public BooleanSetting(String name, Module parent, boolean enabled) {
        super(name, parent);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
