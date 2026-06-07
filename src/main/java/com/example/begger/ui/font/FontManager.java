package com.example.begger.ui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.example.begger.utils.interfaces.MC;

import java.awt.*;
import java.io.IOException;

public class FontManager implements MC {
    public static int lastHeight = 0;
    public static int lastWidth = 0;

    public static CustomFontRenderer consolas15;
    public static CustomFontRenderer consolas18;

    public static CustomFontRenderer productSans18;
    public static CustomFontRenderer productSans20;

    public static CustomFontRenderer poppins15;
    public static CustomFontRenderer montserrat15;

    public static CustomFontRenderer inter15;
    public static CustomFontRenderer inter18;
    public static CustomFontRenderer wielixFont15;
    public static CustomFontRenderer wielixFont20;
    public static CustomFontRenderer wielixFont26;

    public void init() {
        try {
            MinecraftForge.EVENT_BUS.register(this);

            consolas15 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 15));
            consolas18 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 18));
            productSans18 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 18));
            productSans20 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 20));
            poppins15 = new CustomFontRenderer(createFontFromFile("poppins.ttf", 15));
            montserrat15 = new CustomFontRenderer(createFontFromFile("montserrat.ttf", 15));
            inter15 = new CustomFontRenderer(createFontFromFile("inter.ttf", 15));
            inter18 = new CustomFontRenderer(createFontFromFile("inter.ttf", 18));
            wielixFont15 = new CustomFontRenderer(createFontFromFile("TTGateralTrial-SemiBold- for wielix.ttf", 15));
            wielixFont20 = new CustomFontRenderer(createFontFromFile("TTGateralTrial-SemiBold- for wielix.ttf", 20));
            wielixFont26 = new CustomFontRenderer(createFontFromFile("TTGateralTrial-SemiBold- for wielix.ttf", 26));
            lastHeight = mc.displayHeight;
            lastWidth = mc.displayWidth;
        } catch (Exception e) {
            System.err.println("Failed to initialize fonts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent event) {
        if (lastHeight != mc.displayHeight || lastWidth != mc.displayWidth) {
            init();
        }
    }

    private static Font createFontFromFile(String fileName, float size) throws IOException, FontFormatException {
        ResourceLocation resourceLocation = new ResourceLocation(".res/f/" + fileName);
        return Font.createFont(Font.PLAIN, Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()).deriveFont(size);
    }
}
