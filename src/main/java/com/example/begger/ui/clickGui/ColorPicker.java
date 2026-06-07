package com.example.begger.ui.clickGui;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import com.example.begger.utils.interfaces.MC;
import com.example.begger.utils.RenderUtil;
import com.example.begger.ui.font.FontManager;

import java.awt.*;
import java.util.function.Consumer;

public class ColorPicker implements MC {

    private static final float PAD = 14f;
    private static final float FIELD_SIZE = 120f;
    private static final float SLIDER_HEIGHT = 10f;
    private static final float SLIDER_GAP = 12f;
    private static final float FOOTER_HEIGHT = 20f;
    private static final float PANEL_W = FIELD_SIZE + (PAD * 2);
    private static final float PANEL_H = FIELD_SIZE + SLIDER_GAP + SLIDER_HEIGHT + SLIDER_GAP + FOOTER_HEIGHT + (PAD * 2);

    public float panelX, panelY;
    private float hue, saturation, brightness, alpha;
    private float animHue, animSat, animBri;
    private float animFieldDotRad = 4f;
    private float animHueDotRad = 3.5f;

    private boolean draggingField = false;
    private boolean draggingHue = false;

    public float animProgress = 0f;
    private String currentColorHexInputString = "";

    private final Consumer<Color> color;
    public int x, y;
    private double radius;
    private double selectedX, selectedY;

    public ColorPicker(double radius, Color selectedColor, Consumer<Color> color) {
        this.radius = radius;
        this.color = color;
        float[] hsb = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = Math.max(0.01f, hsb[2]);
        this.alpha = selectedColor.getAlpha() / 255f;
        snapAnimValues();
        this.currentColorHexInputString = fmtHex(selectedColor);
    }

    private void snapAnimValues() {
        animHue = hue;
        animSat = saturation;
        animBri = brightness;
        animFieldDotRad = 4f;
        animHueDotRad = 3.5f;
    }

