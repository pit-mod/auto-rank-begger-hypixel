package com.example.begger.manager;

import com.example.begger.context.BeggerContext;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;

public class TypingSimulator {

    private final BeggerContext ctx;

    private String pendingMessage = null;
    private long pendingMessageSendTime = 0;

    public TypingSimulator(BeggerContext ctx) {
        this.ctx = ctx;
    }

    private long calculateTypingDelay(String message) {
        int len = message.length();
        long base = 0;
        for (int i = 0; i < len; i++) {
            base += 60 + ctx.random.nextInt(100);
        }
        base += 500 + ctx.random.nextInt(1000);
        return Math.min(base, 8000);
    }

    private String humanizeMessage(String message) {
        if (message == null || message.length() < 5) return message;

        int roll = ctx.random.nextInt(100);

        if (roll < 35) {
            return Character.toLowerCase(message.charAt(0)) + message.substring(1);
        } else if (roll < 50) {
            int idx = 3 + ctx.random.nextInt(Math.max(1, message.length() - 4));
            if (idx < message.length() - 1) {
                char[] chars = message.toCharArray();
                char tmp = chars[idx];
                chars[idx] = chars[idx + 1];
                chars[idx + 1] = tmp;
                return new String(chars);
            }
        } else if (roll < 55) {
            int idx = 3 + ctx.random.nextInt(Math.max(1, message.length() - 4));
            if (idx < message.length()) {
                return message.substring(0, idx) + message.charAt(idx) + message.substring(idx);
            }
        } else if (roll < 65) {
            int idx = 3 + ctx.random.nextInt(Math.max(1, message.length() - 4));
            if (idx < message.length()) {
                return message.substring(0, idx) + message.substring(idx + 1);
            }
        }
        return message;
    }

    public void sendHumanMessage(String message, RankBeggerModule mod) {
        String finalMsg = mod.humanizeText.isEnabled() ? humanizeMessage(message) : message;

        if (mod.simulateTyping.isEnabled()) {
            long delay = calculateTypingDelay(finalMsg);
            pendingMessage = finalMsg;
            pendingMessageSendTime = System.currentTimeMillis() + delay;
        } else {
            if (McUtil.hasPlayer()) {
                McUtil.sendChat(finalMsg);
                ctx.messageHistory.put(message, System.currentTimeMillis());
                ctx.messagesSent++;
                ctx.messagesSinceLastGift++;
                ctx.lastSentMessage = finalMsg;
            }
        }
    }

    public void tickPendingMessage(long now) {
        if (pendingMessage != null && now >= pendingMessageSendTime) {
            if (McUtil.hasPlayer()) {
                McUtil.sendChat(pendingMessage);
                ctx.messageHistory.put(pendingMessage, now);
                ctx.messagesSent++;
                ctx.messagesSinceLastGift++;
                ctx.lastSentMessage = pendingMessage;
            }
            pendingMessage = null;
            ctx.lastBegTime = now;
            ctx.firstBegSent = true;
            ctx.currentJitter = ctx.random.nextInt(6000) - 3000;
        }
    }

    public boolean hasPending() {
        return pendingMessage != null;
    }

    public String getPendingMessage() {
        return pendingMessage;
    }
}
