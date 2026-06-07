package com.example.begger.manager;

import com.example.begger.RankBegger;
import com.example.begger.context.BeggerContext;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;
import com.example.begger.util.ReflectionCache;
import com.example.begger.utils.WebhookUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.io.File;
import java.util.List;

public class GiftManager {

    private final BeggerContext ctx;
    private final RankDetector rankDetector;

    private int webhookScreenshotTicks = 0;
    private Runnable postScreenshotAction = null;
    private long lastWebhookTime = 0;

    private long lastBookAcceptClick = 0;
    private int bookClickCount = 0;

    public GiftManager(BeggerContext ctx, RankDetector rankDetector) {
        this.ctx = ctx;
        this.rankDetector = rankDetector;
    }

    public void findAndClickAccept(IChatComponent component) {
        if (component == null) return;
        if (component.getChatStyle() != null && component.getChatStyle().getChatClickEvent() != null) {
            ClickEvent clickEvent = component.getChatStyle().getChatClickEvent();
            if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                String val = clickEvent.getValue().toLowerCase();
                String text = component.getUnformattedText().toLowerCase();

                boolean isAccept = text.contains("yes") || val.contains("accept") || val.contains("gift");

                if (!isAccept && component.getChatStyle().getChatHoverEvent() != null) {
                    String hoverText = component.getChatStyle().getChatHoverEvent().getValue().getUnformattedText().toLowerCase();
                    if (hoverText.contains("accept") || hoverText.contains("yes")) {
                        isAccept = true;
                    }
                }

                if (val.contains("/guild") || val.contains("/party") || val.contains("/g accept") || val.contains("/p accept")) {
                    isAccept = false;
                }

                if (isAccept) {
                    McUtil.sendChat(clickEvent.getValue());
                    McUtil.sendChat("Thanks");
                }
            }
        }
        for (IChatComponent sibling : component.getSiblings()) {
            findAndClickAccept(sibling);
        }
    }

    public void handleChestGiftGui(GuiOpenEvent event) {
        if (event.gui instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) guiChest.inventorySlots;
            IInventory inv = container.getLowerChestInventory();
            String name = inv.getDisplayName().getUnformattedText();
            if (name.contains("wants to gift you") || name.contains("Rank Gift")) {
                rankDetector.parseGiftedRank(name);
                for (Slot slot : container.inventorySlots) {
                    if (slot.getHasStack() && slot.getStack().getDisplayName().contains("Yes")) {
                        postScreenshotAction = () -> {
                            McUtil.mc().playerController.windowClick(
                                    container.windowId, slot.slotNumber, 0, 0, McUtil.player());
                            McUtil.sendChat("Thanks");
                        };
                        if (isWebhookEnabled()) {
                            webhookScreenshotTicks = 10;
                        } else {
                            postScreenshotAction.run();
                            postScreenshotAction = null;
                        }
                        break;
                    }
                }
            }
        } else if (event.gui instanceof GuiScreenBook) {
            lastBookAcceptClick = System.currentTimeMillis() - 2000L;
        }
    }

    public void tick(long now) {
        if (webhookScreenshotTicks > 0) {
            webhookScreenshotTicks--;
            if (webhookScreenshotTicks == 0) {
                if (now - lastWebhookTime > 5000L) {
                    onGiftReceived();
                    lastWebhookTime = now;
                }
                if (postScreenshotAction != null) {
                    postScreenshotAction.run();
                    postScreenshotAction = null;
                }
            }
        }

        if (McUtil.mc().currentScreen instanceof GuiScreenBook) {
            if (now - lastBookAcceptClick > 3000L) {
                handleBookGui((GuiScreenBook) McUtil.mc().currentScreen);
                lastBookAcceptClick = now;
            }
        } else {
            bookClickCount = 0;
        }
    }

    private void handleBookGui(GuiScreenBook gui) {

        ItemStack bookStack = null;
        if (ReflectionCache.BOOK_STACK_FIELD != null) {
            try {
                bookStack = (ItemStack) ReflectionCache.BOOK_STACK_FIELD.get(gui);
            } catch (Exception e) {  }
        }

        if (bookStack != null) {
            NBTTagCompound nbt = bookStack.getTagCompound();
            if (nbt != null && nbt.hasKey("pages", 9)) {
                NBTTagList pages = nbt.getTagList("pages", 8);
                boolean isGift = false;
                for (int i = 0; i < pages.tagCount(); i++) {
                    String pageJson = pages.getStringTagAt(i);
                    if (pageJson.contains("wants to gift you") || pageJson.contains("Will you accept")) {
                        isGift = true;
                        IChatComponent component = IChatComponent.Serializer.jsonToComponent(pageJson);
                        rankDetector.parseGiftedRank(component.getUnformattedText());
                    }
                }
                if (!isGift) return;
            }
        }

        if (bookClickCount >= 3) {
            clickDoneButton(gui);
            bookClickCount = 0;
            return;
        }

        if (ReflectionCache.BOOK_GET_CHAT_COMPONENT != null) {
            ScaledResolution sr = new ScaledResolution(McUtil.mc());
            int width = sr.getScaledWidth();
            int height = sr.getScaledHeight();

            int targetX = -1;
            int targetY = -1;
            IChatComponent targetComp = null;

            for (int x = width / 2 - 80; x < width / 2 + 80; x += 2) {
                for (int y = height / 4; y < height * 3 / 4; y += 2) {
                    try {
                        IChatComponent comp = (IChatComponent) ReflectionCache.BOOK_GET_CHAT_COMPONENT.invoke(gui, x, y);
                        if (comp != null) {
                            String unformatted = comp.getUnformattedText().toLowerCase().trim();
                            if (unformatted.equals("yes") || unformatted.equals("[yes]") || unformatted.contains("accept")) {
                                targetX = x;
                                targetY = y;
                                targetComp = comp;
                                break;
                            }
                        }
                    } catch (Exception e) {  }
                }
                if (targetX != -1) break;
            }

            if (targetX != -1) {
                int realX = targetX * McUtil.mc().displayWidth / sr.getScaledWidth();
                int realY = McUtil.mc().displayHeight - (targetY * McUtil.mc().displayHeight / sr.getScaledHeight()) - 1;
                org.lwjgl.input.Mouse.setCursorPosition(realX, realY);

                boolean clicked = false;
                if (targetComp != null && targetComp.getChatStyle() != null && targetComp.getChatStyle().getChatClickEvent() != null) {
                    ClickEvent clickEvent = targetComp.getChatStyle().getChatClickEvent();
                    if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                        McUtil.sendChat(clickEvent.getValue());
                        clicked = true;
                    }
                }

                if (!clicked && ReflectionCache.SCREEN_MOUSE_CLICKED != null) {
                    try {
                        ReflectionCache.SCREEN_MOUSE_CLICKED.invoke(gui, targetX, targetY, 0);
                    } catch (Exception e) {  }
                }

                bookClickCount++;
                if (bookClickCount == 1) {
                    McUtil.sendChat("Thanks");
                    if (isWebhookEnabled()) {
                        webhookScreenshotTicks = 10;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void clickDoneButton(GuiScreen gui) {
        try {
            if (ReflectionCache.SCREEN_BUTTON_LIST != null) {
                List<GuiButton> buttonList = (List<GuiButton>) ReflectionCache.SCREEN_BUTTON_LIST.get(gui);

                for (GuiButton button : buttonList) {
                    if (button.displayString.toLowerCase().contains("done")) {
                        ScaledResolution sr = new ScaledResolution(McUtil.mc());
                        int realX = (button.xPosition + button.width / 2) * McUtil.mc().displayWidth / sr.getScaledWidth();
                        int realY = McUtil.mc().displayHeight - ((button.yPosition + button.height / 2) * McUtil.mc().displayHeight / sr.getScaledHeight()) - 1;
                        org.lwjgl.input.Mouse.setCursorPosition(realX, realY);

                        if (ReflectionCache.SCREEN_ACTION_PERFORMED != null) {
                            try {
                                ReflectionCache.SCREEN_ACTION_PERFORMED.invoke(gui, button);
                            } catch (Exception e) {  }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            McUtil.mc().displayGuiScreen(null);
        }
    }

    private void onGiftReceived() {
        RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
        if (mod == null) return;

        String url = WebhookUtil.normalizeWebhookUrl(mod.webhookUrl.getContent());
        if (!WebhookUtil.isConfigured(url)) return;

        long now = System.currentTimeMillis();
        long actualDuration = now - ctx.lastGiftAcceptedTime;

        long seconds = (actualDuration / 1000) % 60;
        long minutes = (actualDuration / (1000 * 60)) % 60;
        long hours = (actualDuration / (1000 * 60 * 60));

        String timeStr = String.format("%dh %dm %ds", hours, minutes, seconds);
        int msgs = ctx.messagesSinceLastGift;

        String content = "**Rank Begger - Gift Received!**\n" +
                "Time taken: `" + timeStr + "`\n" +
                "Messages sent: `" + msgs + "`";

        File screenshot = WebhookUtil.captureScreenshot();
        new Thread(() -> {
            try {
                WebhookUtil.sendWebhook(url, content, screenshot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        ctx.lastGiftAcceptedTime = now;
        ctx.messagesSinceLastGift = 0;
        ctx.targetUsername = null;
        ctx.idMessageSent = false;
        ctx.firstBegSent = false;
    }

    public void setWebhookScreenshotTicks(int ticks) {
        if (ticks > 0 && !isWebhookEnabled()) {
            return;
        }
        this.webhookScreenshotTicks = ticks;
    }

    private boolean isWebhookEnabled() {
        RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
        return mod != null && WebhookUtil.isConfigured(mod.webhookUrl.getContent());
    }
}
