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

    public static CustomFontRenderer arial16;
    public static CustomFontRenderer arial18;
    public static CustomFontRenderer arial24;
    public static CustomFontRenderer arial36;
    public static CustomFontRenderer arial48;
    public static CustomFontRenderer arial64;

    public static CustomFontRenderer esp21;

    public static CustomFontRenderer consolas15;
    public static CustomFontRenderer consolas17;
    public static CustomFontRenderer consolas18;
    public static CustomFontRenderer consolas21;

    public static CustomFontRenderer productSans18;
    public static CustomFontRenderer productSans20;
    public static CustomFontRenderer productSans26;
    public static CustomFontRenderer productSans31;

    public static CustomFontRenderer comfortaaLight64;

    public static CustomFontRenderer sigma18;
    public static CustomFontRenderer sigma22;
    public static CustomFontRenderer sigma52;

    public static CustomFontRenderer poppins15;
    public static CustomFontRenderer poppins18;
    public static CustomFontRenderer montserrat15;
    public static CustomFontRenderer montserrat18;

    public static CustomFontRenderer inter15;
    public static CustomFontRenderer inter18;
    public static CustomFontRenderer wielixFont15;
    public static CustomFontRenderer wielixFont20;
    public static CustomFontRenderer wielixFont26;

    public void init() {
        try {
            MinecraftForge.EVENT_BUS.register(this);

            arial16 = new CustomFontRenderer(createFontFromFile("arial.ttf", 16));
            arial18 = new CustomFontRenderer(createFontFromFile("arial.ttf", 18));
            arial24 = new CustomFontRenderer(createFontFromFile("arial.ttf", 24));
            arial36 = new CustomFontRenderer(createFontFromFile("arial.ttf", 36));
            arial48 = new CustomFontRenderer(createFontFromFile("arial.ttf", 48));
            arial64 = new CustomFontRenderer(createFontFromFile("arial.ttf", 64));
            esp21 = new CustomFontRenderer(createFontFromFile("esp.ttf", 21));
            consolas15 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 15));
            consolas17 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 17));
            consolas18 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 18));
            consolas21 = new CustomFontRenderer(createFontFromFile("consolas.ttf", 21));
            productSans18 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 18));
            productSans20 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 20));
            productSans26 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 26));
            productSans31 = new CustomFontRenderer(createFontFromFile("productsans.ttf", 31));
            comfortaaLight64 = new CustomFontRenderer(createFontFromFile("comfortaalight.ttf", 64));
            sigma18 = new CustomFontRenderer(createFontFromFile("skidma.otf", 18));
            sigma22 = new CustomFontRenderer(createFontFromFile("skidma.otf", 22));
            sigma52 = new CustomFontRenderer(createFontFromFile("skidma.otf", 52));
            poppins15 = new CustomFontRenderer(createFontFromFile("poppins.ttf", 15));
            poppins18 = new CustomFontRenderer(createFontFromFile("poppins.ttf", 18));

            montserrat15 = new CustomFontRenderer(createFontFromFile("montserrat.ttf", 15));
            montserrat18 = new CustomFontRenderer(createFontFromFile("montserrat.ttf", 18));
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

    private static Font createFontFromFile(String fileName, float size, int type) throws IOException, FontFormatException {
        ResourceLocation resourceLocation = new ResourceLocation(".res/f/" + fileName);
        return Font.createFont(type, Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()).deriveFont(size);
    }

    private static Font createFontFromFile(String fileName, float size) throws IOException, FontFormatException {
        ResourceLocation resourceLocation = new ResourceLocation(".res/f/" + fileName);
        return Font.createFont(Font.PLAIN, Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()).deriveFont(size);
    }
}