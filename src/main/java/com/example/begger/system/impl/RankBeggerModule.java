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
    public com.example.begger.settings.impl.InputSetting customMessage;
    public com.example.begger.settings.impl.ButtonSetting addMsgBtn;
    public com.example.begger.settings.impl.ButtonSetting clearMsgsBtn;

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

        customMessage = new com.example.begger.settings.impl.InputSetting("Message", this, "");
        addMsgBtn = new com.example.begger.settings.impl.ButtonSetting("Add Message", this, () -> {
            String msg = customMessage.getContent();
            if (!msg.trim().isEmpty()) {
                com.example.begger.Config.messages.add(msg);
                com.example.begger.Config.save();
                customMessage.setContent("");
                if (net.minecraft.client.Minecraft.getMinecraft().thePlayer != null) {
                    net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new net.minecraft.util.ChatComponentText(net.minecraft.util.EnumChatFormatting.GREEN + "Added message: " + msg));
                }
            }
        });
        clearMsgsBtn = new com.example.begger.settings.impl.ButtonSetting("Clear All", this, () -> {
            com.example.begger.Config.messages.clear();
            com.example.begger.Config.save();
            if (net.minecraft.client.Minecraft.getMinecraft().thePlayer != null) {
                net.minecraft.client.Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText(net.minecraft.util.EnumChatFormatting.RED + "Cleared all messages!"));
            }
        });

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
        RankBegger.settingsManager.addSetting(customMessage, this);
        RankBegger.settingsManager.addSetting(addMsgBtn, this);
        RankBegger.settingsManager.addSetting(clearMsgsBtn, this);
    }
}
