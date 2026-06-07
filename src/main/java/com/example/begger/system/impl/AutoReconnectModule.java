package com.example.begger.system.impl;

import com.example.begger.RankBegger;
import com.example.begger.system.Category;
import com.example.begger.system.Module;
import com.example.begger.settings.impl.NumberSetting;
import com.example.begger.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

public class AutoReconnectModule extends Module {

    public final NumberSetting delay;
    private final Timer reconnectTimer = new Timer();
    private boolean reconnecting = false;
    private GuiButton autoReconnectButton;

    public AutoReconnectModule() {
        super("AutoReconnect", Category.MISC);
        delay = new NumberSetting("Delay (s)", this, 5, 1, 60, 0);
        RankBegger.settingsManager.addSetting(delay, this);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reconnecting = false;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!toggled) return;
        if (event.gui instanceof GuiDisconnected) {
            reconnecting = true;
            reconnectTimer.reset();
            int yPos = event.gui.height / 2 + event.gui.height / 4 + 24;
            autoReconnectButton = new GuiButton(8844, event.gui.width / 2 - 100, yPos, 200, 20, "AutoReconnect");
            event.buttonList.add(autoReconnectButton);
            updateButtonText();
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!toggled) return;
        if (event.gui instanceof GuiDisconnected && event.button.id == 8844) {
            reconnecting = !reconnecting;
            if (reconnecting) {
                reconnectTimer.reset();
            }
            updateButtonText();
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!toggled) return;
        if (event.gui instanceof GuiDisconnected) {
            updateButtonText();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!toggled) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiDisconnected) {
            if (reconnecting) {
                long delayMs = (long) (delay.getValue() * 1000);
                if (reconnectTimer.hasTimeElapsed(delayMs, false)) {
                    reconnecting = false;
                    ServerData serverData = new ServerData("Hypixel", "mc.hypixel.net", false);
                    mc.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), mc, serverData));
                }
            }
        } else {
            reconnecting = false;
        }
    }

    private void updateButtonText() {
        if (autoReconnectButton != null) {
            if (reconnecting) {
                long delayMs = (long) (delay.getValue() * 1000);
                long timeLeft = delayMs - reconnectTimer.getPassed();
                if (timeLeft < 0) timeLeft = 0;
                int secondsLeft = (int) Math.ceil(timeLeft / 1000.0);
                autoReconnectButton.displayString = "AutoReconnect (" + secondsLeft + ")";
            } else {
                autoReconnectButton.displayString = "AutoReconnect";
            }
        }
    }
}
