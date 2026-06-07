package com.example.begger.manager;

import com.example.begger.Config;
import com.example.begger.context.BeggerContext;
import com.example.begger.data.MessageLoader;
import com.example.begger.system.Rank;

import java.util.ArrayList;
import java.util.List;

public class MessageGenerator {

    private final BeggerContext ctx;
    private final RankDetector rankDetector;
    private final List<String> giftResponseMessages;
    private final List<String> idMessages;

    public MessageGenerator(BeggerContext ctx, RankDetector rankDetector) {
        this.ctx = ctx;
        this.rankDetector = rankDetector;
        this.giftResponseMessages = MessageLoader.loadGiftResponses();
        this.idMessages = MessageLoader.loadIdMessages();
        loadRankMessages();
    }

    private void loadRankMessages() {
        List<String> baseMessages = MessageLoader.loadBegMessages();

        for (Rank r : Rank.values()) {
            if (r == Rank.MVP_PLUS_PLUS) continue;
            Rank targetRank = rankDetector.getTargetRank(r);
            List<String> msgs = new ArrayList<>();
            for (String base : baseMessages) {
                msgs.add(base.replace("VIP", targetRank.getName()).replace("[RANK]", targetRank.getName()));
            }
            Config.rankMessages.put(r, msgs);
        }

        Config.rankMessages.put(Rank.MVP_PLUS_PLUS, MessageLoader.loadMvpPlusPlusMessages());
    }

    public String getRandomBegMessage(Rank rank) {
        List<String> msgs = Config.rankMessages.get(rank);
        if (msgs != null && !msgs.isEmpty()) {
            int attempts = 0;
            while (attempts < 10) {
                String msg = msgs.get(ctx.random.nextInt(msgs.size()));
                long lastSent = ctx.messageHistory.getOrDefault(msg, 0L);
                if (System.currentTimeMillis() - lastSent > 60000L) {
                    return msg;
                }
                attempts++;
            }
        }
        return "";
    }

    public String getRandomGiftResponse(String gifterName, String nextRank) {
        int attempts = 0;
        while (attempts < 10) {
            String baseMsg = giftResponseMessages.get(ctx.random.nextInt(giftResponseMessages.size()));
            String chosenMsg = baseMsg.replace("[Player]", gifterName).replace("[Next rank]", nextRank);
            long lastSent = ctx.messageHistory.getOrDefault(chosenMsg, 0L);
            if (System.currentTimeMillis() - lastSent > 60000L) {
                return chosenMsg;
            }
            attempts++;
        }
        return "";
    }

    public String getRandomIdMessage() {
        return idMessages.get(ctx.random.nextInt(idMessages.size()));
    }
}
