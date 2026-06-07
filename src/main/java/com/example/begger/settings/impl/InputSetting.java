package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class InputSetting extends Setting {
    private String content;
    public InputSetting(String name, Module parent, String content) { super(name, parent); this.content = content; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
