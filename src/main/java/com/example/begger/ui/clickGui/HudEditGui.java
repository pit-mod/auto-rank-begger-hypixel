package com.example.begger.ui.clickGui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import com.example.begger.RankBegger;
import com.example.begger.system.Module;
import com.example.begger.settings.Setting;
import com.example.begger.settings.impl.NumberSetting;
import com.example.begger.utils.interfaces.MM;
import com.example.begger.utils.RenderUtil;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HudEditGui extends GuiChat implements MM, com.example.begger.utils.interfaces.SM {

    private final List<Module> temporarilyEnabled = new ArrayList<>();

    public HudEditGui() {
        super("");
    }

    @Override
    public void initGui() {
        super.initGui();

        temporarilyEnabled.clear();
        for (Module m : mm.getModules()) {
            if (hasPositionSettings(m) && !m.toggled) {
                m.toggle();
                temporarilyEnabled.add(m);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        for (Module m : temporarilyEnabled) {
            m.toggle();
        }
        temporarilyEnabled.clear();

        com.example.begger.Config.save();

        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(0, 0, width, height, new Color(0, 0, 0, 100).getRGB());

        FontRenderer fr = mc.fontRendererObj;
        for (Module m : mm.getModules()) {
            if (!hasPositionSettings(m)) continue;

            float posX = 0, posY = 0;
            for (com.example.begger.settings.Setting s : sm.getValuesByMod(m)) {
                if (s.getName().equals("X Pos") || s.getName().equals("X") || s.getName().equals("xPos")) {
                    posX = (float) ((NumberSetting) s).getValue();
                }
                if (s.getName().equals("Y Pos") || s.getName().equals("Y") || s.getName().equals("yPos")) {
                    posY = (float) ((NumberSetting) s).getValue();
                }
            }

            float boxW = fr.getStringWidth(m.getName()) + 12;
            if (boxW < 60) boxW = 60;
            float boxH = 18;

            int bgColor = new Color(40, 40, 40, 140).getRGB();
            int borderColor = new Color(120, 120, 120, 180).getRGB();
            RenderUtil.drawRect(posX - 1, posY - 1, boxW + 2, boxH + 2, borderColor);
            RenderUtil.drawRect(posX, posY, boxW, boxH, bgColor);

            fr.drawStringWithShadow(m.getName(), posX + 4, posY + 5, 0xFFFFFF);
        }

        String text = "HUD Edit Mode  -  Drag elements to reposition  -  Press ESC to save & exit";
        int textWidth = fr.getStringWidth(text);
        fr.drawStringWithShadow(text, (width - textWidth) / 2f, height / 2f, 0xFFFFFF);

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }

    private boolean hasPositionSettings(Module m) {
        for (com.example.begger.settings.Setting s : sm.getValuesByMod(m)) {
            String name = s.getName();
            if (name.equals("X Pos") || name.equals("X") || name.equals("xPos")) {
                return true;
            }
        }
        return false;
    }
}