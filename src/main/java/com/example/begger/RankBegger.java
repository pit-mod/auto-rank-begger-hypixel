package com.example.begger;

import com.example.begger.handler.BeggerHandler;
import com.example.begger.system.ModuleManager;
import com.example.begger.system.SettingsManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = RankBegger.MODID, version = RankBegger.VERSION, clientSideOnly = true)
public class RankBegger {
    public static final String MODID = "rankbegger";
    public static final String VERSION = "1.0";

    public static KeyBinding guiKey;
    public static KeyBinding toggleKey;

    public static ModuleManager moduleManager;
    public static SettingsManager settingsManager;
    public static com.example.begger.ui.font.FontManager fontManager;
    public static RankBegger instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        
        // Initialize settingsManager BEFORE moduleManager
        settingsManager = new SettingsManager();
        moduleManager = new ModuleManager();
        fontManager = new com.example.begger.ui.font.FontManager();
        fontManager.init();

        Config.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        guiKey = new KeyBinding("Open Begger GUI", Keyboard.KEY_RSHIFT, "Rank Begger");
        toggleKey = new KeyBinding("Toggle Begger", Keyboard.KEY_B, "Rank Begger");

        ClientRegistry.registerKeyBinding(guiKey);
        ClientRegistry.registerKeyBinding(toggleKey);

        MinecraftForge.EVENT_BUS.register(new BeggerHandler());
    }
}