    public void draw() {
        if (animProgress <= 0.001f) return;

        float lerpF = 0.35f;
        float hd = hue - animHue;
        if (hd > 0.5f) hd -= 1f;
        if (hd < -0.5f) hd += 1f;
        animHue += hd * lerpF;
        if (animHue < 0f) animHue += 1f;
        if (animHue > 1f) animHue -= 1f;

        animSat += (saturation - animSat) * lerpF;
        animBri += (brightness - animBri) * lerpF;

        float targetFieldDot = draggingField ? 6f : 4f;
        animFieldDotRad += (targetFieldDot - animFieldDotRad) * 0.45f;

        float targetHueDot = draggingHue ? 5.5f : 3.5f;
        animHueDotRad += (targetHueDot - animHueDotRad) * 0.45f;

        float scale = 0.85f + 0.15f * (float)Math.sin(animProgress * Math.PI / 2);
        int alphaInt = (int) (animProgress * 255);

        float pivX = panelX + PANEL_W / 2f;
        float pivY = panelY + PANEL_H / 2f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(pivX, pivY, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-pivX, -pivY, 0);

        RenderUtil.drawRoundedRect(panelX - 4, panelY - 4, PANEL_W + 8, PANEL_H + 8, 14f, ((int)(alphaInt * 0.15f) << 24));
        RenderUtil.drawRoundedRect(panelX - 2, panelY - 2, PANEL_W + 4, PANEL_H + 4, 12f, ((int)(alphaInt * 0.25f) << 24));
        RenderUtil.drawRoundedRect(panelX, panelY, PANEL_W, PANEL_H, 10f, (alphaInt << 24) | 0x1A1A1E);
        drawBorder(panelX, panelY, PANEL_W, PANEL_H, 10f, ((int)(alphaInt * 0.4f) << 24) | 0x3E3E42);

        float fieldX = panelX + PAD;
        float fieldY = panelY + PAD;
        drawColorField(fieldX, fieldY, FIELD_SIZE, FIELD_SIZE, alphaInt);

        float hueY = fieldY + FIELD_SIZE + SLIDER_GAP;
        drawHueSlider(fieldX, hueY, FIELD_SIZE, SLIDER_HEIGHT, alphaInt);

        float footerY = hueY + SLIDER_HEIGHT + SLIDER_GAP;
        drawFooter(fieldX, footerY, FIELD_SIZE, FOOTER_HEIGHT, alphaInt);

        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void drawColorField(float x, float y, float w, float h, int alpha) {
        Color hueColor = Color.getHSBColor(animHue, 1f, 1f);
        float[] c = new float[] { hueColor.getRed() / 255f, hueColor.getGreen() / 255f, hueColor.getBlue() / 255f };
        float a = alpha / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(1f, 1f, 1f, a);
        GL11.glVertex2d(x, y);

        GL11.glColor4f(0f, 0f, 0f, a);
        GL11.glVertex2d(x, y + h);

        GL11.glColor4f(0f, 0f, 0f, a);
        GL11.glVertex2d(x + w, y + h);

        GL11.glColor4f(c[0], c[1], c[2], a);
        GL11.glVertex2d(x + w, y);
        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        int bgColor = (alpha << 24) | 0x1A1A1E;
        drawInvertedCornerMask(x, y, 4f, bgColor);
        drawInvertedCornerMask(x + w, y, -4f, bgColor);
        drawInvertedCornerMask(x, y + h, 4f, -4f, bgColor);
        drawInvertedCornerMask(x + w, y + h, -4f, -4f, bgColor);

        drawBorder(x, y, w, h, 2f, ((int)(alpha * 0.3f) << 24) | 0x000000);

        float safeDotX = x + 4f + (animSat * (w - 8f));
        float safeDotY = y + 4f + ((1f - animBri) * (h - 8f));
        drawDot(safeDotX, safeDotY, animFieldDotRad, alpha, Color.getHSBColor(animHue, animSat, animBri));
    }

    private void drawHueSlider(float x, float y, float w, float h, int alpha) {
        int segments = 12;
        float segW = w / segments;
        float a = alpha / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < segments; i++) {
            float hueStart = (float) i / segments;
            float hueEnd = (float) (i + 1) / segments;
            Color c1 = Color.getHSBColor(hueStart, 1f, 1f);
            Color c2 = Color.getHSBColor(hueEnd, 1f, 1f);

            GL11.glColor4f(c1.getRed()/255f, c1.getGreen()/255f, c1.getBlue()/255f, a);
            GL11.glVertex2d(x + (i * segW), y);
            GL11.glVertex2d(x + (i * segW), y + h);

            GL11.glColor4f(c2.getRed()/255f, c2.getGreen()/255f, c2.getBlue()/255f, a);
            GL11.glVertex2d(x + ((i + 1) * segW), y + h);
            GL11.glVertex2d(x + ((i + 1) * segW), y);
        }
        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        int bgColor = (alpha << 24) | 0x1A1A1E;
        drawInvertedCornerMask(x, y, 3f, bgColor);
        drawInvertedCornerMask(x + w, y, -3f, bgColor);
        drawInvertedCornerMask(x, y + h, 3f, -3f, bgColor);
        drawInvertedCornerMask(x + w, y + h, -3f, -3f, bgColor);

        drawBorder(x, y, w, h, 2f, ((int)(alpha * 0.3f) << 24) | 0x000000);

        float thumbX = x + 4f + (animHue * (w - 8f));
        drawDot(thumbX, y + h / 2f, animHueDotRad, alpha, Color.getHSBColor(animHue, 1f, 1f));
    }

    private void drawFooter(float x, float y, float w, float h, int alpha) {
        float swatchW = 28f;
        RenderUtil.drawRoundedRect(x, y, swatchW, h, 4f, (alpha << 24) | (getColor().getRGB() & 0xFFFFFF));
        drawBorder(x, y, swatchW, h, 4f, ((int)(alpha * 0.4f) << 24) | 0x000000);

        String hex = "#" + fmtHex(getColor());
        int textColor = ((int) (alpha * 0.9f) << 24) | 0xDDDDDD;
        FontManager.consolas18.drawString(hex, x + swatchW + 10f, y + (h - FontManager.consolas18.getFontHeight()) / 2f + 1f, textColor);
    }

