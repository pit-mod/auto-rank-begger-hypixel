package com.example.begger.util.font;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class CustomFontRenderer {
    private final Font font;
    private final boolean antiAlias;
    private final boolean fractionalMetrics;
    private final CharData[] charData = new CharData[256];
    private DynamicTexture tex;
    private int fontHeight = -1;

    public CustomFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        generateFont();
    }

    private void generateFont() {
        int imgSize = 512;
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imgSize, imgSize);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        FontMetrics fontMetrics = g.getFontMetrics();
        int charHeight = 0;
        int positionX = 0;
        int positionY = 1;

        for (int i = 0; i < 256; i++) {
            char ch = (char) i;
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
            charData.width = (int) dimensions.getBounds().getWidth() + 8;
            charData.height = (int) dimensions.getBounds().getHeight();

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > this.fontHeight) {
                this.fontHeight = charData.height;
            }

            charData.width -= 8;
            this.charData[i] = charData;
            g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width + 1;
        }
        tex = new DynamicTexture(img);
    }

    public float drawString(String text, float x, float y, int color) {
        if (text == null) return 0;
        
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        if (alpha == 0.0F) alpha = 1.0F;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 1f);
        
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.bindTexture(tex.getGlTextureId());
        
        float drawX = x * 2;
        float drawY = y * 2;
        
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 256 && charData[ch] != null) {
                CharData cd = this.charData[ch];
                drawChar(cd, drawX, drawY);
                drawX += cd.width - 2;
            }
        }
        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        return drawX / 2;
    }

    private void drawChar(CharData charData, float x, float y) {
        float textureX = charData.storedX;
        float textureY = charData.storedY;
        float width = charData.width;
        float height = charData.height;

        float u1 = textureX / 512f;
        float v1 = textureY / 512f;
        float u2 = (textureX + width) / 512f;
        float v2 = (textureY + height) / 512f;

        GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u1, v2); GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(u2, v2); GL11.glVertex2f(x + width, y + height);
        
        GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u2, v2); GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(u2, v1); GL11.glVertex2f(x + width, y);
    }

    public float getStringWidth(String text) {
        if (text == null) return 0;
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 256 && this.charData[ch] != null) width += this.charData[ch].width - 2;
        }
        return width / 2.0f;
    }

    public float getHeight() {
        return this.fontHeight / 2.0f;
    }

    private static class CharData {
        public int width;
        public int height;
        public int storedX;
        public int storedY;
    }
}
