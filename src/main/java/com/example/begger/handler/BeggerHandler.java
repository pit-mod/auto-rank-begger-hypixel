package com.example.begger.handler;

import com.example.begger.RankBegger;
import com.example.begger.context.BeggerContext;
import com.example.begger.context.FailsafeState;
import com.example.begger.manager.*;
import com.example.begger.scheduler.TickScheduler;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Thin orchestrator that routes events to specialized managers.
 * Owns the BeggerContext (state) and TickScheduler.
 */
public class BeggerHandler {

    private final BeggerContext ctx = new BeggerContext();
    private final TickScheduler scheduler = new TickScheduler();

    private final RankDetector rankDetector;
    private final MessageGenerator messageGenerator;
    private final TypingSimulator typingSimulator;
    private final GiftManager giftManager;
    private final ChatManager chatManager;
    private final AntiAfkManager antiAfkManager;
    private final FailsafeManager failsafeManager;
    private final HudRenderer hudRenderer;

    public BeggerHandler() {
        this.rankDetector = new RankDetector(ctx);
        this.messageGenerator = new MessageGenerator(ctx, rankDetector);
        this.typingSimulator = new TypingSimulator(ctx);
        this.giftManager = new GiftManager(ctx, rankDetector);
        this.chatManager = new ChatManager(ctx, rankDetector, messageGenerator, typingSimulator, giftManager, scheduler);
        this.antiAfkManager = new AntiAfkManager(ctx.random);
        this.failsafeManager = new FailsafeManager(ctx);
        this.hudRenderer = new HudRenderer(ctx);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (RankBegger.guiKey.isPressed()) {
            McUtil.mc().displayGuiScreen(new com.example.begger.ui.clickGui.ClickGui());
        }
        if (RankBegger.toggleKey.isPressed()) {
            RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
            if (mod != null) {
                mod.toggle();
                if (!mod.toggled) {
                    antiAfkManager.releaseAllKeys();
                    scheduler.clear();
                    ctx.reset();
                }
                String status = mod.toggled ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled";
                McUtil.addClientMessage("Begger", status);
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        chatManager.handleChat(event);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        giftManager.handleChestGiftGui(event);
    }

    @SubscribeEvent
    public void onRenderHud(RenderGameOverlayEvent.Post event) {
        hudRenderer.render(event);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !McUtil.hasPlayer()) return;
        
        RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
        if (mod == null || !mod.toggled) {
            ctx.idMessageSent = false;
            ctx.targetUsername = null;
            ctx.firstBegSent = false;
            ctx.wasToggled = false;
            return;
        }

        if (!ctx.wasToggled) {
            ctx.sessionStartTime = System.currentTimeMillis();
            ctx.lastGiftAcceptedTime = ctx.sessionStartTime;
            ctx.messagesSinceLastGift = 0;
            ctx.wasToggled = true;
        }

        long now = System.currentTimeMillis();

        // Tick scheduler
        scheduler.tick(now);

        // Periodic identification message
        if (ctx.targetUsername == null || now - ctx.lastIdMessageTime >= 600000L) {
            long delay = ctx.targetUsername == null ? 20000L : 600000L;
            if (!ctx.idMessageSent || now - ctx.lastIdMessageTime >= delay) {
                ctx.lastSentIdMessage = messageGenerator.getRandomIdMessage();
                McUtil.sendChat(ctx.lastSentIdMessage);
                ctx.idMessageSent = true;
                ctx.lastIdMessageTime = now;
            }
        }

        // Failsafes
        if (mod.failsafe.isEnabled()) {
            failsafeManager.tick(mod);
            failsafeManager.handleGuiClicks();
        } else {
            ctx.failsafeState = FailsafeState.BEGGING;
        }

        if (ctx.failsafeState != FailsafeState.BEGGING) return;
        if (ctx.targetUsername == null) return;

        // Message simulation
        if (typingSimulator.hasPending()) {
            typingSimulator.tickPendingMessage(now);
            return;
        }

        // Beg logic
        boolean shouldBeg = !mod.smartMode.isEnabled() || (now - ctx.lastGiftTime <= 120000) || !ctx.firstBegSent;
        if (shouldBeg) {
            long delayMs = (long) (mod.delay.getValue() * 1000) + ctx.currentJitter;
            if (!ctx.firstBegSent || now - ctx.lastBegTime >= delayMs) {
                chatManager.sendBegMessage(now, mod);
            }
        }

        // Anti-AFK
        if (McUtil.mc().currentScreen == null) {
            antiAfkManager.tick(now);
        } else {
            antiAfkManager.releaseAllKeys();
        }

        // HUD/Rank sync
        if (now - ctx.lastRankUpdateTime > 1000L) {
            rankDetector.updateCurrentRankFromScoreboard();
            ctx.lastRankUpdateTime = now;
        }

        // Gift Manager tick (webhook screens, book GUI)
        giftManager.tick(now);
    }
}
