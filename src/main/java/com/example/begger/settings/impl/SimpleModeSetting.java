package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class SimpleModeSetting extends Setting {
    private String[] options;
    private int index;
    public SimpleModeSetting(String name, Module parent, String[] options, String selected) {
        super(name, parent); this.options = options;
        for(int i=0; i<options.length; i++) if(options[i].equalsIgnoreCase(selected)) index = i;
    }
    public String getSelected() { return options[index]; }
    public void setSelected(String selected) { for(int i=0; i<options.length; i++) if(options[i].equalsIgnoreCase(selected)) index = i; }
    public String[] getOptions() { return options; }
}
