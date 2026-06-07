package com.example.begger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;

public class BeggerGui extends GuiScreen {
    private GuiTextField delayField;
    private GuiTextField messageField;
    private GuiButton toggleButton;
    private GuiButton smartButton;

    @Override
    public void initGui() {
        this.buttonList.clear();

        // Toggle Enabled
        String enabledText = "Begger: " + (Config.enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
        this.toggleButton = new GuiButton(0, this.width / 2 - 100, 60, 200, 20, enabledText);
        this.buttonList.add(this.toggleButton);

        // Smart Mode
        String smartText = "Smart Mode: " + (Config.smartMode ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
        this.smartButton = new GuiButton(1, this.width / 2 - 100, 90, 200, 20, smartText);
        this.buttonList.add(this.smartButton);

        // Delay Field
        this.delayField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 140, 200, 20);
        this.delayField.setText(String.valueOf(Config.delay));

        // Message Field
        this.messageField = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 100, 180, 150, 20);
        this.buttonList.add(new GuiButton(4, this.width / 2 + 55, 180, 45, 20, "Add"));

        // Close Button
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, 220, 200, 20, "Close"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            Config.enabled = !Config.enabled;
            Config.save();
            button.displayString = "Begger: " + (Config.enabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
        } else if (button.id == 1) {
            Config.smartMode = !Config.smartMode;
            Config.save();
            button.displayString = "Smart Mode: " + (Config.smartMode ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
        } else if (button.id == 4) {
            String newMsg = messageField.getText();
            if (!newMsg.isEmpty()) {
                Config.messages.add(newMsg);
                Config.save();
                messageField.setText("");
            }
        } else if (button.id == 5) {
            saveDelay();
            Config.save();
            this.mc.displayGuiScreen(null);
        }
    }

    private void saveDelay() {
        try {
            Config.delay = Integer.parseInt(delayField.getText());
        } catch (NumberFormatException ignored) {}
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.delayField.textboxKeyTyped(typedChar, keyCode);
        this.messageField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.delayField.mouseClicked(mouseX, mouseY, mouseButton);
        this.messageField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Rank Begger Settings", this.width / 2, 20, 16777215);
        
        this.drawString(this.fontRendererObj, "Delay (ms):", this.width / 2 - 100, 125, 10526880);
        this.delayField.drawTextBox();

        this.drawString(this.fontRendererObj, "Add Message:", this.width / 2 - 100, 165, 10526880);
        this.messageField.drawTextBox();

        String msgCount = "Total Messages: " + Config.messages.size();
        this.drawCenteredString(this.fontRendererObj, msgCount, this.width / 2, 205, 10526880);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
