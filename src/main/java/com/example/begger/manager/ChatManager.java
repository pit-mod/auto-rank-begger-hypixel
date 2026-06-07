package com.example.begger.manager;

import com.example.begger.RankBegger;
import com.example.begger.context.BeggerContext;
import com.example.begger.context.FailsafeState;
import com.example.begger.scheduler.TickScheduler;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.LinkedHashMap;

public class ChatManager {

    private final BeggerContext ctx;
    private final RankDetector rankDetector;
    private final MessageGenerator messageGenerator;
    private final TypingSimulator typingSimulator;
    private final GiftManager giftManager;
    private final TickScheduler scheduler;

    private final LinkedHashMap<String, Integer> gifterQueue = new LinkedHashMap<>();
    private volatile boolean ggFirstPending = false;

    public ChatManager(BeggerContext ctx, RankDetector rankDetector,
                       MessageGenerator messageGenerator, TypingSimulator typingSimulator,
                       GiftManager giftManager, TickScheduler scheduler) {
        this.ctx = ctx;
        this.rankDetector = rankDetector;
        this.messageGenerator = messageGenerator;
        this.typingSimulator = typingSimulator;
        this.giftManager = giftManager;
        this.scheduler = scheduler;
    }

    public void handleChat(ClientChatReceivedEvent event) {
        if (event.message != null) {
            String fullText = event.message.getUnformattedText();
            String lowerText = fullText.toLowerCase();
            if (fullText.contains("wants to gift you") || fullText.contains("Rank Gift") ||
               (fullText.contains("Click here to accept") && !lowerText.contains("guild") && !lowerText.contains("party"))) {
                rankDetector.parseGiftedRank(fullText);
                giftManager.findAndClickAccept(event.message);
                giftManager.setWebhookScreenshotTicks(10);
            }
        }

        String text = event.message.getUnformattedText();

        if (text.contains("already in Bed Wars Lobby #1")) {
            ctx.reachedLobby1 = true;
            if (ctx.failsafeState != FailsafeState.BEGGING) {
                ctx.failsafeState = FailsafeState.BEGGING;
                McUtil.addClientMessage("Begger", EnumChatFormatting.GREEN + "Reached Lobby #1! Resuming...");
            }
        }

        if (ctx.lastSentIdMessage != null && text.contains(ctx.lastSentIdMessage)) {
            String[] parts = text.split(":");
            if (parts.length > 0) {
                String namePart = parts[0].trim();
                rankDetector.parseRank(namePart);
                String[] nameParts = namePart.split(" ");
                ctx.targetUsername = nameParts[nameParts.length - 1];
                McUtil.addClientMessage("Begger",
                    EnumChatFormatting.GREEN + "Identified target: " + ctx.targetUsername + " (" + ctx.currentRank.getName() + ")");
            }
        }

        if (ctx.targetUsername != null && text.contains(" joined the lobby!")) {
            String namePart = text.replace(" joined the lobby!", "").trim();
            if (namePart.endsWith(ctx.targetUsername)) {
                rankDetector.parseRank(namePart);
            }
        }

        if (text.contains(" gifted ") && text.contains(" to ")) {
            ctx.lastGiftTime = System.currentTimeMillis();

            String[] parts = text.split(" gifted ");
            if (parts.length > 0) {
                String gifterPart = parts[0].trim();
                String[] nameParts = gifterPart.split(" ");
                String lastGifterName = nameParts[nameParts.length - 1];

                RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
                if (mod != null && mod.toggled) {
                    boolean isNewGifter = !gifterQueue.containsKey(lastGifterName);
                    boolean queueWasEmpty = gifterQueue.isEmpty();

                    if (isNewGifter) {
                        gifterQueue.put(lastGifterName, 3);
                    } else {
                        gifterQueue.put(lastGifterName, gifterQueue.get(lastGifterName) + 1);
                    }

                    if (queueWasEmpty) {
                        ggFirstPending = true;
                        final String gifterName = gifterQueue.keySet().iterator().next();
                        long delay = 3000 + ctx.random.nextInt(1000);

                        scheduler.schedule(() -> {
                            rankDetector.updateCurrentRankFromScoreboard();
                            String nextRank = rankDetector.getTargetRank(ctx.currentRank).getName();
                            String chosenMsg = messageGenerator.getRandomGiftResponse(gifterName, nextRank);

                            if (McUtil.hasPlayer()) {
                                McUtil.sendChat(chosenMsg);
                                ctx.messageHistory.put(chosenMsg, System.currentTimeMillis());
                                ctx.lastBegTime = System.currentTimeMillis();
                                ctx.messagesSent++;
                                ctx.messagesSinceLastGift++;
                                ctx.lastSentMessage = chosenMsg;

                                if (gifterQueue.containsKey(gifterName)) {
                                    int remaining = gifterQueue.get(gifterName) - 1;
                                    if (remaining <= 0) {
                                        gifterQueue.remove(gifterName);
                                    } else {
                                        gifterQueue.put(gifterName, remaining);
                                    }
                                }
                                ggFirstPending = false;
                            }
                        }, delay);
                    }
                }
            }
        }
    }

    public void processGifterQueue(long now) {
        if (!gifterQueue.isEmpty() && !ggFirstPending) {
            java.util.Map.Entry<String, Integer> firstEntry = gifterQueue.entrySet().iterator().next();
            String gifterName = firstEntry.getKey();
            int remaining = firstEntry.getValue();

            rankDetector.updateCurrentRankFromScoreboard();
            String nextRank = rankDetector.getTargetRank(ctx.currentRank).getName();
            String chosenMsg = messageGenerator.getRandomGiftResponse(gifterName, nextRank);

            McUtil.sendChat(chosenMsg);
            ctx.messageHistory.put(chosenMsg, now);
            ctx.lastBegTime = now;
            ctx.firstBegSent = true;
            ctx.currentJitter = ctx.random.nextInt(6000) - 3000;
            ctx.messagesSent++;
            ctx.messagesSinceLastGift++;
            ctx.lastSentMessage = chosenMsg;

            remaining--;
            if (remaining <= 0) {
                gifterQueue.remove(gifterName);
            } else {
                gifterQueue.put(gifterName, remaining);
            }
            if (gifterQueue.isEmpty()) {
                ctx.lastGiftTime = 0;
            }
        }
    }

    public void sendBegMessage(long now, RankBeggerModule mod) {
        if (!gifterQueue.isEmpty() && !ggFirstPending) {
            processGifterQueue(now);
            return;
        }

        rankDetector.updateCurrentRankFromScoreboard();
        String msg = messageGenerator.getRandomBegMessage(ctx.currentRank);
        if (msg != null && !msg.isEmpty()) {
            typingSimulator.sendHumanMessage(msg, mod);
            if (!mod.simulateTyping.isEnabled()) {
                ctx.lastBegTime = now;
                ctx.firstBegSent = true;
                ctx.currentJitter = ctx.random.nextInt(6000) - 3000;
            }
        }
    }
}
