package com.example.begger.manager;

import com.example.begger.RankBegger;
import com.example.begger.context.BeggerContext;
import com.example.begger.context.FailsafeState;
import com.example.begger.system.impl.RankBeggerModule;
import com.example.begger.util.McUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import com.example.begger.utils.RenderUtil;
import com.example.begger.ui.font.FontManager;
import com.example.begger.ui.font.CustomFontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class HudRenderer {

    private final BeggerContext ctx;

    public HudRenderer(BeggerContext ctx) {
        this.ctx = ctx;
    }

    public void render(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        RankBeggerModule mod = RankBegger.moduleManager.getModuleByClass(RankBeggerModule.class);
        if (mod == null || !mod.toggled || !mod.showHud.isEnabled()) return;

        String design = mod.hudDesign.getSelected();
        if (design.equalsIgnoreCase("Nebula")) {
            renderNebula(event, mod);
        } else if (design.equalsIgnoreCase("Taunahi")) {
            renderTaunahi(event, mod);
        } else if (design.equalsIgnoreCase("Polar")) {
            renderPolar(event, mod);
        } else if (design.equalsIgnoreCase("Polinex")) {
            renderPolinex(event, mod);
        } else if (design.equalsIgnoreCase("Wielix")) {
            renderWielix(event, mod);
        }
    }

    private static final ResourceLocation POLAR_LOGO = new ResourceLocation("rankbegger", "polar_logo.png");
    private static final ResourceLocation POLINEX_LOGO = new ResourceLocation("rankbegger", "polinex_logo.png");
    private static final ResourceLocation WIELIX_LOGO = new ResourceLocation("logo", "wielix logo.png");

    private void renderPolar(RenderGameOverlayEvent.Post event, RankBeggerModule mod) {
        long now = System.currentTimeMillis();
        String sessionTime = formatSessionTime(now - ctx.sessionStartTime);

        float x = (float) mod.hudX.getValue();
        float y = (float) mod.hudY.getValue();

        CustomFontRenderer fontSmall = FontManager.inter15;
        CustomFontRenderer fontBig = FontManager.inter18;

        if (fontSmall == null || fontBig == null) {
            if (RankBegger.fontManager != null) RankBegger.fontManager.init();
            fontSmall = FontManager.inter15;
            fontBig = FontManager.inter18;
            if (fontSmall == null || fontBig == null) return;
        }

        class PolarLine {
            String text;
            CustomFontRenderer font;
            int color;
            boolean isSeparator;
            PolarLine(String text, CustomFontRenderer font, int color) {
                this.text = text; this.font = font; this.color = color; this.isSeparator = false;
            }
            PolarLine() { this.isSeparator = true; }
        }

        java.util.List<PolarLine> lines = new java.util.ArrayList<>();
        lines.add(new PolarLine("Rank Begger", fontBig, 0xFFFFFFFF));
        lines.add(new PolarLine("Uptime: " + sessionTime, fontSmall, 0xFFDDDDDD));
        lines.add(new PolarLine("Please remain attended at all times!", fontSmall, 0xFFDDDDDD));
        lines.add(new PolarLine());

        lines.add(new PolarLine("Status", fontBig, 0xFFFFFFFF));
        String failsafeText = ctx.failsafeState == FailsafeState.BEGGING ? "Running" : ctx.failsafeState.name();
        int statusColor = ctx.failsafeState == FailsafeState.BEGGING ? 0xFF55FF55 : 0xFFFF5555;
        lines.add(new PolarLine("Begger: " + failsafeText, fontSmall, statusColor));
        boolean gifterActive = (now - ctx.lastGiftTime) <= 120000;
        lines.add(new PolarLine("Gifter: " + (gifterActive ? "§aActive" : "§cInactive"), fontSmall, 0xFFFFFFFF));
        lines.add(new PolarLine());

        lines.add(new PolarLine("Session Stats", fontBig, 0xFFFFFFFF));
        lines.add(new PolarLine("Messages Sent: " + formatNumber(ctx.messagesSent), fontSmall, 0xFFDDDDDD));
        lines.add(new PolarLine("Current Rank: " + ctx.currentRank.getName(), fontSmall, 0xFFDDDDDD));
        lines.add(new PolarLine());

        lines.add(new PolarLine("Target Info", fontBig, 0xFFFFFFFF));
        lines.add(new PolarLine("Target: " + (ctx.targetUsername != null ? ctx.targetUsername : "N/A"), fontSmall, 0xFFDDDDDD));

        float width = 145;
        float footerH = 28;
        float height = 12;
        for (PolarLine pl : lines) {
            if (pl.isSeparator) height += 6;
            else height += pl.font.getFontHeight() + 2;
        }
        height += footerH + 4;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.disableTexture2D();

        for (int i = 1; i <= 8; i++) {
            RenderUtil.drawRoundedRect(x - (i * 1.0f), y - (i * 1.0f), width + (i * 2.0f), height + (i * 2.0f), 8, (14 - i) << 24 | 0x00FFFF);
        }

        RenderUtil.drawRoundedRect(x, y, width, height, 8, 0xDD080808);

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        float drawY = y + 10;
        for (PolarLine pl : lines) {
            if (pl.isSeparator) {

                RenderUtil.drawRect(x + 10, drawY + 2, width - 20, 0.5f, 0x40888888);
                drawY += 8;
            } else {
                float textX = x + (width - pl.font.getStringWidth(pl.text)) / 2f;
                pl.font.drawStringWithShadow(pl.text, textX, drawY, pl.color);
                drawY += pl.font.getFontHeight() + 2;
            }
        }

        float logoSize = 20;
        String polarName = "Polar";
        String polarVer = " v3.2.11";
        float nameW = fontBig.getStringWidth(polarName);
        float verW = fontSmall.getStringWidth(polarVer);
        float footerContentW = logoSize + 6 + nameW + verW;
        float footerX = x + (width - footerContentW) / 2f;
        float footerY = y + height - footerH + (footerH - logoSize) / 2f - 3;

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        drawTexture(POLAR_LOGO, footerX, footerY, logoSize, logoSize);

        fontBig.drawStringWithShadow(polarName, footerX + logoSize + 6, footerY + (logoSize - fontBig.getFontHeight()) / 2f + 1, 0xFFFFFFFF);
        fontSmall.drawStringWithShadow(polarVer, footerX + logoSize + 6 + nameW, footerY + (logoSize - fontSmall.getFontHeight()) / 2f + 2, 0xFFAAAAAA);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

    }

    private static class PolinexField {
        String label;
        String value;
        int color;
        PolinexField(String l, String v, int c) { label = l; value = v; color = c; }
    }

    private static class PolinexSection {
        String title;
        java.util.List<PolinexField> fields = new java.util.ArrayList<>();
        boolean hasProgressBar = false;
        float progress = 0;
        PolinexSection(String t) { title = t; }
    }

    private void renderPolinex(RenderGameOverlayEvent.Post event, RankBeggerModule mod) {
        long now = System.currentTimeMillis();
        float x = (float) mod.hudX.getValue();
        float y = (float) mod.hudY.getValue();
        float width = 140;

        CustomFontRenderer fontSmall = FontManager.inter15;
        CustomFontRenderer fontBig = FontManager.inter18;

        if (fontSmall == null || fontBig == null) {
            if (RankBegger.fontManager != null) RankBegger.fontManager.init();
            fontSmall = FontManager.inter15;
            fontBig = FontManager.inter18;
            if (fontSmall == null || fontBig == null) return;
        }

        java.util.List<PolinexSection> sections = new java.util.ArrayList<>();

        PolinexSection header = new PolinexSection("Rank Begger");
        sections.add(header);

        PolinexSection session = new PolinexSection("SESSION INFO");
        session.fields.add(new PolinexField("Status:", ctx.failsafeState == FailsafeState.BEGGING ? "Running" : ctx.failsafeState.name(),
                                           ctx.failsafeState == FailsafeState.BEGGING ? 0xFF55FF55 : 0xFFFF5555));
        session.fields.add(new PolinexField("Rank:", ctx.currentRank.getName(), 0xFFFFFFFF));
        sections.add(session);

        PolinexSection stats = new PolinexSection("BEGGER INFO");
        stats.fields.add(new PolinexField("Messages Sent:", formatNumber(ctx.messagesSent), 0xFFFFFFFF));
        stats.fields.add(new PolinexField("Target:", ctx.targetUsername != null ? ctx.targetUsername : "N/A", 0xFF55FFFF));
        sections.add(stats);

        PolinexSection lastMsg = new PolinexSection("LAST MESSAGE");
        String lastText = ctx.lastSentMessage != null ? ctx.lastSentMessage : "None";

        java.util.List<String> wrappedLines = fontSmall.listFormattedStringToWidth(lastText, (int)width - 30);
        for (String line : wrappedLines) {
            lastMsg.fields.add(new PolinexField("", line, 0xFFFFFFFF));
        }
        sections.add(lastMsg);

        long uptimeMs = now - ctx.sessionStartTime;

        PolinexSection footer = new PolinexSection("Polinex 1.3.9");
        footer.fields.add(new PolinexField("Running:", formatSessionTime(uptimeMs), 0xFFAAAAAA));
        sections.add(footer);

        float totalH = 0;
        for (int i = 0; i < sections.size(); i++) {
            PolinexSection sec = sections.get(i);
            float h = (i == 0) ? 20 : 20;
            if (i > 0) {
                for (PolinexField f : sec.fields) h += fontSmall.getFontHeight() + 4;
                if (sec.hasProgressBar) h += 16;
                h += 8;
            }
            totalH += h + 4;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        RenderUtil.drawRoundedRect(x - 4, y - 4, width + 8, totalH + 4, 6, 0xB010162B);

        float currentY = y;

        for (int i = 0; i < sections.size(); i++) {
            PolinexSection sec = sections.get(i);
            boolean isFirst = (i == 0);
            boolean isLast = (i == sections.size() - 1);

            float secH = 20;
            if (!isFirst) {
                for (PolinexField f : sec.fields) secH += fontSmall.getFontHeight() + 4;
                if (sec.hasProgressBar) secH += 16;
                secH += 8;
            }

            GlStateManager.disableTexture2D();
            RenderUtil.drawRoundedRect(x, currentY, width, secH, 4, 0xE610162B);
            GlStateManager.enableBlend();

            GlStateManager.enableTexture2D();

            float innerY = currentY + 3;

            if (isFirst) {

                fontBig.drawStringWithShadow("▶ " + sec.title, x + 10, innerY + (secH - 6 - fontBig.getFontHeight()) / 2f + 1, 0xFF7777BB);
            } else if (isLast) {

                float logoS = 18;
                drawTexture(POLINEX_LOGO, x + 10, innerY + 2, logoS, logoS);
                fontBig.drawStringWithShadow("Polinex 1.3.9", x + 10 + logoS + 6, innerY + 1, 0xFFFFFFFF);
                innerY += 18;
            } else {
                String icon = "■";
                if (sec.title.contains("SESSION")) icon = "📊";
                if (sec.title.contains("BEGGER")) icon = "⛏";
                if (sec.title.contains("MESSAGE")) icon = "💬";

                fontSmall.drawStringWithShadow(icon + " " + sec.title, x + 10, innerY, 0xFF555577);
                innerY += fontSmall.getFontHeight() + 4;
            }

            if (!isFirst) {
                for (PolinexField f : sec.fields) {
                    if (isLast) {
                        fontSmall.drawString(f.label + " " + f.value, x + 10 + 24, innerY, f.color);
                    } else if (f.label.isEmpty()) {

                        fontSmall.drawString(f.value, x + 10, innerY, f.color);
                    } else {
                        fontSmall.drawString(f.label, x + 10, innerY, 0xFF777777);
                        float valX = x + width - 10 - fontSmall.getStringWidth(f.value);
                        fontSmall.drawString(f.value, valX, innerY, f.color);
                    }
                    innerY += fontSmall.getFontHeight() + 4;
                }

                if (sec.hasProgressBar) {
                    float barW = width - 16;
                    float barH = 6;
                    float barX = x + 8;
                    float barY = innerY + 2;
                    GlStateManager.disableTexture2D();
                    drawRoundedRect(barX, barY, barW, barH, 2, 0x40000000);
                    drawRoundedRect(barX, barY, barW * sec.progress, barH, 2, 0xFF5555FF);
                    GlStateManager.enableTexture2D();
                }
            }

            currentY += secH + 4;
        }

        GlStateManager.popMatrix();
    }

    private void drawTexture(ResourceLocation loc, float x, float y, float width, float height) {
        McUtil.mc().getTextureManager().bindTexture(loc);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, (int)width, (int)height, width, height);
    }

    private void renderTaunahi(RenderGameOverlayEvent.Post event, RankBeggerModule mod) {
        long now = System.currentTimeMillis();
        String sessionTime = formatSessionTime(now - ctx.sessionStartTime);

        float x = (float) mod.hudX.getValue();
        float y = (float) mod.hudY.getValue();

        CustomFontRenderer fontSmall = FontManager.productSans18;
        CustomFontRenderer fontBig = FontManager.productSans20;

        if (fontSmall == null || fontBig == null) {
            if (RankBegger.fontManager != null) RankBegger.fontManager.init();
            fontSmall = FontManager.productSans18;
            fontBig = FontManager.productSans20;
            if (fontSmall == null || fontBig == null) return;
        }

        class HudLine {
            String text;
            CustomFontRenderer font;
            boolean isSeparator;
            HudLine(String text, CustomFontRenderer font) { this.text = text; this.font = font; this.isSeparator = false; }
            HudLine() { this.isSeparator = true; }
        }

        java.util.List<HudLine> lines = new java.util.ArrayList<>();
        lines.add(new HudLine("§aRank Begger (" + sessionTime + ")", fontBig));
        lines.add(new HudLine("§7Messages Sent: §f" + formatNumber(ctx.messagesSent), fontSmall));
        boolean gifterActive = (now - ctx.lastGiftTime) <= 120000;
        lines.add(new HudLine("§7Gifter Active: " + (gifterActive ? "§aYES" : "§cNO"), fontSmall));
        lines.add(new HudLine());

        lines.add(new HudLine("§aTarget Info", fontBig));
        lines.add(new HudLine("§7Target: §f" + (ctx.targetUsername != null ? ctx.targetUsername : "N/A"), fontSmall));
        String rColor = getRankColor(ctx.currentRank);
        lines.add(new HudLine("§7Current Rank: " + rColor + ctx.currentRank.getName(), fontSmall));
        lines.add(new HudLine());

        lines.add(new HudLine("§aLast Message", fontBig));
        String lastMsg = ctx.lastSentMessage != null ? ctx.lastSentMessage : "None";
        java.util.List<String> wrapped = fontSmall.listFormattedStringToWidth(lastMsg, 160);
        for (String s : wrapped) {
            lines.add(new HudLine("§f" + s, fontSmall));
        }

        float width = 0;
        float height = 10;
        for (HudLine hl : lines) {
            if (hl.isSeparator) {
                height += 8;
            } else {
                width = Math.max(width, hl.font.getStringWidth(hl.text));
                height += hl.font.getFontHeight() + 2;
            }
        }
        width += 12;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderUtil.drawRect(x, y, width, height, 0x90000000);
        RenderUtil.drawRect(x, y, width, 1.5f, 0xFF55FF55);
        RenderUtil.drawRect(x, y + height - 1.5f, width, 1.5f, 0xFF55FF55);

        GlStateManager.enableTexture2D();

        float drawY = y + 5;
        for (HudLine hl : lines) {
            if (hl.isSeparator) {
                RenderUtil.drawRect(x + 5, drawY + 3, width - 10, 0.5f, 0x40888888);
                drawY += 8;
            } else {
                hl.font.drawStringWithShadow(hl.text, x + 6, drawY, 0xFFFFFFFF);
                drawY += hl.font.getFontHeight() + 2;
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private String getRankColor(com.example.begger.system.Rank rank) {
        if (rank == null) return "§7";
        switch (rank) {
            case VIP:
            case VIP_PLUS:
                return "§a";
            case MVP:
            case MVP_PLUS:
                return "§b";
            case MVP_PLUS_PLUS:
                return "§6";
            case NON:
            default:
                return "§7";
        }
    }

    private void renderNebula(RenderGameOverlayEvent.Post event, RankBeggerModule mod) {
        FontRenderer fr = McUtil.mc().fontRendererObj;
        String lastMsg = ctx.lastSentMessage != null ? ctx.lastSentMessage : "None";

        java.util.List<String> msgLines = fr.listFormattedStringToWidth(lastMsg, 135);

        float x = (float) mod.hudX.getValue();
        float y = (float) mod.hudY.getValue();
        float width = 190;
        float height = 175;
        float radius = 10;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();

        GL11.glShadeModel(GL11.GL_SMOOTH);

        for (int i = 8; i >= 1; i--) {
            int alpha = 10 + (8 - i) * 3;
            int color = (alpha << 24) | 0x8A5CF5;
            float exp = i * 1.5f;
            drawRoundedRect(x - exp, y - exp, width + exp * 2, height + exp * 2, radius + (exp / 2), color);
        }

        drawRoundedRect(x, y, width, height, radius, 0xE6111111);

        drawRoundedRectOutline(x, y, width, height, radius, 1.0f, 0x4D8A5CF5);

        float iconCx = x + 28;
        float iconCy = y + 28;

        drawSolidCircle(iconCx, iconCy, 14, 0x33000000);

        long now = System.currentTimeMillis();
        float progress = 0f;
        long delayMs = (long) (mod.delay.getValue() * 1000) + ctx.currentJitter;
        if (ctx.firstBegSent && ctx.failsafeState == FailsafeState.BEGGING) {
            long elapsed = now - ctx.lastBegTime;
            progress = Math.min(1.0f, Math.max(0f, (float) elapsed / delayMs));
        } else if (!ctx.firstBegSent && ctx.failsafeState == FailsafeState.BEGGING) {
            progress = 1.0f;
        }
        drawProgressCircle(iconCx, iconCy, 14, 2.0f, 1.0f, 0x338A5CF5);
        drawProgressCircle(iconCx, iconCy, 14, 2.0f, progress, 0xFF8A5CF5);

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.enableGUIStandardItemLighting();
        McUtil.mc().getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.nether_star), (int) iconCx - 8, (int) iconCy - 8);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

        drawScaledString(fr, "Rank Begger", x + 50, y + 18, 0xFF8A5CF5, 1.0f);
        String failsafeText = ctx.failsafeState == FailsafeState.BEGGING ? "Active" : ctx.failsafeState.name();
        drawScaledString(fr, failsafeText, x + 50, y + 31, 0xFFAAAAAA, 1.0f);

        float rightAlignX = x + width - 12;
        float heroY = y + 55;
        float gridY1 = heroY;
        float gridY2 = heroY + 40;
        float gridY3 = heroY + 80;

        drawScaledString(fr, "MESSAGES SENT", x + 12, gridY1, 0xFFAAAAAA, 1.0f);
        drawScaledString(fr, formatNumber(ctx.messagesSent), x + 12, gridY1 + 12, 0xFFFFFFFF, 2.0f);

        drawScaledStringRight(fr, "TARGET", rightAlignX, gridY1, 0xFFAAAAAA, 1.0f);
        String target = ctx.targetUsername != null ? ctx.targetUsername : "N/A";
        drawScaledStringRight(fr, target, rightAlignX, gridY1 + 12, 0xFFFFFFFF, 1.0f);

        drawScaledString(fr, "GIFTER ACTIVE", x + 12, gridY2, 0xFFAAAAAA, 1.0f);
        boolean gifterActive = (now - ctx.lastGiftTime) <= 120000;
        drawScaledString(fr, gifterActive ? "YES" : "NO", x + 12, gridY2 + 12, 0xFFFFFFFF, 1.0f);

        drawScaledStringRight(fr, "CURRENT RANK", rightAlignX, gridY2, 0xFFAAAAAA, 1.0f);
        drawScaledStringRight(fr, ctx.currentRank.getName(), rightAlignX, gridY2 + 12, 0xFFFFFFFF, 1.0f);

        drawScaledString(fr, "SESSION TIME", x + 12, gridY3, 0xFFAAAAAA, 1.0f);
        drawScaledString(fr, formatSessionTime(now - ctx.sessionStartTime), x + 12, gridY3 + 12, 0xFFFFFFFF, 1.0f);

        drawScaledStringRight(fr, "LAST MESSAGE", rightAlignX, gridY3, 0xFFAAAAAA, 1.0f);

        float msgY = gridY3 + 12;
        float msgScale = 0.85f;
        for (String line : msgLines) {
            drawScaledStringRight(fr, line, rightAlignX, msgY, 0xFFFFFFFF, msgScale);
            msgY += (fr.FONT_HEIGHT * msgScale) + 2;
        }

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawScaledString(FontRenderer fr, String text, float x, float y, int color, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        fr.drawString(text, 0, 0, color, false);
        GlStateManager.popMatrix();
    }

    private void drawScaledStringRight(FontRenderer fr, String text, float rightX, float y, int color, float scale) {
        GlStateManager.pushMatrix();
        float width = fr.getStringWidth(text) * scale;
        GlStateManager.translate(rightX - width, y, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        fr.drawString(text, 0, 0, color, false);
        GlStateManager.popMatrix();
    }

    private String formatNumber(double value) {
        if (value >= 1000000) {
            return String.format("%.2fM", value / 1000000.0);
        } else if (value >= 1000) {
            return String.format("%.1fk", value / 1000.0);
        } else {
            return String.valueOf((int) value);
        }
    }

    private String formatSessionTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        return minutes + "m " + seconds + "s";
    }

    private void drawSolidCircle(float cx, float cy, float radius, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(cx, cy);
        for(int i = 0; i <= 360; i += 5) {
            GL11.glVertex2d(cx + Math.cos(Math.toRadians(i)) * radius, cy + Math.sin(Math.toRadians(i)) * radius);
        }
        GL11.glEnd();
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f, f1, f2, f3);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + width / 2, y + height / 2);

        for(int i = 180; i <= 270; i += 5) {
            GL11.glVertex2d(x + radius + Math.cos(Math.toRadians(i)) * radius, y + radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 270; i <= 360; i += 5) {
            GL11.glVertex2d(x + width - radius + Math.cos(Math.toRadians(i)) * radius, y + radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 0; i <= 90; i += 5) {
            GL11.glVertex2d(x + width - radius + Math.cos(Math.toRadians(i)) * radius, y + height - radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 90; i <= 180; i += 5) {
            GL11.glVertex2d(x + radius + Math.cos(Math.toRadians(i)) * radius, y + height - radius + Math.sin(Math.toRadians(i)) * radius);
        }
        GL11.glVertex2d(x + radius + Math.cos(Math.toRadians(180)) * radius, y + radius + Math.sin(Math.toRadians(180)) * radius);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    private void drawRoundedRectOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        GlStateManager.color(f, f1, f2, f3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(thickness);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for(int i = 180; i <= 270; i += 5) {
            GL11.glVertex2d(x + radius + Math.cos(Math.toRadians(i)) * radius, y + radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 270; i <= 360; i += 5) {
            GL11.glVertex2d(x + width - radius + Math.cos(Math.toRadians(i)) * radius, y + radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 0; i <= 90; i += 5) {
            GL11.glVertex2d(x + width - radius + Math.cos(Math.toRadians(i)) * radius, y + height - radius + Math.sin(Math.toRadians(i)) * radius);
        }
        for(int i = 90; i <= 180; i += 5) {
            GL11.glVertex2d(x + radius + Math.cos(Math.toRadians(i)) * radius, y + height - radius + Math.sin(Math.toRadians(i)) * radius);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private void drawProgressCircle(float cx, float cy, float radius, float thickness, float progress, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        GlStateManager.color(f, f1, f2, f3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(thickness);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        int maxAngle = (int) (360 * progress);
        for(int i = 0; i <= maxAngle; i += 5) {
            GL11.glVertex2d(cx + Math.cos(Math.toRadians(i - 90)) * radius, cy + Math.sin(Math.toRadians(i - 90)) * radius);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private void renderWielix(RenderGameOverlayEvent.Post event, RankBeggerModule mod) {
        long now = System.currentTimeMillis();
        float x = (float) mod.hudX.getValue();
        float y = (float) mod.hudY.getValue();
        float width = 240;

        CustomFontRenderer fontSmall = FontManager.wielixFont15;
        CustomFontRenderer fontBig = FontManager.wielixFont26;
        CustomFontRenderer fontContent = FontManager.wielixFont20;

        if (fontSmall == null || fontBig == null || fontContent == null) {
            if (RankBegger.fontManager != null) RankBegger.fontManager.init();
            fontSmall = FontManager.wielixFont15;
            fontBig = FontManager.wielixFont26;
            fontContent = FontManager.wielixFont20;
            if (fontSmall == null || fontBig == null || fontContent == null) return;
        }

        java.util.List<PolinexField> fields = new java.util.ArrayList<>();
        fields.add(new PolinexField("Status", ctx.failsafeState == FailsafeState.BEGGING ? "Running" : "Paused", ctx.failsafeState == FailsafeState.BEGGING ? 0xFF55FF55 : 0xFFFF5555));
        fields.add(new PolinexField("Current Rank", ctx.currentRank != null ? ctx.currentRank.getName() : "None", 0xFFC8DCFF));
        fields.add(new PolinexField("Messages Sent", formatNumber(ctx.messagesSent), 0xFFC8DCFF));
        fields.add(new PolinexField("Target", ctx.targetUsername != null ? ctx.targetUsername : "N/A", 0xFF55FFFF));

        String lastText = ctx.lastSentMessage != null ? ctx.lastSentMessage : "None";
        java.util.List<String> wrappedLines = fontSmall.listFormattedStringToWidth(lastText, (int)width - 32);

        float headH = 38;
        float footH = 30;
        float contentH = (fields.size() * (fontContent.getFontHeight() + 8)) + 12;
        contentH += (wrappedLines.size() * (fontSmall.getFontHeight() + 4)) + 18;
        float totalH = headH + contentH + footH;

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderUtil.drawRoundedRect(x, y, width, totalH, 12, 0xDC0E182E);

        RenderUtil.drawRoundedRect(x, y, width, 30, 12, 0x15D2E6FF);

        drawRoundedRectOutline(x, y, width, totalH, 12, 1.2f, 0xB4375A8C);

        float currY = y + 8;
        fontBig.drawCenteredString("Rank Begger", x + width / 2f, currY, 0xFFD2E6FF);
        currY += headH - 12;

        RenderUtil.drawRect(x + 16, currY, width - 32, 0.8f, 0x78375A8C);
        currY += 12;

        for (PolinexField f : fields) {
            fontContent.drawString(f.label + ":", x + 16, currY, 0xFF82A5D2);
            fontContent.drawString(f.value, x + width - 16 - fontContent.getStringWidth(f.value), currY, f.color);
            currY += fontContent.getFontHeight() + 8;
        }

        currY += 6;
        fontSmall.drawString("Last Message:", x + 16, currY, 0xFF82A5D2);
        currY += fontSmall.getFontHeight() + 4;
        for (String line : wrappedLines) {
            fontSmall.drawString(line, x + 20, currY, 0xFFC8DCFF);
            currY += fontSmall.getFontHeight() + 4;
        }

        float footerY = y + totalH - footH + 10;
        RenderUtil.drawRect(x + 16, footerY - 6, width - 32, 0.8f, 0x78375A8C);

        float logoS = 12;
        drawTexture(WIELIX_LOGO, x + 16, footerY, logoS, logoS);
        fontSmall.drawString("Wielix", x + 16 + logoS + 4, footerY + 1, 0xFFC8A03C);
        fontSmall.drawString(" 0.10.0", x + 16 + logoS + 4 + fontSmall.getStringWidth("Wielix"), footerY + 1, 0xFF777777);

        String sessionTime = formatSessionTime(now - ctx.sessionStartTime);
        fontSmall.drawString(sessionTime, x + width - 16 - fontSmall.getStringWidth(sessionTime), footerY + 1, 0xFF64A0E6);

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}

