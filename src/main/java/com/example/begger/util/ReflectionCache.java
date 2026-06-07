package com.example.begger.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Caches reflection lookups that would otherwise be scanned every tick.
 * All fields/methods are resolved once at class-load time.
 */
public final class ReflectionCache {

    private ReflectionCache() {}

    // ── GuiScreenBook: ItemStack field ────────────────────────────
    public static final Field BOOK_STACK_FIELD;

    // ── GuiScreenBook: getChatComponent(int, int) method ─────────
    public static final Method BOOK_GET_CHAT_COMPONENT;

    // ── GuiScreen: mouseClicked(int, int, int) method ────────────
    public static final Method SCREEN_MOUSE_CLICKED;

    // ── GuiScreen: buttonList field ──────────────────────────────
    public static final Field SCREEN_BUTTON_LIST;

    // ── GuiScreen: actionPerformed(GuiButton) method ─────────────
    public static final Method SCREEN_ACTION_PERFORMED;

    static {
        // --- GuiScreenBook.bookStack (ItemStack field) ---
        Field bookField = null;
        for (Field f : GuiScreenBook.class.getDeclaredFields()) {
            if (f.getType() == ItemStack.class) {
                f.setAccessible(true);
                bookField = f;
                break;
            }
        }
        BOOK_STACK_FIELD = bookField;

        // --- GuiScreenBook.getChatComponent(int, int) ---
        Method chatCompMethod = null;
        for (Method m : GuiScreenBook.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 2
                && m.getParameterTypes()[0] == int.class
                && m.getParameterTypes()[1] == int.class
                && IChatComponent.class.isAssignableFrom(m.getReturnType())) {
                m.setAccessible(true);
                chatCompMethod = m;
                break;
            }
        }
        BOOK_GET_CHAT_COMPONENT = chatCompMethod;

        // --- GuiScreen.mouseClicked(int, int, int) ---
        Method mouseMethod = null;
        for (Method m : GuiScreen.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 3
                && m.getParameterTypes()[0] == int.class
                && m.getParameterTypes()[1] == int.class
                && m.getParameterTypes()[2] == int.class) {
                m.setAccessible(true);
                mouseMethod = m;
                break;
            }
        }
        SCREEN_MOUSE_CLICKED = mouseMethod;

        // --- GuiScreen.buttonList (List field) ---
        Field btnField = null;
        for (Field f : GuiScreen.class.getDeclaredFields()) {
            if (f.getType() == List.class) {
                f.setAccessible(true);
                btnField = f;
                break;
            }
        }
        SCREEN_BUTTON_LIST = btnField;

        // --- GuiScreen.actionPerformed(GuiButton) ---
        Method actionMethod = null;
        for (Method m : GuiScreen.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 1
                && m.getParameterTypes()[0] == GuiButton.class) {
                m.setAccessible(true);
                actionMethod = m;
                break;
            }
        }
        SCREEN_ACTION_PERFORMED = actionMethod;
    }
}
