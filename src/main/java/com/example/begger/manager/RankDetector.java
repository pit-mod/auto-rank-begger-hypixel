package com.example.begger.manager;

import com.example.begger.context.BeggerContext;
import com.example.begger.system.Rank;
import com.example.begger.util.McUtil;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class RankDetector {

    private final BeggerContext ctx;

    public RankDetector(BeggerContext ctx) {
        this.ctx = ctx;
    }

    public void parseRank(String text) {
        String clean = StringUtils.stripControlCodes(text).toUpperCase();
        if (clean.contains("[MVP++]")) ctx.currentRank = Rank.MVP_PLUS_PLUS;
        else if (clean.contains("[MVP+]")) ctx.currentRank = Rank.MVP_PLUS;
        else if (clean.contains("[MVP]")) ctx.currentRank = Rank.MVP;
        else if (clean.contains("[VIP+]")) ctx.currentRank = Rank.VIP_PLUS;
        else if (clean.contains("[VIP]")) ctx.currentRank = Rank.VIP;
        else ctx.currentRank = Rank.NON;
    }

    public void parseGiftedRank(String text) {
        String clean = StringUtils.stripControlCodes(text).toUpperCase();
        int idx = clean.indexOf("WANTS TO GIFT YOU ");
        if (idx != -1) {
            String after = clean.substring(idx + "WANTS TO GIFT YOU ".length());
            if (after.startsWith("MVP++")) ctx.currentRank = Rank.MVP_PLUS_PLUS;
            else if (after.startsWith("MVP+")) ctx.currentRank = Rank.MVP_PLUS;
            else if (after.startsWith("MVP")) ctx.currentRank = Rank.MVP;
            else if (after.startsWith("VIP+")) ctx.currentRank = Rank.VIP_PLUS;
            else if (after.startsWith("VIP")) ctx.currentRank = Rank.VIP;
        }
    }

    public void updateCurrentRankFromScoreboard() {
        if (McUtil.mc().theWorld == null || ctx.targetUsername == null) return;
        Scoreboard sb = McUtil.mc().theWorld.getScoreboard();
        String fullText = "";

        ScorePlayerTeam team = sb.getPlayersTeam(ctx.targetUsername);
        if (team != null) {
            fullText += team.getColorPrefix() + " " + team.getColorSuffix();
        }

        if (McUtil.mc().getNetHandler() != null && McUtil.mc().getNetHandler().getPlayerInfoMap() != null) {
            for (NetworkPlayerInfo info : McUtil.mc().getNetHandler().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equalsIgnoreCase(ctx.targetUsername)) {
                    if (info.getDisplayName() != null) {
                        fullText += " " + info.getDisplayName().getUnformattedText();
                    }
                    break;
                }
            }
        }

        if (!fullText.trim().isEmpty()) {
            parseRank(fullText);
        }
    }

    public Rank getTargetRank(Rank current) {
        switch (current) {
            case NON: return Rank.VIP;
            case VIP: return Rank.VIP_PLUS;
            case VIP_PLUS: return Rank.MVP;
            case MVP: return Rank.MVP_PLUS;
            case MVP_PLUS: return Rank.MVP_PLUS_PLUS;
            case MVP_PLUS_PLUS: return Rank.MVP_PLUS_PLUS;
            default: return Rank.VIP;
        }
    }
}
