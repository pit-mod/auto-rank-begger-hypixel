package com.example.begger.manager;

import com.example.begger.context.BeggerContext;
import com.example.begger.context.FailsafeState;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;
import com.example.begger.utils.Timer;
import com.example.begger.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

public class FailsafeManager {

    private final BeggerContext ctx;

    private final Timer failsafeTimer = new Timer();
    private final Timer guiClickTimer = new Timer();
    private boolean limboSentOnce = false;

    public FailsafeManager(BeggerContext ctx) {
        this.ctx = ctx;
    }

    public void tick(RankBeggerModule mod) {
        String title = Utils.getScoreboardTitle();

        if (title.isEmpty() || (!title.toUpperCase().contains("BED WARS") && !title.equalsIgnoreCase("HYPIXEL"))) {
            ctx.reachedLobby1 = false;
            if (ctx.failsafeState != FailsafeState.LIMBO) {
                ctx.failsafeState = FailsafeState.LIMBO;
                failsafeTimer.reset();
                limboSentOnce = false;
            }
            long waitTime = limboSentOnce ? 10000L : 5000L;
            if (failsafeTimer.hasTimeElapsed(waitTime, false)) {
                McUtil.sendChat("/l");
                limboSentOnce = true;
                failsafeTimer.reset();
            }
        } else if (title.equalsIgnoreCase("HYPIXEL")) {
            ctx.reachedLobby1 = false;
            if (ctx.failsafeState == FailsafeState.BEGGING || ctx.failsafeState == FailsafeState.LIMBO) {
                ctx.failsafeState = FailsafeState.LOBBY_WAIT;
                failsafeTimer.reset();
            }
            if (ctx.failsafeState == FailsafeState.LOBBY_WAIT && failsafeTimer.hasTimeElapsed(3000L, false)) {
                int slot = findItemSlot("Game Menu", "Compass");
                McUtil.mc().thePlayer.inventory.currentItem = (slot != -1) ? slot : 0;
                ctx.failsafeState = FailsafeState.LOBBY_OPEN_COMPASS;
                failsafeTimer.reset();
            }
            if (ctx.failsafeState == FailsafeState.LOBBY_OPEN_COMPASS && failsafeTimer.hasTimeElapsed(1000L, false)) {
                rightClick();
                ctx.failsafeState = FailsafeState.LOBBY_SELECT_BW;
                failsafeTimer.reset();
            }
        } else if (isInBwLobby() && !ctx.reachedLobby1) {
            if (ctx.failsafeState != FailsafeState.BW_LOBBY_WAIT && ctx.failsafeState != FailsafeState.BW_OPEN_SELECTOR && ctx.failsafeState != FailsafeState.BW_SELECT_LOBBY1) {
                ctx.failsafeState = FailsafeState.BW_LOBBY_WAIT;
                failsafeTimer.reset();
            }
            if (ctx.failsafeState == FailsafeState.BW_LOBBY_WAIT && failsafeTimer.hasTimeElapsed(3000L, false)) {
                int slot = findItemSlot("Lobby Selector", "Nether Star");
                McUtil.mc().thePlayer.inventory.currentItem = (slot != -1) ? slot : 8;
                ctx.failsafeState = FailsafeState.BW_OPEN_SELECTOR;
                failsafeTimer.reset();
            }
            if (ctx.failsafeState == FailsafeState.BW_OPEN_SELECTOR && failsafeTimer.hasTimeElapsed(1000L, false)) {
                rightClick();
                ctx.failsafeState = FailsafeState.BW_SELECT_LOBBY1;
                failsafeTimer.reset();
            }
        } else if (ctx.failsafeState != FailsafeState.BEGGING) {
            if (!(title.toUpperCase().contains("BED WARS") && isInBwLobby())) {
                ctx.failsafeState = FailsafeState.BEGGING;
            }
        }
    }

    public void handleGuiClicks() {
        if (McUtil.mc().currentScreen instanceof GuiChest && guiClickTimer.hasTimeElapsed(500L, true)) {
            GuiChest guiChest = (GuiChest) McUtil.mc().currentScreen;
            ContainerChest container = (ContainerChest) guiChest.inventorySlots;
            IInventory inv = container.getLowerChestInventory();
            String name = inv.getDisplayName().getUnformattedText();
            if (ctx.failsafeState == FailsafeState.LOBBY_SELECT_BW) {
                for (Slot slot : container.inventorySlots) {
                    if (slot.getHasStack()) {
                        String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName()).toUpperCase();
                        if (itemName.contains("BED WARS")) {
                            McUtil.mc().playerController.windowClick(container.windowId, slot.slotNumber, 0, 0, McUtil.player());
                            return;
                        }
                    }
                }
            } else if (ctx.failsafeState == FailsafeState.BW_SELECT_LOBBY1) {
                String cleanName = StringUtils.stripControlCodes(name).toUpperCase();
                if (cleanName.contains("LOBBY SELECTOR") || cleanName.contains("BED WARS")) {
                    McUtil.mc().playerController.windowClick(container.windowId, 0, 0, 0, McUtil.player());
                    ctx.reachedLobby1 = true;
                    ctx.failsafeState = FailsafeState.BEGGING;
                }
            }
        }
    }

    private int findItemSlot(String namePart, String typePart) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = McUtil.mc().thePlayer.inventory.mainInventory[i];
            if (stack != null) {
                String name = StringUtils.stripControlCodes(stack.getDisplayName()).toUpperCase();
                if (name.contains(namePart.toUpperCase())) return i;
            }
        }
        return -1;
    }

    private boolean isInBwLobby() {
        if (!McUtil.hasPlayer()) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = McUtil.mc().thePlayer.inventory.mainInventory[i];
            if (stack != null) {
                String name = StringUtils.stripControlCodes(stack.getDisplayName());
                if (stack.getItem() == Items.bed || name.contains("Slumber Inventory") || name.contains("Bed Wars")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void rightClick() {
        int key = McUtil.mc().gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.onTick(key);
    }
}
