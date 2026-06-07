package com.example.begger.system.impl;

import com.example.begger.RankBegger;
import com.example.begger.settings.impl.BooleanSetting;
import com.example.begger.settings.impl.NumberSetting;
import com.example.begger.system.Category;
import com.example.begger.system.Module;

public class ClickGuiModule extends Module {
    public NumberSetting guiScale = new NumberSetting("Scale", this, 1.0, 0.5, 2.0, 1);
    public NumberSetting windowWidth = new NumberSetting("Width", this, 600, 400, 800, 0);
    public NumberSetting windowHeight = new NumberSetting("Height", this, 400, 300, 600, 0);
    public NumberSetting sidebarWidth = new NumberSetting("Sidebar Width", this, 140, 100, 200, 0);
    public NumberSetting cornerRadius = new NumberSetting("Corner Radius", this, 8, 0, 20, 0);
    public NumberSetting panelOpacity = new NumberSetting("Opacity", this, 200, 50, 255, 0);
    public BooleanSetting blur = new BooleanSetting("Blur", this, true);
    public NumberSetting blurStrength = new NumberSetting("Blur Strength", this, 5, 0, 20, 0);
    public BooleanSetting snow = new BooleanSetting("Snow", this, true);
    public BooleanSetting performanceMode = new BooleanSetting("Performance", this, false);
    public com.example.begger.settings.impl.SimpleModeSetting fontSetting = new com.example.begger.settings.impl.SimpleModeSetting("Font", this, new String[]{"Product Sans", "Poppins", "Montserrat", "Inter", "Consolas"}, "Product Sans");

    public ClickGuiModule() {
        super("ClickGUI", Category.RENDER);
        this.hidden = false;
        
        RankBegger.settingsManager.addSetting(guiScale, this);
        RankBegger.settingsManager.addSetting(windowWidth, this);
        RankBegger.settingsManager.addSetting(windowHeight, this);
        RankBegger.settingsManager.addSetting(sidebarWidth, this);
        RankBegger.settingsManager.addSetting(cornerRadius, this);
        RankBegger.settingsManager.addSetting(panelOpacity, this);
        RankBegger.settingsManager.addSetting(blur, this);
        RankBegger.settingsManager.addSetting(blurStrength, this);
        RankBegger.settingsManager.addSetting(snow, this);
        RankBegger.settingsManager.addSetting(performanceMode, this);
        RankBegger.settingsManager.addSetting(fontSetting, this);
    }
}
