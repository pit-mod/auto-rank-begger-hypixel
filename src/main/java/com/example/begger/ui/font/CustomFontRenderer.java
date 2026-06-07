package com.example.begger.ui.font;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import com.example.begger.utils.interfaces.MC;
import com.example.begger.utils.RenderUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CustomFontRenderer extends FontRenderer implements MC {

    private static final String ALPHABET = "ABCDEFGHOKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをんアイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン";
    private static final String COLOR_CODE_CHARACTERS = "0123456789abcdefklmnor";
    private static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    private static final float SCALE = 0.5f;
    private static final float SCALE_INVERSE = 1 / SCALE;
    private static final char COLOR_INVOKER = '\247';
    private static final int[] COLOR_CODES = new int[32];
    private static final int LATIN_MAX_AMOUNT = 256;
    private static final int MARGIN_WIDTH = 4;
    private static final int MASK = 0xFF;

    private final Font font;
    private final boolean fractionalMetrics;
    private final float fontHeight;
    private final FontCharacter[] defaultCharacters = new FontCharacter[LATIN_MAX_AMOUNT];
    private final FontCharacter[] boldCharacters = new FontCharacter[LATIN_MAX_AMOUNT];
    private boolean antialiasing = true;

    public CustomFontRenderer(final Font font) {
        this(font, true);
    }

    public CustomFontRenderer(final Font font, final boolean fractionalMetrics, final boolean antialiasing) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), mc.isUnicode());
        this.antialiasing = antialiasing;
        this.font = font;
        this.fractionalMetrics = fractionalMetrics;
        this.fontHeight = (float) (font.getStringBounds(ALPHABET, new FontRenderContext(new AffineTransform(), antialiasing, fractionalMetrics)).getHeight() / 2);
        this.fillCharacters(this.defaultCharacters, Font.PLAIN);
        this.fillCharacters(this.boldCharacters, Font.BOLD);
        this.FONT_HEIGHT = (int) fontHeight;
    }

    public CustomFontRenderer(final Font font, final boolean fractionalMetrics) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), mc.isUnicode());
        this.font = font;
        this.fractionalMetrics = fractionalMetrics;
        this.fontHeight = (float) (font.getStringBounds(ALPHABET, new FontRenderContext(new AffineTransform(), true, fractionalMetrics)).getHeight() / 2);
        this.fillCharacters(this.defaultCharacters, Font.PLAIN);
        this.fillCharacters(this.boldCharacters, Font.BOLD);
        this.FONT_HEIGHT = (int) fontHeight;
    }

    public static void calculateColorCodes() {
        for (int i = 0; i < 32; ++i) {
            final int amplifier = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + amplifier;
            int green = (i >> 1 & 1) * 170 + amplifier;
            int blue = (i & 1) * 170 + amplifier;
            if (i == 6) {
                red += 85;
            }
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            COLOR_CODES[i] = 0xFF000000 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    public void fillCharacters(final FontCharacter[] characters, final int style) {
        final Font font = this.font.deriveFont(style);
        final BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();
        final FontMetrics fontMetrics = fontGraphics.getFontMetrics(font);

        for (int i = 0; i < characters.length; ++i) {
            final char character = (char) i;
            final Rectangle2D charRectangle = fontMetrics.getStringBounds(character + "", fontGraphics);

            final BufferedImage charImage = new BufferedImage(MathHelper.ceiling_float_int(
                    (float) charRectangle.getWidth()) + MARGIN_WIDTH * 2, MathHelper.ceiling_float_int(
                    (float) charRectangle.getHeight() + 5), BufferedImage.TYPE_INT_ARGB);

            final Graphics2D charGraphics = (Graphics2D) charImage.getGraphics();
            charGraphics.setFont(font);

            final int width = charImage.getWidth();
            final int height = charImage.getHeight();
            charGraphics.setColor(TRANSPARENT_COLOR);
            charGraphics.fillRect(0, 0, width, height);
            setRenderHints(charGraphics);
            charGraphics.drawString(character + "", MARGIN_WIDTH, font.getSize());

            final int charTexture = GL11.glGenTextures();
            uploadTexture(charTexture, charImage, width, height);

            FontCharacter fontChar = new FontCharacter();
            fontChar.setTexture(charTexture);
            fontChar.setWidth(width);
            fontChar.setHeight(height);

            characters[i] = fontChar;

            charGraphics.dispose();
        }

        fontGraphics.dispose();
    }

    public void setRenderHints(final Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        if (antialiasing) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public void uploadTexture(final int texture, final BufferedImage image, final int width, final int height) {
        final int[] pixels = image.getRGB(0, 0, width, height, new int[width * height], 0, width);
        final ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width * height * MARGIN_WIDTH);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int pixel = pixels[x + y * width];
                byteBuffer.put((byte) ((pixel >> 16) & MASK));
                byteBuffer.put((byte) ((pixel >> 8) & MASK));
                byteBuffer.put((byte) (pixel & MASK));
                byteBuffer.put((byte) ((pixel >> 24) & MASK));
            }
        }
        byteBuffer.flip();
        GlStateManager.bindTexture(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
    }

    public int drawStringWithShadow(final String text, final float x, final float y, final int color) {
        drawString(text, x + 0.5f, y + 0.5f, color, true);
        return drawString(text, x, y, color);
    }

    public int drawString(final String text, final float x, final float y, final int color) {
        return drawString(text, x, y, color, false);
    }

    public int drawCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        drawString(text, x - (getStringWidth(text) / 2f) + 0.5f, y + 0.5f, color, true);
        return drawString(text, x - (getStringWidth(text) / 2f), y, color, false);
    }

    public int drawCenteredString(final String text, final float x, final float y, final int color) {
        return drawString(text, x - (getStringWidth(text) / 2f), y, color, false);
    }

    public int drawTotalCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        drawString(text, x - (getStringWidth(text) / 2f) + 0.5f, y - (getFontHeight() / 2) + 0.5f, color, true);
        return drawString(text, x - (getStringWidth(text) / 2f), y - (getFontHeight() / 2), color, false);
    }

    public int drawTotalCenteredString(final String text, final float x, final float y, final int color) {
        return drawString(text, x - (getStringWidth(text) / 2f), y - (getFontHeight() / 2), color, false);
    }

    @Override
    public int drawString(String text, float x, float y, final int color, final boolean shadow) {
        y += 2;
        calculateColorCodes();
        FontCharacter[] characterSet = defaultCharacters;

        double givenX = x;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glScalef(SCALE, SCALE, SCALE);

        x -= MARGIN_WIDTH / SCALE_INVERSE;
        y -= MARGIN_WIDTH / SCALE_INVERSE;
        x *= SCALE_INVERSE;
        y *= SCALE_INVERSE;
        y -= fontHeight / 5;

        final int shadowColor = Color.BLACK.getRGB();

        final float startX = x;

        final int length = text.length();
        RenderUtil.color(shadow ? shadowColor : color);
        char previousCharacter = '.';

        for (int i = 0; i < length; ++i) {
            final char character = text.charAt(i);

            try {
                if (character == '\n') {
                    x = startX;
                    y += getFontHeight() * 2;
                    continue;
                }

                if (previousCharacter != COLOR_INVOKER) {
                    if (character == COLOR_INVOKER && i + 1 < length) {
                        final int index = COLOR_CODE_CHARACTERS.indexOf(text.toLowerCase().charAt(i + 1));
                        if (index < 16) {
                            RenderUtil.color(shadow ? shadowColor : COLOR_CODES[index]);
                        } else if (index == 17) {
                            characterSet = boldCharacters;
                        }
                    } else if (characterSet.length > character) {
                        final FontCharacter fontCharacter = characterSet[character];
                        fontCharacter.render(x, y);
                        x += fontCharacter.getWidth() - MARGIN_WIDTH * 2;
                    }
                }
            } catch (Exception exception) {
            }
            previousCharacter = character;
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(0);
        GL11.glPopAttrib();
        GL11.glPopMatrix();

        return (int) (x - givenX);
    }

    @Override
    public int getStringWidth(String text) {
        FontCharacter[] characterSet = defaultCharacters;
        final int length = text.length();
        char previousCharacter = '.';
        float width = 0;

        for (int i = 0; i < length; ++i) {
            final char character = text.charAt(i);
            if (previousCharacter != COLOR_INVOKER) {
                if (character == COLOR_INVOKER && i + 1 < length) {
                    final int index = COLOR_CODE_CHARACTERS.indexOf(text.toLowerCase().charAt(i + 1));
                    if (index < 16) {
                        characterSet = defaultCharacters;
                    } else if (index == 17) {
                        characterSet = boldCharacters;
                    }
                } else if (characterSet.length > character) {
                    width += characterSet[character].getWidth() - MARGIN_WIDTH * 2;
                }
            }
            previousCharacter = character;
        }

        return (int) (width / 2);
    }

    public float getFontHeight() {
        return FONT_HEIGHT;
    }

    @Override
    public java.util.List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return java.util.Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.sizeStringToWidth(str, wrapWidth);
        if (str.length() <= i) {
            return str;
        } else {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == ' ' || c0 == '\n';
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k) {
            char c0 = str.charAt(k);
            switch (c0) {
                case '\n':
                    --k;
                    break;
                case '\247':
                    if (k < i - 1) {
                        ++k;
                        char c1 = str.charAt(k);
                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
                    break;
                case ' ':
                    l = k;
                default:
                    j += this.getStringWidth(String.valueOf(c0));
                    if (flag) {
                        ++j;
                    }
            }

            if (c0 == '\n') {
                l = ++k;
                break;
            }

            if (j > wrapWidth) {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    public static String getFormatFromString(String p_78282_0_) {
        String s = "";
        int i = -1;
        int j = p_78282_0_.length();

        while ((i = p_78282_0_.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = p_78282_0_.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s = "\247" + c0;
                } else if (isFormatSpecial(c0)) {
                    s = s + "\247" + c0;
                }
            }
        }

        return s;
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }
}