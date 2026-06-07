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

    public static void drawBox(double x1, double y1, double z1, double x2, double y2, double z2, float red, float green,
            float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x1, y1, z2);

        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y2, z2);

        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x1, y2, z1);

        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y2, z2);

        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y2, z1);

        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z1);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawBoxOutline(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y1, z1);

        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2);

        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x1, y1, z2);

        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z1);

        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y2, z1);

        GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2);

        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y2, z2);

        GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y2, z1);

        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y2, z1);

        GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y2, z1);

        GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y2, z2);

        GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y2, z2);
    }

    public static void drawColoredCircle(double x, double y, double radius, float brightness) {
        GL11.glPushMatrix();
        GL11.glLineWidth(3.5F);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glBegin(3);

        for (int i = 0; i < 360; ++i) {
            color(Color.HSBtoRGB(1.0F, 0.0F, brightness));
            GL11.glVertex2d(x, y);
            color(Color.HSBtoRGB((float) i / 360.0F, 1.0F, brightness));
            GL11.glVertex2d(x + Math.sin(Math.toRadians(i)) * radius, y + Math.cos(Math.toRadians(i)) * radius);
        }

        GL11.glEnd();
        GL11.glShadeModel(7424);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
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

    public static void drawAbsoluteRect(float x, float y, float x2, float y2, int color) {
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, color);
    }

    public static void drawBorder(float left, float top, float right, float bottom, float borderWidth, int borderColor,
            boolean borderIncludedInBounds) {
        float adjustedLeft = left;
        float adjustedTop = top;
        float adjustedRight = right;
        float adjustedBottom = bottom;
        if (!borderIncludedInBounds) {
            adjustedLeft = left - borderWidth;
            adjustedTop = top - borderWidth;
            adjustedRight = right + borderWidth;
            adjustedBottom = bottom + borderWidth;
        }

        drawAbsoluteRect(adjustedLeft, adjustedTop, adjustedRight, adjustedTop + borderWidth, borderColor);
        drawAbsoluteRect(adjustedLeft, adjustedBottom - borderWidth, adjustedRight, adjustedBottom, borderColor);
        drawAbsoluteRect(
                adjustedLeft, adjustedTop + borderWidth, adjustedLeft + borderWidth, adjustedBottom - borderWidth,
                borderColor);
        drawAbsoluteRect(
                adjustedRight - borderWidth, adjustedTop + borderWidth, adjustedRight, adjustedBottom - borderWidth,
                borderColor);
    }

    public static void drawBorder(double left, double top, double right, double bottom, double borderWidth,
            int borderColor, boolean borderIncludedInBounds) {
        drawBorder((float) left, (float) top, (float) right, (float) bottom, (float) borderWidth, borderColor,
                borderIncludedInBounds);
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

    private static java.nio.IntBuffer viewportBuffer = net.minecraft.client.renderer.GLAllocation.createDirectIntBuffer(16);
    private static java.nio.FloatBuffer modelViewBuffer = net.minecraft.client.renderer.GLAllocation.createDirectFloatBuffer(16);
    private static java.nio.FloatBuffer projectionBuffer = net.minecraft.client.renderer.GLAllocation.createDirectFloatBuffer(16);
    private static java.nio.FloatBuffer vectorBuffer = net.minecraft.client.renderer.GLAllocation.createDirectFloatBuffer(4);

    public static javax.vecmath.Vector4d projectToScreen(net.minecraft.entity.Entity entity, double screenScale, float renderPartialTicks) {
        javax.vecmath.Vector4d vector4d;
        {
            double d3 = lerpDouble(entity.posX, entity.lastTickPosX, renderPartialTicks);
            double d4 = lerpDouble(entity.posY, entity.lastTickPosY, renderPartialTicks);
            double d5 = lerpDouble(entity.posZ, entity.lastTickPosZ, renderPartialTicks);
            net.minecraft.util.AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f).offset(d3 - entity.posX, d4 - entity.posY, d5 - entity.posZ);
            vector4d = null;
            for (javax.vecmath.Vector3d vector3d : new javax.vecmath.Vector3d[]{new javax.vecmath.Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new javax.vecmath.Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new javax.vecmath.Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new javax.vecmath.Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new javax.vecmath.Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new javax.vecmath.Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new javax.vecmath.Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new javax.vecmath.Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)}) {
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
                GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
                GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);
                if (!org.lwjgl.util.glu.GLU.gluProject((float) (vector3d.x - mc.getRenderManager().viewerPosX), (float) (vector3d.y - mc.getRenderManager().viewerPosY), (float) (vector3d.z - mc.getRenderManager().viewerPosZ), modelViewBuffer, projectionBuffer, viewportBuffer, vectorBuffer))
                    continue;
                vector3d = new javax.vecmath.Vector3d((double) vectorBuffer.get(0) / screenScale, (double) ((float) org.lwjgl.opengl.Display.getHeight() - vectorBuffer.get(1)) / screenScale, vectorBuffer.get(2));
                if (!(vector3d.z >= 0.0) || !(vector3d.z < 1.0)) continue;
                if (vector4d == null) {
                    vector4d = new javax.vecmath.Vector4d(vector3d.x, vector3d.y, vector3d.z, 0.0);
                }
                vector4d.x = Math.min(vector3d.x, vector4d.x);
                vector4d.y = Math.min(vector3d.y, vector4d.y);
                vector4d.z = Math.max(vector3d.x, vector4d.z);
                vector4d.w = Math.max(vector3d.y, vector4d.w);
            }
        }
        return vector4d;
    }

    public static void drawOutlineRect(float x1, float y1, float x2, float y2, float lineWidth, int backgroundColor, int lineColor) {
        drawAbsoluteRect(0.0f, 0.0f, x2, 27.0f, backgroundColor);
        if (lineColor == 0) {
            return;
        }
        setColor(lineColor);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x1, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);
        GlStateManager.resetColor();
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float lineWidth, int color) {
        setColor(color);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);
        GlStateManager.resetColor();
    }
    public static void drawRect3D(float x1, float y1, float x2, float y2, int color) {
        if (color == 0) {
            return;
        }
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y1);
        GL11.glEnd();
        GlStateManager.resetColor();
    }
    public static void setColor(int argb) {
        float f = (float) (argb >> 24 & 0xFF) / 255.0f;
        float f2 = (float) (argb >> 16 & 0xFF) / 255.0f;
        float f3 = (float) (argb >> 8 & 0xFF) / 255.0f;
        float f4 = (float) (argb & 0xFF) / 255.0f;
        GlStateManager.color(f2, f3, f4, f);
    }
    public static double lerpDouble(double current, double previous, double t) {
        return previous + (current - previous) * t;
    }

    public static void drawEntityBoundingBox(net.minecraft.entity.Entity entity, int red, int green, int blue, int alpha, float lineWidth, double expand, float renderPartialTicks) {
        double d2 = lerpDouble(entity.posX, entity.lastTickPosX, renderPartialTicks);
        double d3 = lerpDouble(entity.posY, entity.lastTickPosY, renderPartialTicks);
        double d4 = lerpDouble(entity.posZ, entity.lastTickPosZ, renderPartialTicks);
        drawBoundingBox(entity.getEntityBoundingBox().expand(expand, expand, expand).offset(d2 - entity.posX, d3 - entity.posY, d4 - entity.posZ).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ), red, green, blue, alpha, lineWidth);
    }
    public static void drawBoundingBox(net.minecraft.util.AxisAlignedBB axisAlignedBB, int red, int green, int blue, int alpha, float lineWidth) {
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        net.minecraft.client.renderer.RenderGlobal.drawOutlinedBoundingBox(axisAlignedBB, red, green, blue, alpha);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);
    }

    public static void drawCornerESP(net.minecraft.entity.player.EntityPlayer entity, float red, float green, float blue, float renderPartialTicks) {
        float x = (float) (lerpDouble(entity.posX, entity.lastTickPosX, renderPartialTicks) - mc.getRenderManager().viewerPosX);
        float y = (float) (lerpDouble(entity.posY, entity.lastTickPosY, renderPartialTicks) - mc.getRenderManager().viewerPosY);
        float z = (float) (lerpDouble(entity.posZ, entity.lastTickPosZ, renderPartialTicks) - mc.getRenderManager().viewerPosZ);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0F, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-0.098F, -0.098F, 0.098F);
        float width = (float) (26.6 * entity.width / 2.0);
        float height = 12.0F;
        GlStateManager.color(red, green, blue);
        draw3DRect(width, height - 1.0F, width - 4.0F, height);
        draw3DRect(-width, height - 1.0F, -width + 4.0F, height);
        draw3DRect(-width, height, -width + 1.0F, height - 4.0F);
        draw3DRect(width, height, width - 1.0F, height - 4.0F);
        draw3DRect(width, -height, width - 4.0F, -height + 1.0F);
        draw3DRect(-width, -height, -width + 4.0F, -height + 1.0F);
        draw3DRect(-width, -height + 1.0F, -width + 1.0F, -height + 4.0F);
        draw3DRect(width, -height + 1.0F, width - 1.0F, -height + 4.0F);
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        draw3DRect(width, height, width - 4.0F, height + 0.2F);
        draw3DRect(-width, height, -width + 4.0F, height + 0.2F);
        draw3DRect(-width - 0.2F, height + 0.2F, -width, height - 4.0F);
        draw3DRect(width + 0.2F, height + 0.2F, width, height - 4.0F);
        draw3DRect(width + 0.2F, -height, width - 4.0F, -height - 0.2F);
        draw3DRect(-width - 0.2F, -height, -width + 4.0F, -height - 0.2F);
        draw3DRect(-width - 0.2F, -height, -width, -height + 4.0F);
        draw3DRect(width + 0.2F, -height, width, -height + 4.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawFake2DESP(net.minecraft.entity.player.EntityPlayer entity, float red, float green, float blue, float renderPartialTicks) {
        float x = (float) (lerpDouble(entity.posX, entity.lastTickPosX, renderPartialTicks) - mc.getRenderManager().viewerPosX);
        float y = (float) (lerpDouble(entity.posY, entity.lastTickPosY, renderPartialTicks) - mc.getRenderManager().viewerPosY);
        float z = (float) (lerpDouble(entity.posZ, entity.lastTickPosZ, renderPartialTicks) - mc.getRenderManager().viewerPosZ);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0F, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-0.1F, -0.1F, 0.1F);
        GlStateManager.color(red, green, blue);
        float width = (float) (23.3 * entity.width / 2.0);
        float height = 12.0F;
        draw3DRect(width, height, -width, height + 0.4F);
        draw3DRect(width, -height, -width, -height + 0.4F);
        draw3DRect(width, -height + 0.4F, width - 0.4F, height + 0.4F);
        draw3DRect(-width, -height + 0.4F, -width + 0.4F, height + 0.4F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void draw3DRect(float x1, float y1, float x2, float y2) {
        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
    }
}