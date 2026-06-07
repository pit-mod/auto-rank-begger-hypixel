package com.example.begger.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import com.example.begger.utils.RenderUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Snow {
    private final List<Snowflake> snowflakes = new ArrayList<>();

    public Snow(int amount) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        for (int i = 0; i < amount; i++) {
            float x = (float) (Math.random() * sr.getScaledWidth());
            float y = (float) (Math.random() * sr.getScaledHeight());
            float speed = (float) (Math.random() * 2f + 0.5f);
            float size = (float) (Math.random() * 2.5f + 1f);
            snowflakes.add(new Snowflake(x, y, speed, size));
        }
    }

    public void drawSnow(int mouseX, int mouseY) {
        for (Snowflake snowflake : snowflakes) {
            snowflake.update(mouseX, mouseY);
            RenderUtil.drawCircle(snowflake.getX(), snowflake.getY(), snowflake.getSize(),
                    new Color(255, 255, 255, 180).getRGB());
        }
    }
}