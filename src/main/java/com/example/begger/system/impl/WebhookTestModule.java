package com.example.begger.system.impl;

import com.example.begger.RankBegger;
import com.example.begger.system.Category;
import com.example.begger.system.Module;
import com.example.begger.utils.WebhookUtil;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;

public class WebhookTestModule extends Module {

    public WebhookTestModule() {
        super("Test Webhook", Category.MISC);
    }

    @Override
    public void onEnable() {
        RankBeggerModule mainModule = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
        if (mainModule == null) {
            this.toggle();
            return;
        }

        String url = WebhookUtil.normalizeWebhookUrl(mainModule.webhookUrl.getContent());
        if (!WebhookUtil.isConfigured(url)) {
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(EnumChatFormatting.RED + "[Begger] Webhook URL is empty!"));
            }
            this.toggle();
            return;
        }

        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(EnumChatFormatting.YELLOW + "[Begger] Sending test webhook..."));
        }

        File screenshot = WebhookUtil.captureScreenshot();

        new Thread(() -> {
            try {
                String content = "**[TEST] Rank Begger - Gift Received!**\n" +
                        "Time taken: `0h 5m 23s` (Fake)\n" +
                        "Messages sent: `42` (Fake)";
                WebhookUtil.sendWebhook(url, content, screenshot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        this.toggled = false;
        this.onDisable();
    }
}
