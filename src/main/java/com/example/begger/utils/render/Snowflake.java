package com.example.begger.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Snowflake {
    private float x;
    private float y;
    private float speed;
    private float size;

    public Snowflake(float x, float y, float speed, float size) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
    }

    public void update(int mouseX, int mouseY) {
        y += speed;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        float centerX = sr.getScaledWidth() / 2f;

        float distanceFormCenter = mouseX - centerX;
        x += (distanceFormCenter / centerX) * (speed * 0.5f);

        if (y > sr.getScaledHeight()) {
            y = 0;
            x = (float) (Math.random() * sr.getScaledWidth());
            speed = (float) (Math.random() * 2f + 0.5f);
            size = (float) (Math.random() * 2.5f + 1f);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }
}