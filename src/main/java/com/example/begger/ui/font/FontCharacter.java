package com.example.begger.ui.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class FontCharacter {
    public int texture;
    public float width, height;

    public FontCharacter() {
        this.texture = 0;
        this.width = 0;
        this.height = 0;
    }

    public FontCharacter(int texture, float width, float height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void render(final float x, final float y) {
        if (texture == 0 || width == 0 || height == 0) {
            return;
        }

        GlStateManager.bindTexture(texture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }

    public int getTexture() {
        return this.texture;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

}