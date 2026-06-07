package com.example.begger.ui.clickGui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import com.example.begger.RankBegger;
import com.example.begger.system.Category;
import com.example.begger.system.Module;
import com.example.begger.system.impl.ClickGuiModule;
import com.example.begger.settings.Setting;
import com.example.begger.settings.impl.*;
import com.example.begger.ui.font.CustomFontRenderer;
import com.example.begger.ui.font.FontManager;
import com.example.begger.utils.interfaces.MM;
import com.example.begger.utils.interfaces.SM;
import com.example.begger.utils.MathUtil;
import com.example.begger.utils.GuiUtil;
import com.example.begger.utils.RenderUtil;
import com.example.begger.utils.render.Snow;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ChatAllowedCharacters;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClickGui extends GuiScreen implements MM, SM {

    public static class Bounds {
        public float x, y, w, h;

        public Bounds(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean isHovered(int mx, int my) {
            return GuiUtil.isHovered(mx, my, x, y, w, h);
        }
    }

    private Bounds getBooleanBounds(float px, float drawY, float pw) {
        float baseW = 24f, baseH = 12f;
        float sx = px + pw - baseW - 4f;
        float sy = drawY + (18f - baseH) / 2f;
        return new Bounds(sx - 4f, sy - 4f, baseW + 8f, baseH + 8f);
    }

    private Bounds getSliderBounds(float px, float drawY, float pw) {
        float sliderX = px + 4f, sliderY = drawY + FontManager.consolas15.getFontHeight() + 6f;
        float sliderW = pw - 8f, sliderH = 3f;
        return new Bounds(sliderX - 4f, sliderY - 6f, sliderW + 8f, sliderH + 12f);
    }

    private Bounds getColorBounds(float px, float drawY, float pw) {
        return new Bounds(px + pw - 24f, drawY + 4f, 20f, 18f - 8f);
    }

    private Bounds getBindBounds(float px, float drawY, float pw, String text) {
        float kw = FontManager.consolas15.getStringWidth(text) + 8f;
        return new Bounds(px + pw - kw - 4f, drawY + 2f, kw, 18f - 4f);
    }

    private Bounds getInputBounds(float px, float drawY, float pw, String text) {
        float kw = FontManager.consolas15.getStringWidth(text) + 8f;
        if (kw > pw - 60f)
            kw = pw - 60f;
        float inputX = px + pw - kw - 4f;
        return new Bounds(inputX, drawY + 2f, kw, 18f - 4f);
    }

    public final ArrayList<ColorPicker> colorPickers = new ArrayList<>();
    public ClickGuiModule clickGuiModule;
    public NumberSetting hoveredSetting = null;
    public InputSetting selectedInputSetting = null;
    public BindSetting listeningBind = null;
    public ColorSetting selectedPicker = null;
    public ColorSetting closingPicker = null;
    public NumberSetting draggingSlider = null;
    public Object draggingToggle = null;
    private final Map<NumberSetting, Spring> sliderPosSprings = new HashMap<>();
    private final Map<NumberSetting, Spring> sliderScaleSprings = new HashMap<>();
    private final Map<Object, Spring> togglePosSprings = new HashMap<>();
    private final Map<Object, Spring> toggleScaleSprings = new HashMap<>();
    private final Map<Module, Spring> cardScaleSprings = new HashMap<>();

    public static class Spring {
        public float value;
        public float velocity;

        public Spring(float val) {
            this.value = val;
        }

        public void update(float target, float stiffness, float damping) {
            com.example.begger.system.impl.ClickGuiModule mod = (com.example.begger.system.impl.ClickGuiModule) com.example.begger.RankBegger.moduleManager.getModuleByClass(com.example.begger.system.impl.ClickGuiModule.class);
            if (mod != null && mod.performanceMode.isEnabled()) {
                this.value = target;
                this.velocity = 0;
                return;
            }
            float dt = 0.0166f;
            float force = stiffness * (target - value) - damping * velocity;
            velocity += force * dt;
            value += velocity * dt;
        }
    }

    private float windowX = -1, windowY = -1;
    private float WINDOW_W = 600f;
    private float WINDOW_H = 400f;
    private boolean draggingWindow = false;
    private float dragX = 0, dragY = 0;

    private float SIDEBAR_W = 140f;
    private static Category selectedCategory = Category.BEGGER;

    private final Map<Category, Float> categoryAnim = new HashMap<>();
    private final Map<Module, Float> moduleHoverAnim = new HashMap<>();
    private final Map<Module, Boolean> expandTarget = new HashMap<>();

    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private float scrollVelocity = 0f;

    private String searchQuery = "";
    private boolean isSearching = false;
    private final Map<Module, Float> moduleSearchAnim = new HashMap<>();

    private final Map<Module, Spring> expandSprings = new HashMap<>();
    private final Map<Module, Spring> moduleSlideSprings = new HashMap<>();
    private final Map<Module, Spring> moduleYSprings = new HashMap<>();
    private Spring categoryHighlightY;
    private long lastCategoryChange = 0;

    private final Snow snowSystem = new Snow(100);

    public static final ArrayList<Character> allowedChars = new ArrayList<>();
    static {
        for (char c = 'a'; c <= 'z'; c++)
            allowedChars.add(c);
        for (char c = 'A'; c <= 'Z'; c++)
            allowedChars.add(c);
        for (char c = '0'; c <= '9'; c++)
            allowedChars.add(c);
        allowedChars.add('_');
        allowedChars.add('-');
        allowedChars.add('.');
        allowedChars.add(':');
        allowedChars.add(',');
        allowedChars.add(';');
        allowedChars.add('#');
        allowedChars.add('$');
        allowedChars.add(' ');
        allowedChars.add('!');
    }

    public ClickGui() {
        for (Category c : Category.values())
            categoryAnim.put(c, 0f);
    }

    private float getEffectiveScale() {
        ScaledResolution sr = new ScaledResolution(mc);
        float userScale = clickGuiModule != null ? (float) clickGuiModule.guiScale.getValue() : 1.0f;
        float screenW = (float) sr.getScaledWidth();
        float screenH = (float) sr.getScaledHeight();
        float margin = 20f;
        float maxW = Math.max(100f, screenW - margin);
        float maxH = Math.max(100f, screenH - margin);
        float fitScale = Math.min(maxW / (WINDOW_W * userScale), maxH / (WINDOW_H * userScale));
        if (fitScale > 1.0f) fitScale = 1.0f;
        return userScale * fitScale;
    }

    @Override
    public void initGui() {
        super.initGui();

        if (clickGuiModule == null)
            clickGuiModule = mm.getModuleByClass(ClickGuiModule.class);

        WINDOW_W = clickGuiModule != null ? (float) clickGuiModule.windowWidth.getValue() : 600f;
        WINDOW_H = clickGuiModule != null ? (float) clickGuiModule.windowHeight.getValue() : 400f;

        ScaledResolution sr = new ScaledResolution(mc);
        float scale = getEffectiveScale();
        windowX = (sr.getScaledWidth() / scale - WINDOW_W) / 2f;
        windowY = (sr.getScaledHeight() / scale - WINDOW_H) / 2f;

        if (clickGuiModule != null && !clickGuiModule.performanceMode.isEnabled() && clickGuiModule.blur.isEnabled()
                && clickGuiModule.blurStrength.getValue() > 0) {
            if (mc.entityRenderer.getShaderGroup() != null) {
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (clickGuiModule != null && clickGuiModule.toggled) {
            clickGuiModule.toggle();
        }
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        hoveredSetting = null;
        colorPickers.clear();

        if (selectedPicker != null) {
            selectedPicker.getPicker().animProgress += (1f - selectedPicker.getPicker().animProgress) * 0.25f;
            if (selectedPicker.getPicker().animProgress > 0.99f)
                selectedPicker.getPicker().animProgress = 1f;
        }
        if (closingPicker != null) {
            closingPicker.getPicker().animProgress -= 0.2f;
            if (closingPicker.getPicker().animProgress <= 0f) {
                closingPicker.getPicker().animProgress = 0f;
                closingPicker = null;
            }
        }

        if (clickGuiModule == null)
            clickGuiModule = mm.getModuleByClass(ClickGuiModule.class);

        WINDOW_W = clickGuiModule != null ? (float) clickGuiModule.windowWidth.getValue() : 600f;
        WINDOW_H = clickGuiModule != null ? (float) clickGuiModule.windowHeight.getValue() : 400f;
        SIDEBAR_W = clickGuiModule != null ? (float) clickGuiModule.sidebarWidth.getValue() : 140f;

        float scale = getEffectiveScale();
        int realMouseX = mouseX;
        int realMouseY = mouseY;
        mouseX = (int) (realMouseX / scale);
        mouseY = (int) (realMouseY / scale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);

        float cornerRadius = clickGuiModule != null ? (float) clickGuiModule.cornerRadius.getValue() : 8f;
        float animSpeed = 0.15f;
        int opacity = clickGuiModule != null ? (int) (clickGuiModule.panelOpacity.getValue() * 2.55) : 255;

        int bgDark = (opacity << 24) | 0x0C0C0C;
        int bgMain = (opacity << 24) | 0x181818;
        int accent = 0xFF0096FF;
        int textWhite = 0xFFF0F0F0;
        int textGray = 0xFF969696;

        if (FontManager.productSans18 == null || FontManager.consolas15 == null) {
            if (com.example.begger.RankBegger.fontManager != null) {
                com.example.begger.RankBegger.fontManager.init();
            }
            if (FontManager.productSans18 == null || FontManager.consolas15 == null) return;
        }

        CustomFontRenderer font = FontManager.productSans18;
        CustomFontRenderer small = FontManager.consolas15;
        if (clickGuiModule != null && clickGuiModule.fontSetting != null) {
            switch (clickGuiModule.fontSetting.getSelected().toLowerCase()) {
                case "poppins":
                    small = FontManager.poppins15;
                    break;
                case "montserrat":
                    small = FontManager.montserrat15;
                    break;
                case "inter":
                    small = FontManager.inter15;
                    break;
                case "product sans":
                    small = FontManager.productSans18;
                    break;
                case "consolas":
                    small = FontManager.consolas15;
                    break;
                default:
                    small = FontManager.consolas15;
                    break;
            }
        }
        CustomFontRenderer headerFont = FontManager.productSans20;
        if (headerFont == null) headerFont = font;

        if (!Mouse.isButtonDown(0)) {
            draggingWindow = false;
        }
        if (draggingWindow) {
            windowX = mouseX - dragX;
            windowY = mouseY - dragY;
        }

        ScaledResolution srBounds = new ScaledResolution(mc);
        float sw = srBounds.getScaledWidth() / scale;
        float sh = srBounds.getScaledHeight() / scale;

        if (windowX < -WINDOW_W + 50)
            windowX = -WINDOW_W + 50;
        if (windowX > sw - 50)
            windowX = sw - 50;
        if (windowY < 0)
            windowY = 0;
        if (windowY > sh - 40)
            windowY = sh - 40;

        if (clickGuiModule != null && !clickGuiModule.performanceMode.isEnabled() && clickGuiModule.blur.isEnabled()
                && clickGuiModule.blurStrength.getValue() > 0) {
            float strength = (float) clickGuiModule.blurStrength.getValue();
            RenderUtil.drawRect(0, 0, sw, sh, ((int) (strength * 20) << 24));
        }

        if (clickGuiModule != null && !clickGuiModule.performanceMode.isEnabled() && clickGuiModule.snow.isEnabled()) {
            snowSystem.drawSnow(mouseX, mouseY);
        }

        RenderUtil.drawRoundedRect(windowX - 2, windowY - 2, WINDOW_W + 4, WINDOW_H + 4, cornerRadius + 2f,
                (100 << 24));

        RenderUtil.drawRoundedRect(windowX, windowY, WINDOW_W, WINDOW_H, cornerRadius, bgMain);

        RenderUtil.drawRoundedRect(windowX, windowY, SIDEBAR_W, WINDOW_H, cornerRadius, bgDark);
        RenderUtil.drawRect(windowX + SIDEBAR_W - 8f, windowY, 8f, WINDOW_H, bgDark);
        RenderUtil.drawRect(windowX + SIDEBAR_W, windowY, 1f, WINDOW_H, ((int) (opacity * 0.45f) << 24));

        headerFont.drawString("RANK Begger", windowX + 15, windowY + 20, accent);
        RenderUtil.drawRect(windowX + 15, windowY + 38, SIDEBAR_W - 30, 1, 0xFF323232);

        float searchY = windowY + 45f;
        boolean searchHovered = GuiUtil.isHovered(mouseX, mouseY, windowX + 10, searchY, SIDEBAR_W - 20, 20f);
        int searchBg = isSearching ? 0x1EFFFFFF
                : (searchHovered ? 0x0FFFFFFF : 0x05FFFFFF);
        RenderUtil.drawRoundedRect(windowX + 10, searchY, SIDEBAR_W - 20, 20f, 4f, searchBg);

        String displaySearch = searchQuery.isEmpty() && !isSearching ? "Search..."
                : searchQuery + (isSearching && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        small.drawString(displaySearch, windowX + 15, searchY + (20f - small.getFontHeight()) / 2f,
                searchQuery.isEmpty() && !isSearching ? textGray : textWhite);

        float catStartY = searchY + 25f;
        float targetSelY = catStartY;
        for (Category cat : Category.values()) {
            if (selectedCategory == cat)
                break;
            targetSelY += 30f;
        }

        if (categoryHighlightY == null) {
            categoryHighlightY = new Spring(targetSelY);
        }
        categoryHighlightY.update(targetSelY, 120f, 13f);

        float catY = catStartY;
        for (Category cat : Category.values()) {
            boolean isSelected = (selectedCategory == cat);
            boolean hovered = GuiUtil.isHovered(mouseX, mouseY, windowX + 10, catY, SIDEBAR_W - 20, 26f);

            if (hovered && !isSelected) {
                int hoverBg = 0x0CFFFFFF;
                RenderUtil.drawRoundedRect(windowX + 10, catY, SIDEBAR_W - 20, 26f, 4f, hoverBg);
            }
            catY += 30f;
        }

        int selBg = (accent & 0x00FFFFFF) | ((int) (255f * 0.15f) << 24);
        int glowBg = (accent & 0x00FFFFFF) | ((int) (255f * 0.25f) << 24);
        RenderUtil.drawRoundedRect(windowX + 8, categoryHighlightY.value - 2, SIDEBAR_W - 16, 30f, 6f, glowBg);
        RenderUtil.drawRoundedRect(windowX + 10, categoryHighlightY.value, SIDEBAR_W - 20, 26f, 4f, selBg);

        RenderUtil.drawRoundedRect(windowX + 10, categoryHighlightY.value + 4f, 3f, 18f, 1.5f, accent);

        catY = catStartY;
        for (Category cat : Category.values()) {
            boolean isSelected = (selectedCategory == cat);
            float anim = categoryAnim.getOrDefault(cat, 0f);
            anim = MathUtil.lerp(anim, isSelected ? 1f : 0f, animSpeed);
            categoryAnim.put(cat, anim);

            boolean hovered = GuiUtil.isHovered(mouseX, mouseY, windowX + 10, catY, SIDEBAR_W - 20, 26f);
            int catColor = interpolateColor(textGray, textWhite, Math.max(anim, hovered ? 0.6f : 0f));

            RenderUtil.drawRect(0, 0, 0, 0, 0);
            font.drawString(cat.name(), windowX + 22 + (anim * 4f), catY + (26f - font.getFontHeight()) / 2f, catColor);
            catY += 30f;
        }

        float hudBtnY = windowY + WINDOW_H - 40f;
        float hudBtnW = SIDEBAR_W - 20;
        float hudBtnH = 26f;
        boolean hudBtnHovered = GuiUtil.isHovered(mouseX, mouseY, windowX + 10, hudBtnY, hudBtnW, hudBtnH);
        int hudBtnBg = hudBtnHovered ? 0x0FFFFFFF : 0;
        if (hudBtnBg != 0) {
            RenderUtil.drawRoundedRect(windowX + 10, hudBtnY, hudBtnW, hudBtnH, 4f, hudBtnBg);
        }
        RenderUtil.drawRect(windowX + 10, hudBtnY + 4f, 2f, hudBtnH - 8f, accent);
        int hudTextColor = hudBtnHovered ? textWhite : textGray;
        font.drawString("Move Modules", windowX + 20, hudBtnY + (hudBtnH - font.getFontHeight()) / 2f, hudTextColor);

        float mainX = windowX + SIDEBAR_W;
        float mainW = WINDOW_W - SIDEBAR_W;
        float headerH = 40f;

        String headerText = searchQuery.isEmpty() ? selectedCategory.name() : "Search Results";
        headerFont.drawString(headerText, mainX + 20, windowY + 20, textWhite);
        RenderUtil.drawRect(mainX + 20, windowY + 38, mainW - 40, 1, 0xFF323232);

        java.util.List<Module> allMods = mm.getModules();
        java.util.List<Module> displayMods = new ArrayList<>();

        long timeSinceChange = System.currentTimeMillis() - lastCategoryChange;
        int staggerIndex = 0;

        for (Module m : allMods) {
            if (m.hidden)
                continue;
            boolean matchesSearch = false;
            if (searchQuery.isEmpty()) {
                matchesSearch = (m.getCategory() == selectedCategory);
            } else {
                matchesSearch = m.getName().toLowerCase().contains(searchQuery.toLowerCase());
            }

            float targetSlide = matchesSearch ? 1f : 0f;
            if (matchesSearch && targetSlide == 1f && !isSearching) {
                if (timeSinceChange < staggerIndex * 20L) {
                    targetSlide = 0f;
                } else {
                    staggerIndex++;
                }
            }

            final float initialSlide = matchesSearch ? 1f : 0f;
            Spring slideSpring = moduleSlideSprings.computeIfAbsent(m, k -> new Spring(initialSlide));
            if (!matchesSearch) {
                slideSpring.value = 0f;
            }
            slideSpring.update(targetSlide, 150f, 15f);

            float searchT = Math.max(0f, Math.min(1f, slideSpring.value));
            moduleSearchAnim.put(m, searchT);

            if (searchT > 0.001f) {
                displayMods.add(m);
            }
        }

        float contentHeight = 10f;
        for (Module m : displayMods) {
            float searchT = moduleSearchAnim.getOrDefault(m, 1f);
            Spring exSpring = expandSprings.computeIfAbsent(m, k -> new Spring(0f));
            float expandT = exSpring.value;
            float baseH = 35f;
            if (expandT > 0.001f) {
                baseH += 6f * expandT + settingsHeight(m) * expandT;
            }
            contentHeight += baseH * searchT;
            contentHeight += 5f * searchT;
        }

        float viewH = WINDOW_H - headerH;
        float maxScroll = Math.min(0f, viewH - contentHeight - 10f);

        int dWheel = Mouse.getDWheel();
        if (GuiUtil.isHovered(mouseX, mouseY, mainX, windowY, mainW, WINDOW_H)) {
            if (dWheel != 0) {
                scrollVelocity += dWheel * 0.15f;
            }
        }
        targetScrollY += scrollVelocity;
        scrollVelocity *= 0.82f;
        if (Math.abs(scrollVelocity) < 0.1f)
            scrollVelocity = 0f;

        targetScrollY = Math.max(maxScroll, Math.min(0f, targetScrollY));
        scrollY = MathUtil.lerp(scrollY, targetScrollY, 0.25f);

        RenderUtil.startScissorBox();
        RenderUtil.drawScissorBox(mainX, windowY + headerH, mainW, viewH, (double) scale);

        float drawY = windowY + headerH + scrollY + 10f;

        for (Module m : displayMods) {
            float searchT = moduleSearchAnim.getOrDefault(m, 1f);

            Spring toggleSpring = togglePosSprings.computeIfAbsent(m, k -> new Spring(m.toggled ? 1f : 0f));
            toggleSpring.update(m.toggled ? 1f : 0f, 150f, 14f);
            float toggleT = toggleSpring.value;

            Spring scaleSpring = toggleScaleSprings.computeIfAbsent(m, k -> new Spring(1.0f));
            float targetScale = (draggingToggle == m) ? 0.98f : 1.0f;
            scaleSpring.update(targetScale, 150f, 14f);

            float hoverT = moduleHoverAnim.getOrDefault(m, 0f);
            boolean isHovered = GuiUtil.isHovered(mouseX, mouseY, mainX + 20, drawY, mainW - 40, 35f * searchT);
            hoverT = MathUtil.lerp(hoverT, isHovered ? 1f : 0f, animSpeed);
            moduleHoverAnim.put(m, hoverT);

            Spring exSpring = expandSprings.computeIfAbsent(m, k -> new Spring(0f));
            float expandTgt = expandTarget.getOrDefault(m, false) ? 1f : 0f;
            exSpring.update(expandTgt, 100f, 15f);
            float expandT = Math.max(0f, exSpring.value);

            float modH = 35f;
            float setH = settingsHeight(m);
            float totalH = modH;
            if (expandT > 0.001f) {
                totalH += 6f * expandT + setH * expandT;
            }
            float scaledH = totalH * searchT;

            if (scaledH <= 0.5f) {
                drawY += scaledH + 5f * searchT;
                continue;
            }

            float outerY = windowY + headerH;
            float outerH = viewH;
            final float initialDrawY = drawY;
            Spring ySpring = moduleYSprings.computeIfAbsent(m, k -> new Spring(initialDrawY + 40f));
            if (searchT <= 0.05f) {
                ySpring.value = drawY + 30f;
                ySpring.velocity = 0f;
            } else if (Math.abs(ySpring.value - drawY) > 500f) {
                ySpring.value = drawY;
            }
            ySpring.update(drawY, 150f, 15f);
            float actualDrawY = ySpring.value;

            float modAlphaT = Math.min(1f, searchT * searchT);
            int bgAlpha = (int) (255 * modAlphaT);

            Spring cardScaleSpring = cardScaleSprings.computeIfAbsent(m, k -> new Spring(1.0f));
            float targetCardScale = 1.0f;
            if (draggingToggle == m && !expandTarget.getOrDefault(m, false)) {
                targetCardScale = 0.99f;
            } else if (isHovered && !expandTarget.getOrDefault(m, false)) {
                targetCardScale = 1.01f;
            }
            if (toggleSpring.velocity > 0.1f) {
                targetCardScale += (toggleSpring.velocity * 0.001f);
            }
            cardScaleSpring.update(targetCardScale, 150f, 14f);
            float cardScaleOffset = (1f - cardScaleSpring.value) * (mainW - 40f) / 2f;
            float cardX = mainX + 20 + cardScaleOffset;
            float cardW = (mainW - 40) * cardScaleSpring.value;
            float cardYDraw = actualDrawY + cardScaleOffset;
            float cardHTotal = scaledH * cardScaleSpring.value;

            float innerY = cardYDraw - 20f;
            float intersectY = Math.max(outerY, innerY);
            float intersectBottom = Math.min(outerY + outerH, cardYDraw + cardHTotal + 20f);
            float intersectH = Math.max(0f, intersectBottom - intersectY);

            if (intersectH <= 0f) {
                drawY += scaledH + 5f * searchT;
                continue;
            }

            RenderUtil.endScissorBox();
            RenderUtil.startScissorBox();
            if (intersectH > 0) {
                RenderUtil.drawScissorBox(mainX + 5, intersectY, mainW - 10, intersectH, (double) scale);
            } else {
                RenderUtil.drawScissorBox(mainX + 5, intersectY, mainW - 10, 0, (double) scale);
            }

            int cardShadow = ((int) (40 * modAlphaT) << 24);
            RenderUtil.drawRoundedRect(cardX + 2, cardYDraw + 2, cardW, totalH * cardScaleSpring.value, 8f, cardShadow);

            int baseCardColor = 0xFF1C1C1C;
            int hoverCardColor = 0xFF2A2A2A;
            float colorToggleT = Math.max(0f, Math.min(1f, toggleT));

            if (colorToggleT > 0.05f) {
                int activeTint = accent;
                int mixedBright = interpolateColor(hoverCardColor, activeTint, colorToggleT * 0.15f);
                hoverCardColor = mixedBright;
                baseCardColor = interpolateColor(baseCardColor, activeTint, colorToggleT * 0.08f);
            }

            int cardBg = interpolateColor(baseCardColor, hoverCardColor, hoverT);
            cardBg = (cardBg & 0x00FFFFFF) | (bgAlpha << 24);
            RenderUtil.drawRoundedRect(cardX, cardYDraw, cardW, totalH * cardScaleSpring.value, 8f, cardBg);
            if (m.toggled && toggleSpring.velocity > 0.5f) {
                float sweepFlashAlpha = (float) Math.sin(colorToggleT * Math.PI) * 45f * modAlphaT;
                int flashColor = (accent & 0x00FFFFFF) | ((int) Math.max(0, sweepFlashAlpha) << 24);
                RenderUtil.drawRoundedRect(cardX, cardYDraw, cardW, totalH * cardScaleSpring.value, 8f, flashColor);
            }
            if (colorToggleT > 0.05f) {
                int strokeColor = (accent & 0x00FFFFFF) | ((int) (40 * colorToggleT * modAlphaT) << 24);
                RenderUtil.drawRoundedRect(cardX - 1f, cardYDraw - 1f, cardW + 2f, totalH * cardScaleSpring.value + 2f,
                        9f, strokeColor);
                RenderUtil.drawRoundedRect(cardX, cardYDraw, cardW, totalH * cardScaleSpring.value, 8f, cardBg);
            }

            int nameColor = interpolateColor(textGray, textWhite, Math.max(toggleT, hoverT * 0.5f));
            nameColor = (nameColor & 0x00FFFFFF) | (bgAlpha << 24);
            font.drawString(m.getName(), cardX + 12,
                    cardYDraw + (modH * cardScaleSpring.value - font.getFontHeight()) / 2f, nameColor);

            float baseW = 32f * cardScaleSpring.value;
            float baseH = 16f * cardScaleSpring.value;
            Spring tScaleSpring = toggleScaleSprings.computeIfAbsent(m, k -> new Spring(1.0f));
            float switchW = baseW * tScaleSpring.value;
            float switchH = baseH * tScaleSpring.value;
            float switchX = cardX + cardW - 12f - baseW + (baseW - switchW) / 2f;
            float switchY = cardYDraw + (modH * cardScaleSpring.value - switchH) / 2f;

            int hoverOff = (bgAlpha << 24) | 0x4B4B4B;
            int baseOff = (bgAlpha << 24) | 0x2D2D2D;
            int targetOff = hoverT > 0.5f ? hoverOff : baseOff;
            int swColor = interpolateColor(targetOff, accent, colorToggleT);
            swColor = (swColor & 0x00FFFFFF) | (bgAlpha << 24);
            if (colorToggleT > 0.1f) {
                int glowColor1 = (accent & 0x00FFFFFF) | ((int) (60 * colorToggleT * modAlphaT) << 24);
                RenderUtil.drawRoundedRect(switchX - 1f, switchY - 1f, switchW + 2f, switchH + 2f, (switchH + 2f) / 2f,
                        glowColor1);
                int glowColor2 = (accent & 0x00FFFFFF) | ((int) (25 * colorToggleT * modAlphaT) << 24);
                RenderUtil.drawRoundedRect(switchX - 3f, switchY - 3f, switchW + 6f, switchH + 6f, (switchH + 6f) / 2f,
                        glowColor2);
            }
            RenderUtil.drawRoundedRect(switchX, switchY, switchW, switchH, switchH / 2f, swColor);

            float knobR = switchH / 2f - 2f;
            float stretch = Math.abs(toggleSpring.velocity) * 0.4f;
            float knobW = knobR * 2f + stretch;
            float startCenter = switchX + 2f + knobR;
            float endCenter = switchX + switchW - 2f - knobR;
            float knobCenter = startCenter + toggleT * (endCenter - startCenter);
            float knobX = knobCenter - knobW / 2f;
            int shadowColor = ((int) (60 * modAlphaT) << 24);
            RenderUtil.drawRoundedRect(knobX, switchY + 3f, knobW, knobR * 2f, knobR, shadowColor);
            int offKnob = (bgAlpha << 24) | 0xC8C8C8;
            int onKnob = (bgAlpha << 24) | 0xFFFFFF;
            int knobC = interpolateColor(offKnob, onKnob, colorToggleT);
            RenderUtil.drawRoundedRect(knobX, switchY + 2f, knobW, knobR * 2f, knobR, knobC);

            if (expandT > 0.001f) {
                float settingsPanelX = cardX + 8f;
                float separatorY = cardYDraw + (modH - 6f) * cardScaleSpring.value;
                float settingsPanelY = separatorY + 4f * expandT;
                float settingsPanelW = cardW - 16f;
                float settingsPanelH = setH * expandT + 4f * expandT;

                float settingsContentX = settingsPanelX + 8f;
                float settingsContentY = settingsPanelY + 4f;
                float settingsContentW = settingsPanelW - 16f;

                RenderUtil.drawRect(settingsContentX, separatorY, settingsContentW, 1,
                        ((int) (25 * expandT * modAlphaT) << 24) | 0xFFFFFF);

                int sBg = ((int) (bgAlpha * 0.95f) << 24) | 0x121212;
                RenderUtil.drawRoundedRect(settingsPanelX, settingsPanelY, settingsPanelW, settingsPanelH, 6f, sBg);

                float innerIntersectY = Math.max(outerY, settingsPanelY);
                float innerIntersectBottom = Math.min(outerY + outerH, settingsPanelY + settingsPanelH);
                float innerIntersectH = Math.max(0f, innerIntersectBottom - innerIntersectY);

                RenderUtil.endScissorBox();
                RenderUtil.startScissorBox();
                if (innerIntersectH > 0) {
                    RenderUtil.drawScissorBox(mainX + 20, innerIntersectY, mainW - 40, innerIntersectH, (double) scale);
                } else {
                    RenderUtil.drawScissorBox(mainX + 20, innerIntersectY, mainW - 40, 0, (double) scale);
                }

                float sY = settingsContentY;

                int settingIndex = 0;
                for (Setting s : sm.getValuesByMod(m)) {
                    if (!s.isVisible())
                        continue;
                    float staggerDelay = settingIndex * 0.08f;
                    float individualExpandT = Math.max(0f,
                            Math.min(1f, (expandT - staggerDelay) / (1f - staggerDelay)));
                    float individualAlphaT = Math.min(1f, individualExpandT * individualExpandT) * modAlphaT;
                    float yOffset = (1f - individualAlphaT) * 10f;
                    float renderY = sY + yOffset;
                    if (individualAlphaT > 0.01f) {
                        sY = renderSetting(mouseX, mouseY, s, settingsContentX, renderY, settingsContentW, font, small,
                                accent, textWhite, textGray, individualAlphaT);
                    } else {
                        sY += 18f;
                    }
                    settingIndex++;
                }
            }

            RenderUtil.endScissorBox();
            RenderUtil.startScissorBox();
            RenderUtil.drawScissorBox(mainX, windowY + headerH, mainW, viewH, (double) scale);

            drawY += scaledH + 5f * searchT;
        }

        RenderUtil.endScissorBox();

        if (closingPicker != null) {
            closingPicker.getPicker().draw();
        }
        if (selectedPicker != null) {
            selectedPicker.getPicker().draw();
        }
        GlStateManager.popMatrix();
        super.drawScreen(realMouseX, realMouseY, partialTicks);
    }

    private float renderSetting(int mouseX, int mouseY, Setting setting, float px, float drawY, float pw,
            CustomFontRenderer font, CustomFontRenderer small, int accentColor,
            int textActive, int textInact, float alpha) {
        float SETTING_H = 18f;

        int textColorAlpha = (int) (255 * alpha);
        textInact = (textInact & 0x00FFFFFF) | (textColorAlpha << 24);
        textActive = (textActive & 0x00FFFFFF) | (textColorAlpha << 24);
        int bgAlphaVal = (int) (60 * alpha);
        int bgAccentVal = (accentColor & 0x00FFFFFF) | ((int) (255 * alpha) << 24);

        if (setting instanceof BooleanSetting) {
            BooleanSetting b = (BooleanSetting) setting;
            Spring toggleSpring = togglePosSprings.computeIfAbsent(b, k -> new Spring(b.isEnabled() ? 1f : 0f));
            toggleSpring.update(b.isEnabled() ? 1f : 0f, 150f, 14f);
            float toggleT = toggleSpring.value;
            Spring scaleSpring = toggleScaleSprings.computeIfAbsent(b, k -> new Spring(1.0f));
            float targetScale = (draggingToggle == b) ? 0.98f : 1.0f;
            scaleSpring.update(targetScale, 150f, 14f);

            small.drawString(b.getName(), px + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, textInact);

            float baseW = 24f, baseH = 12f;
            float sw = baseW * scaleSpring.value;
            float sh = baseH * scaleSpring.value;
            float sx = px + pw - baseW - 4f + (baseW - sw) / 2f;
            float sy = drawY + (SETTING_H - sh) / 2f;
            int offBg = (bgAlphaVal << 24) | 0x2D2D2D;
            Bounds bounds = getBooleanBounds(px, drawY, pw);
            boolean isSettingHovered = bounds.isHovered(mouseX, mouseY);
            if (isSettingHovered && toggleT < 0.5f) {
                offBg = (bgAlphaVal << 24) | 0x4B4B4B;
            }

            int bg = interpolateColor(offBg, bgAccentVal, toggleT);
            if (toggleT > 0.1f) {
                int glowColor1 = (accentColor & 0x00FFFFFF) | ((int) (60 * toggleT * alpha) << 24);
                RenderUtil.drawRoundedRect(sx - 1f, sy - 1f, sw + 2f, sh + 2f, (sh + 2f) / 2f, glowColor1);
                int glowColor2 = (accentColor & 0x00FFFFFF) | ((int) (25 * toggleT * alpha) << 24);
                RenderUtil.drawRoundedRect(sx - 3f, sy - 3f, sw + 6f, sh + 6f, (sh + 6f) / 2f, glowColor2);
            }
            RenderUtil.drawRoundedRect(sx, sy, sw, sh, sh / 2f, bg);
            float kr = sh / 2f - 1.5f;
            float stretch = Math.abs(toggleSpring.velocity) * 0.3f;
            float kw = kr * 2f + stretch;
            float startCenter = sx + 1.5f + kr;
            float endCenter = sx + sw - 1.5f - kr;
            float knobCenter = startCenter + toggleT * (endCenter - startCenter);
            float kx = knobCenter - kw / 2f;
            int shadowColor = ((int) (60 * alpha) << 24);
            RenderUtil.drawRoundedRect(kx, sy + 2.5f, kw, kr * 2f, kr, shadowColor);
            int offKnob = (textColorAlpha << 24) | 0xC8C8C8;
            int onKnob = (textColorAlpha << 24) | 0xFFFFFF;
            int knobC = interpolateColor(offKnob, onKnob, toggleT);
            RenderUtil.drawRoundedRect(kx, sy + 1.5f, kw, kr * 2f, kr, knobC);

            drawY += SETTING_H;
        } else if (setting instanceof NumberSetting) {
            NumberSetting n = (NumberSetting) setting;

            Bounds bounds = getSliderBounds(px, drawY, pw);
            float sliderX = bounds.x + 4f, sliderY = bounds.y + 6f;
            float sliderW = bounds.w - 8f, sliderH = bounds.h - 12f;

            if (draggingSlider == n) {
                double nv = MathUtil.round(
                        (mouseX - sliderX) * (n.getMaxValue() - n.getMinValue()) / sliderW + n.getMinValue(),
                        n.getDecimalPlaces());
                n.setValue(Math.max(n.getMinValue(), Math.min(n.getMaxValue(), nv)));
            }

            float currentVal = (float) n.getValue();
            Spring posSpring = sliderPosSprings.computeIfAbsent(n, k -> new Spring(currentVal));
            Spring scaleSpring = sliderScaleSprings.computeIfAbsent(n, k -> new Spring(1.0f));

            posSpring.update(currentVal, 150f, 14f);
            float targetScale = (draggingSlider == n) ? 0.95f : 1.0f;
            scaleSpring.update(targetScale, 150f, 14f);

            small.drawString(n.getName(), px + 4f, drawY + 2f, textInact);
            small.drawString(String.valueOf(n.getValue()),
                    px + pw - small.getStringWidth(String.valueOf(n.getValue())) - 4f, drawY + 2f, textActive);
            int trackOff = (bgAlphaVal << 24) | 0x2D2D2D;
            RenderUtil.drawRoundedRect(sliderX, sliderY, sliderW, sliderH, sliderH / 2f, trackOff);
            float filled = (float) ((posSpring.value - n.getMinValue()) / (n.getMaxValue() - n.getMinValue()))
                    * sliderW;
            float clampedFilled = Math.max(0, Math.min(sliderW, filled));
            RenderUtil.drawRoundedRect(sliderX, sliderY, clampedFilled, sliderH, sliderH / 2f, bgAccentVal);
            float knobX = sliderX + filled;
            knobX = Math.max(sliderX, Math.min(sliderX + sliderW, knobX));
            float knobR = 4.5f * scaleSpring.value;
            float knobY = sliderY + sliderH / 2f;
            if (draggingSlider == n) {
                int glowColor = (accentColor & 0x00FFFFFF) | ((int) (60 * scaleSpring.value * alpha) << 24);
                RenderUtil.drawCircle(knobX, knobY, knobR + 3.5f, glowColor);
            }
            int shadowColor = ((int) (75 * alpha) << 24);
            RenderUtil.drawCircle(knobX, knobY + 1.5f, knobR, shadowColor);
            int offKnob = (textColorAlpha << 24) | 0xDCDCDC;
            int onKnob = (textColorAlpha << 24) | 0xFFFFFF;
            int knobC = interpolateColor(offKnob, onKnob, scaleSpring.value);
            RenderUtil.drawCircle(knobX, knobY, knobR, knobC);

            if (bounds.isHovered(mouseX, mouseY)) {
                hoveredSetting = n;
            }
            drawY += SETTING_H + 8f;
        } else if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            small.drawString(cs.getName(), px + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, textInact);
            boolean isSelected = selectedPicker == cs;
            if (isSelected) {
                RenderUtil.drawRoundedRect(px + pw - 26f, drawY + 3f, 24f, SETTING_H - 6f, 3f,
                        (textColorAlpha << 24) | 0xFFFFFF);
            }
            int csC = (cs.getColor().getRGB() & 0x00FFFFFF) | (textColorAlpha << 24);
            RenderUtil.drawRoundedRect(px + pw - 24f, drawY + 4f, 20f, SETTING_H - 8f, 3f, csC);
            if (isSelected) {
                ColorPicker picker = cs.getPicker();
                float anchorX = windowX + WINDOW_W + 8f;
                float anchorY = drawY - 20f;
                ScaledResolution sr = new ScaledResolution(mc);
                float scale = getEffectiveScale();
                float screenW = sr.getScaledWidth() / scale;
                float screenH = sr.getScaledHeight() / scale;
                if (anchorX + picker.getPanelWidth() > screenW) {
                    anchorX = windowX - picker.getPanelWidth() - 8f;
                }
                if (anchorX < 2)
                    anchorX = 2;
                if (anchorY + picker.getPanelHeight() > screenH - 2)
                    anchorY = screenH - 2 - picker.getPanelHeight();
                if (anchorY < 2)
                    anchorY = 2;
                picker.panelX = anchorX;
                picker.panelY = anchorY;
            }
            drawY += SETTING_H;
        } else if (setting instanceof BindSetting) {
            BindSetting bs = (BindSetting) setting;
            small.drawString(bs.getName(), px + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, textInact);
            String kn = bs.isListening() ? "..." : bs.getKeyName();
            int kc = bs.isListening() ? ((textColorAlpha << 24) | 0xFFC800) : textActive;
            float kw = small.getStringWidth(kn) + 8f;
            RenderUtil.drawRoundedRect(px + pw - kw - 4f, drawY + 2f, kw, SETTING_H - 4f, 3f,
                    (bgAlphaVal << 24) | 0x282828);
            small.drawString(kn, px + pw - kw / 2f - small.getStringWidth(kn) / 2f - 4f,
                    drawY + (SETTING_H - small.getFontHeight()) / 2f, kc);
            drawY += SETTING_H + 2f;
        } else if (setting instanceof InputSetting) {
            InputSetting is = (InputSetting) setting;
            small.drawString(is.getName(), px + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, textInact);
            String content = is.getContent();
            boolean isListening = (selectedInputSetting == is);
            String displayContent = content + (isListening && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
            int kc = isListening ? ((textColorAlpha << 24) | 0xFFC800) : textActive;
            float kw = small.getStringWidth(displayContent) + 8f;
            if (kw > pw - 60f)
                kw = pw - 60f;
            float inputX = px + pw - kw - 4f;
            RenderUtil.drawRoundedRect(inputX, drawY + 2f, kw, SETTING_H - 4f, 3f, (bgAlphaVal << 24) | 0x1E1E1E);
            String renderStr = displayContent;
            while (small.getStringWidth(renderStr) > kw - 6f && renderStr.length() > 1) {
                renderStr = renderStr.substring(1);
            }
            small.drawString(renderStr, inputX + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, kc);
            drawY += SETTING_H + 2f;
        } else if (setting instanceof SimpleModeSetting) {
            SimpleModeSetting sms = (SimpleModeSetting) setting;
            small.drawString(sms.getName(), px + 4f, drawY + (SETTING_H - small.getFontHeight()) / 2f, textInact);
            String selected = sms.getSelected();
            float kw = small.getStringWidth(selected) + 8f;
            RenderUtil.drawRoundedRect(px + pw - kw - 4f, drawY + 2f, kw, SETTING_H - 4f, 3f,
                    (bgAlphaVal << 24) | 0x282828);
            small.drawString(selected, px + pw - kw / 2f - small.getStringWidth(selected) / 2f - 4f,
                    drawY + (SETTING_H - small.getFontHeight()) / 2f, textActive);
            drawY += SETTING_H + 2f;
        } else if (setting instanceof ButtonSetting) {
            ButtonSetting bs = (ButtonSetting) setting;
            float kw = small.getStringWidth(bs.getName()) + 16f;
            float bx = px + (pw - kw) / 2f;
            boolean hovered = GuiUtil.isHovered(mouseX, mouseY, bx, drawY + 2f, kw, SETTING_H - 4f);
            int bColor = hovered ? (bgAlphaVal << 24) | 0x404040 : (bgAlphaVal << 24) | 0x2D2D2D;
            RenderUtil.drawRoundedRect(bx, drawY + 2f, kw, SETTING_H - 4f, 3f, bColor);
            small.drawString(bs.getName(), bx + kw / 2f - small.getStringWidth(bs.getName()) / 2f,
                    drawY + (SETTING_H - small.getFontHeight()) / 2f, textActive);
            drawY += SETTING_H + 2f;
        }
        return drawY;
    }

    private float settingsHeight(Module m) {
        float h = 0f;
        for (Setting s : sm.getValuesByMod(m)) {
            if (!s.isVisible())
                continue;
            if (s instanceof NumberSetting)
                h += 26f;
            else if (s instanceof BindSetting || s instanceof InputSetting || s instanceof SimpleModeSetting
                    || s instanceof ButtonSetting)
                h += 20f;
            else
                h += 18f;
        }
        return h + 5f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float scale = getEffectiveScale();
        mouseX /= scale;
        mouseY /= scale;

        if (selectedPicker != null) {
            ColorPicker picker = selectedPicker.getPicker();
            if (picker.isInsidePanel(mouseX, mouseY)) {
                picker.click(mouseX, mouseY, mouseButton);
                return;
            } else {
                closingPicker = selectedPicker;
                selectedPicker = null;
            }
        }

        boolean topBarHovered = GuiUtil.isHovered(mouseX, mouseY, windowX, windowY, SIDEBAR_W, 40f);
        boolean headerHovered = GuiUtil.isHovered(mouseX, mouseY, windowX + SIDEBAR_W, windowY, WINDOW_W - SIDEBAR_W,
                40f);
        if (mouseButton == 0 && (topBarHovered || headerHovered)) {
            draggingWindow = true;
            dragX = mouseX - windowX;
            dragY = mouseY - windowY;
        }

        float searchY = windowY + 45f;
        if (mouseButton == 0 && GuiUtil.isHovered(mouseX, mouseY, windowX + 10, searchY, SIDEBAR_W - 20, 20f)) {
            isSearching = true;
            return;
        } else {
            if (mouseButton == 0)
                isSearching = false;
        }

        float catY = searchY + 25f;
        for (Category cat : Category.values()) {
            if (mouseButton == 0 && GuiUtil.isHovered(mouseX, mouseY, windowX + 10, catY, SIDEBAR_W - 20, 26f)) {
                selectedCategory = cat;
                targetScrollY = 0f;
                searchQuery = "";
                isSearching = false;
                moduleSearchAnim.clear();
                lastCategoryChange = System.currentTimeMillis();
                return;
            }
            catY += 30f;
        }

        float hudBtnY = windowY + WINDOW_H - 40f;
        if (mouseButton == 0 && GuiUtil.isHovered(mouseX, mouseY, windowX + 10, hudBtnY, SIDEBAR_W - 20, 26f)) {
            mc.displayGuiScreen(new HudEditGui());
            return;
        }

        float mainX = windowX + SIDEBAR_W;
        float mainW = WINDOW_W - SIDEBAR_W;
        float headerH = 40f;

        if (mouseY > windowY + headerH && mouseY < windowY + WINDOW_H && mouseX > mainX
                && mouseX < windowX + WINDOW_W) {
            float drawY = windowY + headerH + scrollY + 10f;

            java.util.List<Module> allMods = mm.getModules();
            java.util.List<Module> displayMods = new ArrayList<>();
            for (Module m : allMods) {
                if (m.hidden)
                    continue;
                if (moduleSearchAnim.getOrDefault(m, 1f) > 0.001f) {
                    displayMods.add(m);
                }
            }

            for (Module m : displayMods) {
                float searchT = moduleSearchAnim.getOrDefault(m, 1f);
                Spring exSpring = expandSprings.computeIfAbsent(m, k -> new Spring(0f));
                float expandT = Math.max(0f, exSpring.value);
                boolean isExpanded = expandTarget.getOrDefault(m, false);

                float modH = 35f;
                float setH = settingsHeight(m);
                float totalH = modH;
                if (expandT > 0.001f) {
                    totalH += 6f * expandT + setH * expandT;
                }
                float scaledH = totalH * searchT;

                if (scaledH <= 0.5f) {
                    drawY += scaledH + 5f * searchT;
                    continue;
                }
                float currentCardScale = cardScaleSprings.containsKey(m) ? cardScaleSprings.get(m).value : 1.0f;
                float cardScaleOffset = (1f - currentCardScale) * (mainW - 40f) / 2f;
                float actualDrawY = moduleYSprings.containsKey(m) ? moduleYSprings.get(m).value : drawY;
                float cardYDraw = actualDrawY + cardScaleOffset;
                float cardHTotal = modH * currentCardScale;
                float cardX = mainX + 20f + cardScaleOffset;
                float cardW = (mainW - 40f) * currentCardScale;

                if (GuiUtil.isHovered(mouseX, mouseY, mainX + 20, cardYDraw, mainW - 40, cardHTotal)) {
                    if (mouseButton == 0) {
                        if (!m.isKeybindOnly()) {
                            draggingToggle = m;
                        }
                    } else if (mouseButton == 1) {
                        expandTarget.put(m, !isExpanded);
                        selectedInputSetting = null;
                        if (selectedPicker != null) {
                            closingPicker = selectedPicker;
                            selectedPicker = null;
                        }
                    }
                    return;
                }

                if (isExpanded || expandT > 0.01f) {
                    float settingsPanelX = cardX + 8f;
                    float separatorY = cardYDraw + (modH - 6f) * currentCardScale;
                    float settingsPanelY = separatorY + 4f * expandT;
                    float settingsPanelW = cardW - 16f;

                    float settingsContentX = settingsPanelX + 8f;
                    float settingsContentY = settingsPanelY + 4f;
                    float settingsContentW = settingsPanelW - 16f;

                    float sY = settingsContentY;
                    for (Setting s : sm.getValuesByMod(m)) {
                        if (!s.isVisible())
                            continue;

                        if (searchT > 0.5f && expandT > 0.5f) {
                            sY = handleSettingClick(mouseX, mouseY, s, settingsContentX, sY, settingsContentW,
                                    mouseButton);
                        } else {
                            float h = (s instanceof NumberSetting) ? 26f
                                    : ((s instanceof BindSetting || s instanceof InputSetting) ? 20f : 18f);
                            sY += h;
                        }
                    }
                }
                drawY += scaledH + 5f * searchT;
            }
        }
    }

    private float handleSettingClick(int mouseX, int mouseY, Setting s, float px, float drawY, float pw, int btn) {
        float SETTING_H = 18f;
        if (s instanceof BooleanSetting) {
            Bounds bounds = getBooleanBounds(px, drawY, pw);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                draggingToggle = s;
            }
            drawY += SETTING_H;
        } else if (s instanceof NumberSetting) {
            Bounds bounds = getSliderBounds(px, drawY, pw);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                draggingSlider = (NumberSetting) s;
            }
            drawY += SETTING_H + 8f;
        } else if (s instanceof ColorSetting) {
            Bounds bounds = getColorBounds(px, drawY, pw);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                if (selectedPicker == s) {
                    closingPicker = selectedPicker;
                    selectedPicker = null;
                } else {
                    if (selectedPicker != null) {
                        closingPicker = selectedPicker;
                    }
                    if (closingPicker == s) {
                        closingPicker = null;
                    }
                    selectedPicker = (ColorSetting) s;
                    selectedPicker.getPicker().animProgress = 0f;
                }
            }
            drawY += SETTING_H;
        } else if (s instanceof BindSetting) {
            BindSetting bs = (BindSetting) s;
            String kn = bs.isListening() ? "..." : bs.getKeyName();
            Bounds bounds = getBindBounds(px, drawY, pw, kn);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                if (!bs.isListening()) {
                    listeningBind = bs;
                    bs.setListening(true);
                    selectedInputSetting = null;
                    if (selectedPicker != null) {
                        closingPicker = selectedPicker;
                        selectedPicker = null;
                    }
                }
            }
            drawY += SETTING_H + 2f;
        } else if (s instanceof InputSetting) {
            InputSetting is = (InputSetting) s;
            String content = is.getContent();
            Bounds bounds = getInputBounds(px, drawY, pw, content);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                selectedInputSetting = is;
                listeningBind = null;
                if (selectedPicker != null) {
                    closingPicker = selectedPicker;
                    selectedPicker = null;
                }
            } else if (btn == 0 && selectedInputSetting == is) {
                selectedInputSetting = null;
                com.example.begger.Config.save();
            }
            drawY += SETTING_H + 2f;
        } else if (s instanceof SimpleModeSetting) {
            SimpleModeSetting sms = (SimpleModeSetting) s;
            String modeStr = sms.getSelected();
            float kw = FontManager.consolas15.getStringWidth(modeStr) + 8f;
            Bounds bounds = new Bounds(px + pw - kw - 4f, drawY + 2f, kw, SETTING_H - 4f);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                String[] opts = sms.getOptions();
                int idx = 0;
                for (int i = 0; i < opts.length; i++) {
                    if (opts[i].equalsIgnoreCase(sms.getSelected())) {
                        idx = i;
                        break;
                    }
                }
                idx = (idx + 1) % opts.length;
                sms.setSelected(opts[idx]);
                com.example.begger.Config.save();
            }
            drawY += SETTING_H + 2f;
        } else if (s instanceof ButtonSetting) {
            ButtonSetting bs = (ButtonSetting) s;
            float kw = FontManager.productSans18.getStringWidth(bs.getName()) + 16f;
            float bx = px + (pw - kw) / 2f;
            Bounds bounds = new Bounds(bx, drawY + 2f, kw, SETTING_H - 4f);
            if (btn == 0 && bounds.isHovered(mouseX, mouseY)) {
                bs.runMethod();
            }
            drawY += SETTING_H + 2f;
        }
        return drawY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        float scale = getEffectiveScale();
        mouseX /= scale;
        mouseY /= scale;

        draggingWindow = false;
        if (selectedPicker != null) {
            selectedPicker.getPicker().release();
        }
        if (draggingSlider != null) {
            com.example.begger.Config.save();
            draggingSlider = null;
        }
        if (draggingToggle != null) {
            if (draggingToggle instanceof Module) {
                Module m = (Module) draggingToggle;
                if (!m.isKeybindOnly()) {
                    m.toggle();
                    com.example.begger.Config.save();
                }
            } else if (draggingToggle instanceof BooleanSetting) {
                BooleanSetting b = (BooleanSetting) draggingToggle;
                b.setEnabled(!b.isEnabled());
                com.example.begger.Config.save();
            }
            draggingToggle = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int btn, long timeSinceLastClick) {
        float scale = getEffectiveScale();
        mouseX /= scale;
        mouseY /= scale;

        if (selectedPicker != null) {
            selectedPicker.getPicker().drag(mouseX, mouseY);
        }
        super.mouseClickMove(mouseX, mouseY, btn, timeSinceLastClick);
    }

    @Override
    public void keyTyped(char key, int code) throws IOException {
        if (code == 1 && listeningBind == null) {
            mc.displayGuiScreen(null);
            return;
        }

        if (isSearching) {
            if (code == Keyboard.KEY_ESCAPE) {
                isSearching = false;
            } else if (code == Keyboard.KEY_BACK && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            } else if (allowedChars.contains(key)) {
                searchQuery += key;
            }
            return;
        }

        if (listeningBind != null) {
            if (code == Keyboard.KEY_ESCAPE || code == Keyboard.KEY_DELETE || code == Keyboard.KEY_BACK) {
                listeningBind.setKeyCode(Keyboard.KEY_NONE);
                listeningBind.setListening(false);
                listeningBind = null;
                com.example.begger.Config.save();
            } else {
                listeningBind.setKeyCode(code);
                listeningBind.setListening(false);
                listeningBind = null;
                com.example.begger.Config.save();
            }
        } else if (selectedInputSetting != null) {
            if (code == Keyboard.KEY_RETURN || code == Keyboard.KEY_ESCAPE) {
                selectedInputSetting = null;
                com.example.begger.Config.save();
            } else if (code == Keyboard.KEY_BACK && !selectedInputSetting.getContent().isEmpty()) {
                selectedInputSetting.setContent(
                        selectedInputSetting.getContent().substring(0, selectedInputSetting.getContent().length() - 1));
            } else if (ChatAllowedCharacters.isAllowedCharacter(key)) {
                selectedInputSetting.setContent(selectedInputSetting.getContent() + key);
            }
        } else if (selectedPicker != null) {
            String hex = selectedPicker.getPicker().getCurrentColorHexInputString();
            if (code == Keyboard.KEY_BACK && !hex.isEmpty())
                selectedPicker.getPicker().setCurrentColorHexInputString(hex.substring(0, hex.length() - 1));
            else if (allowedChars.contains(key) && hex.length() <= 7)
                selectedPicker.getPicker().setCurrentColorHexInputString(hex + key);
            selectedPicker.getPicker().setColor(selectedPicker.getPicker()
                    .stringToColor(selectedPicker.getPicker().getCurrentColorHexInputString()));
        } else if (hoveredSetting != null) {
            double step = 1.0 / Math.pow(10, hoveredSetting.getDecimalPlaces());
            double nv = hoveredSetting.getValue()
                    + (code == Keyboard.KEY_RIGHT ? step : code == Keyboard.KEY_LEFT ? -step : 0);
            hoveredSetting.setValue(
                    MathUtil.round(Math.max(hoveredSetting.getMinValue(), Math.min(hoveredSetting.getMaxValue(), nv)),
                            hoveredSetting.getDecimalPlaces()));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static int interpolateColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF, a1 = (c1 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF, a2 = (c2 >> 24) & 0xFF;
        return ((int) (a1 + (a2 - a1) * t) << 24) | ((int) (r1 + (r2 - r1) * t) << 16)
                | ((int) (g1 + (g2 - g1) * t) << 8) | (int) (b1 + (b2 - b1) * t);
    }
}