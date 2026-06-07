package com.example.begger.settings.impl;

import com.example.begger.settings.Setting;
import com.example.begger.system.Module;

public class BindSetting extends Setting {
    private int keyCode;
    private boolean listening;
    public BindSetting(String name, Module parent, int keyCode) { super(name, parent); this.keyCode = keyCode; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    public boolean isListening() { return listening; }
    public void setListening(boolean listening) { this.listening = listening; }
    public String getKeyName() { return org.lwjgl.input.Keyboard.getKeyName(keyCode); }
}
