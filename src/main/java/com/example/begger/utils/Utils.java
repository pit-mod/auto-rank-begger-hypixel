package com.example.begger.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class Utils {
    public static String getScoreboardTitle() {
        if (Minecraft.getMinecraft().theWorld == null) return "";
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return "";
        return StringUtils.stripControlCodes(objective.getDisplayName());
    }
}
