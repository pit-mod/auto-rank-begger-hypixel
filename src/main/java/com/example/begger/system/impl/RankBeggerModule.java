package com.example.begger.system.impl;

import com.example.begger.RankBegger;
import com.example.begger.settings.impl.BooleanSetting;
import com.example.begger.settings.impl.NumberSetting;
import com.example.begger.system.Category;
import com.example.begger.system.Module;

public class RankBeggerModule extends Module {
    public BooleanSetting smartMode;
    public BooleanSetting simulateTyping;
    public BooleanSetting humanizeText;
    public NumberSetting delay;
    public com.example.begger.settings.impl.BooleanSetting failsafe;
    public com.example.begger.settings.impl.BooleanSetting showHud;
    public NumberSetting hudX;
    public NumberSetting hudY;
    public com.example.begger.settings.impl.InputSetting webhookUrl;
    public com.example.begger.settings.impl.SimpleModeSetting hudDesign;

    public RankBeggerModule() {
        super("Rank Begger", Category.BEGGER);
        this.toggled = true;

        smartMode = new BooleanSetting("Smart Mode", this, true);
        simulateTyping = new BooleanSetting("Simulate Typing", this, true);
        humanizeText = new BooleanSetting("Humanize Text", this, true);
        delay = new NumberSetting("Delay (s)", this, 15, 1, 300, 0);
        failsafe = new com.example.begger.settings.impl.BooleanSetting("Failsafe", this, true);
        showHud = new com.example.begger.settings.impl.BooleanSetting("Show HUD", this, true);
        hudX = new NumberSetting("HUD X", this, 10, 0, 2000, 0);
        hudY = new NumberSetting("HUD Y", this, 10, 0, 2000, 0);
        hudDesign = new com.example.begger.settings.impl.SimpleModeSetting("HUD Design", this, new String[]{"Nebula", "Taunahi", "Polar", "Polinex", "Wielix"}, "Nebula");
        webhookUrl = new com.example.begger.settings.impl.InputSetting("Webhook URL", this, "");

        RankBegger.settingsManager.addSetting(smartMode, this);
        RankBegger.settingsManager.addSetting(simulateTyping, this);
        RankBegger.settingsManager.addSetting(humanizeText, this);
        RankBegger.settingsManager.addSetting(delay, this);
        RankBegger.settingsManager.addSetting(failsafe, this);
        RankBegger.settingsManager.addSetting(showHud, this);
        RankBegger.settingsManager.addSetting(hudX, this);
        RankBegger.settingsManager.addSetting(hudY, this);
        RankBegger.settingsManager.addSetting(hudDesign, this);
        RankBegger.settingsManager.addSetting(webhookUrl, this);
    }
}
