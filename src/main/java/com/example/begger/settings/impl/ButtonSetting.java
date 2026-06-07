package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class ButtonSetting extends Setting {
    private Runnable method;
    public ButtonSetting(String name, Module parent, Runnable method) { super(name, parent); this.method = method; }
    public void runMethod() { if(method != null) method.run(); }
}
