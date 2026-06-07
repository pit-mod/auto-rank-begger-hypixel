package com.example.begger.utils;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import com.example.begger.utils.interfaces.MC;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil implements MC {

    private static final double[] SIN_CACHE = new double[361];
    private static final double[] COS_CACHE = new double[361];

    static {
        for (int i = 0; i <= 360; i++) {
            SIN_CACHE[i] = Math.sin(i * Math.PI / 180.0);
            COS_CACHE[i] = Math.cos(i * Math.PI / 180.0);
        }
    }

    public static void drawCircle(double x, double y, double radius, int color) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        color(color);
        GL11.glBegin(9);

        for (int i = 0; i < 360; ++i) {
            GL11.glVertex2d(x + Math.sin(Math.toRadians(i)) * radius, y + Math.cos(Math.toRadians(i)) * radius);
        }

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void color(int argb) {
        float alpha = (float) (argb >> 24 & 0xFF) / 255.0F;
        float red = (float) (argb >> 16 & 0xFF) / 255.0F;
        float green = (float) (argb >> 8 & 0xFF) / 255.0F;
        float blue = (float) (argb & 0xFF) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void color(Color color) {
        color(color.getRGB());
    }

    public static void drawRoundedRect(float startX, float startY, float width, float height, float radius, int color) {
        if (radius > width / 2.0f) radius = width / 2.0f;
        if (radius > height / 2.0f) radius = height / 2.0f;

        float endX = startX + width;
        float endY = startY + height;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float z = 0.0F;
        if (startX > endX) {
            z = startX;
            startX = endX;
            endX = z;
        }

        if (startY > endY) {
            z = startY;
            startY = endY;
            endY = z;
        }

        double x1 = startX + radius;
        double y1 = startY + radius;
        double x2 = endX - radius;
        double y2 = endY - radius;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glLineWidth(1.0F);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(9);

        for (int i = 0; i <= 90; ++i) {
            GL11.glVertex2d(x2 + SIN_CACHE[i] * (double) radius, y2 + COS_CACHE[i] * (double) radius);
        }

        for (int i = 90; i <= 180; ++i) {
            GL11.glVertex2d(x2 + SIN_CACHE[i] * (double) radius, y1 + COS_CACHE[i] * (double) radius);
        }

        for (int i = 180; i <= 270; ++i) {
            GL11.glVertex2d(x1 + SIN_CACHE[i] * (double) radius, y1 + COS_CACHE[i] * (double) radius);
        }

        for (int i = 270; i <= 360; ++i) {
            GL11.glVertex2d(x1 + SIN_CACHE[i] * (double) radius, y2 + COS_CACHE[i] * (double) radius);
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glDisable(2848);
        GlStateManager.popMatrix();
    }

    public static void drawRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        Gui.drawRect((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void startScissorBox() {
        GL11.glPushMatrix();
        GL11.glEnable(3089);
    }

    public static void drawScissorBox(double x, double y, double width, double height) {
        drawScissorBox(x, y, width, height, 1.0);
    }

    public static void drawScissorBox(double x, double y, double width, double height, double customScale) {
        width = Math.max(width, 0.1);
        ScaledResolution sr = new ScaledResolution(mc);
        double totalScale = sr.getScaleFactor() * customScale;

        double screenHeight = (double) sr.getScaledHeight() * sr.getScaleFactor();

        double realX = x * totalScale;
        double realY = screenHeight - (y * totalScale) - (height * totalScale);
        double realWidth = width * totalScale;
        double realHeight = height * totalScale;

        GL11.glScissor((int) realX, (int) realY, (int) realWidth, (int) realHeight);
    }

    public static void endScissorBox() {
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }
}