    private void drawDot(float cx, float cy, float r, int alpha, Color innerColor) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        RenderUtil.drawCircle(cx, cy + 1f, r + 2f, ((int)(alpha * 0.2f) << 24));
        RenderUtil.drawCircle(cx, cy, r + 1f, (alpha << 24) | 0xFFFFFF);
        RenderUtil.drawCircle(cx, cy, r, (alpha << 24) | (innerColor.getRGB() & 0xFFFFFF));
    }

    private void drawBorder(float x, float y, float w, float h, float r, int color) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        RenderUtil.drawRoundedRect(x, y, w, 1f, r, color);
        RenderUtil.drawRoundedRect(x, y + h - 1.5f, w, 1.5f, r, color);
        RenderUtil.drawRoundedRect(x, y, 1f, h, r, color);
        RenderUtil.drawRoundedRect(x + w - 1f, y, 1f, h, r, color);
    }

    private void drawInvertedCornerMask(float x, float y, float hw, float color) {
        drawInvertedCornerMask(x, y, hw, hw, (int)color);
    }

    private void drawInvertedCornerMask(float x, float y, float hw, float hh, int color) {
        RenderUtil.drawRect(x, y, hw, hh, color);
        float mX = hw > 0 ? 1f : -1f;
        float mY = hh > 0 ? 1f : -1f;
        RenderUtil.drawRect(x + mX * (Math.abs(hw) - 1f), y, 1f * mX, hh, 0);
        RenderUtil.drawRect(x, y + mY * (Math.abs(hh) - 1f), hw, 1f * mY, 0);
    }

    public void click(double mx, double my, int btn) {
        if (btn != 0 || animProgress < 0.5f) return;
        float fieldX = panelX + PAD;
        float fieldY = panelY + PAD;
        float hueY = fieldY + FIELD_SIZE + SLIDER_GAP;

        if (mx >= fieldX - 2 && mx <= fieldX + FIELD_SIZE + 2 && my >= fieldY - 2 && my <= fieldY + FIELD_SIZE + 2) {
            draggingField = true;
            pickColorQuad(mx, my, fieldX, fieldY, FIELD_SIZE, FIELD_SIZE);
        }
        else if (mx >= fieldX - 2 && mx <= fieldX + FIELD_SIZE + 2 && my >= hueY - 2 && my <= hueY + SLIDER_HEIGHT + 2) {
            draggingHue = true;
            pickHueList(mx, fieldX, FIELD_SIZE);
        }
    }

    public void drag(double mx, double my) {
        float fieldX = panelX + PAD;
        float fieldY = panelY + PAD;

        if (draggingField) {
            pickColorQuad(mx, my, fieldX, fieldY, FIELD_SIZE, FIELD_SIZE);
        } else if (draggingHue) {
            pickHueList(mx, fieldX, FIELD_SIZE);
        }
    }

    public void release() {
        draggingField = false;
        draggingHue = false;
    }

    private void pickColorQuad(double mx, double my, float x, float y, float w, float h) {
        saturation = (float) Math.max(0, Math.min(1, (mx - x) / w));
        brightness = (float) Math.max(0, Math.min(1, 1f - (my - y) / h));
        pushColor();
    }

    private void pickHueList(double mx, float x, float w) {
        hue = (float) Math.max(0, Math.min(1, (mx - x) / w));
        pushColor();
    }

    private void pushColor() {
        Color c = getColor();
        currentColorHexInputString = fmtHex(c);
        color.accept(c);
    }

    public Color getColor() {
        Color rgb = Color.getHSBColor(hue, saturation, brightness);
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (alpha * 255));
    }

    public void setColor(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = Math.max(0.01f, hsb[2]);
        this.alpha = c.getAlpha() / 255f;
        snapAnimValues();
        this.currentColorHexInputString = fmtHex(c);
    }

    public Color stringToColor(String s) {
        try {
            long v = Long.parseLong(s, 16);
            if (s.length() >= 6) return new Color((int) v, s.length() == 8);
        } catch (NumberFormatException ignored) {}
        return getColor();
    }

    public boolean isInsidePanel(double mx, double my) {
        return mx >= panelX && mx <= panelX + PANEL_W
            && my >= panelY && my <= panelY + PANEL_H;
    }

    public float getPanelWidth() { return PANEL_W; }
    public float getPanelHeight() { return PANEL_H; }

    public static String fmtHex(Color c) {
        return String.format("%02X%02X%02X%02X", c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String getStringRepresentation(Color c) {
        return fmtHex(c);
    }
    public String getCurrentColorHexInputString() {
        return currentColorHexInputString;
    }

    public void setCurrentColorHexInputString(String currentColorHexInputString) {
        this.currentColorHexInputString = currentColorHexInputString;
    }
}