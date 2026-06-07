package com.example.begger.settings;

import com.example.begger.system.Module;

public abstract class Setting {
    private String name;
    private Module parent;

    public Setting(String name, Module parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return true;
    }
}
