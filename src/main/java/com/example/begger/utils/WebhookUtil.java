package com.example.begger.utils;

import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

public class WebhookUtil {

    public static void sendWebhook(String webhookUrl, String content, File screenshot) {
        new Thread(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                String boundary = UUID.randomUUID().toString();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream out = connection.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true)) {

                    // Content part
                    writer.println("--" + boundary);
                    writer.println("Content-Disposition: form-data; name=\"content\"");
                    writer.println();
                    writer.println(content);

                    // File part
                    if (screenshot != null && screenshot.exists()) {
                        writer.println("--" + boundary);
                        writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + screenshot.getName() + "\"");
                        writer.println("Content-Type: image/png");
                        writer.println();
                        writer.flush();
                        Files.copy(screenshot.toPath(), out);
                        out.flush();
                        writer.println();
                    }

                    writer.println("--" + boundary + "--");
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {
                    InputStream err = connection.getErrorStream();
                    if (err != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(err));
                        String line;
                        StringBuilder sb = new StringBuilder();
                        while ((line = reader.readLine()) != null) sb.append(line);
                        System.err.println("Webhook failed (" + responseCode + "): " + sb.toString());
                    } else {
                        System.err.println("Webhook failed with response code: " + responseCode);
                    }
                }
                
                // Cleanup temp file
                if (screenshot != null && screenshot.exists() && screenshot.getName().startsWith("temp_gift_")) {
                    screenshot.delete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static File captureScreenshot() {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            int width = mc.displayWidth;
            int height = mc.displayHeight;
            net.minecraft.client.shader.Framebuffer framebuffer = mc.getFramebuffer();

            if (net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled()) {
                width = framebuffer.framebufferTextureWidth;
                height = framebuffer.framebufferTextureHeight;
            }

            int i = width * height;
            java.nio.IntBuffer pixelBuffer = org.lwjgl.BufferUtils.createIntBuffer(i);
            int[] pixelValues = new int[i];

            org.lwjgl.opengl.GL11.glPixelStorei(org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT, 1);
            org.lwjgl.opengl.GL11.glPixelStorei(org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled()) {
                org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
                org.lwjgl.opengl.GL11.glGetTexImage(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL12.GL_BGRA, org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            } else {
                org.lwjgl.opengl.GL11.glReadPixels(0, 0, width, height, org.lwjgl.opengl.GL12.GL_BGRA, org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }

            pixelBuffer.get(pixelValues);
            net.minecraft.client.renderer.texture.TextureUtil.processPixelValues(pixelValues, width, height);
            
            BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);
            
            File tempFile = File.createTempFile("temp_gift_", ".png");
            ImageIO.write(bufferedimage, "png", tempFile);
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
