package com.example.begger.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Static helpers to reduce Minecraft boilerplate across all managers.
 */
public final class McUtil {

    private McUtil() {}

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP player() {
        return mc().thePlayer;
    }

    public static boolean hasPlayer() {
        return mc().thePlayer != null;
    }

    public static void sendChat(String msg) {
        if (hasPlayer()) player().sendChatMessage(msg);
    }

    public static void addClientMessage(String prefix, String msg) {
        if (hasPlayer()) {
            player().addChatMessage(new ChatComponentText(
                EnumChatFormatting.GRAY + "[" + prefix + "] " + msg));
        }
    }
}
