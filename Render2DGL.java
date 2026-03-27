package x7k2m9.rendersystem.render2d;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import x7k2m9.base.utils.render.GLStateBackup;
import x7k2m9.rendersystem.render2d.arc.ArcGL;
import x7k2m9.rendersystem.render2d.blur.KawaseGL;
import x7k2m9.rendersystem.render2d.glass.LiquidGlassGL;
import x7k2m9.rendersystem.render2d.glow.GlowGL;
import x7k2m9.rendersystem.render2d.glow.GlowOutlineGL;
import x7k2m9.rendersystem.render2d.outline.OutlineGL;
import x7k2m9.rendersystem.render2d.rectangle.RectangleGL;

public class Render2DGL {
    private static final float Z_OVERRIDE = 0.0f;
    private static final int FIXED_GUI_SCALE = 2;

    private static final ThreadLocal<GLStateBackup> stateBackup = new ThreadLocal<>();

    public static int getFixedScaledWidth() {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return 854 / 2;
        return mc.getWindow().getScaledWidth();
    }

    public static int getFixedScaledHeight() {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return 480 / 2;
        return mc.getWindow().getScaledHeight();
    }

    public static Matrix4f createProjection() {
        int w = getFixedScaledWidth();
        int h = getFixedScaledHeight();
        if (w <= 0 || h <= 0) {
            var window = MinecraftClient.getInstance().getWindow();
            w = Math.max(1, (int) (window.getWidth() / FIXED_GUI_SCALE));
            h = Math.max(1, (int) (window.getHeight() / FIXED_GUI_SCALE));
        }
        return new Matrix4f().ortho(0, (float) w, (float) h, 0, -1000, 1000);
    }

    public static void beginCustomRender() {
        stateBackup.set(GLStateBackup.save());

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endCustomRender() {
        GLStateBackup backup = stateBackup.get();
        if (backup != null) {
            backup.restore();
            stateBackup.remove();
        }
    }

    public static void rect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        rect(createProjection(), x, y, width, height, radius, radius, radius, radius, color);
    }

    public static void rect(DrawContext context, float x, float y, float width, float height, float radius, int... colors) {
        rect(createProjection(), x, y, width, height, radius, radius, radius, radius, colors);
    }

    public static void rect(DrawContext context, float x, float y, float width, float height,
                            float tl, float tr, float br, float bl, int... colors) {
        rect(createProjection(), x, y, width, height, tl, tr, br, bl, colors);
    }

    public static void rect(Matrix4f projection, float x, float y, float width, float height,
                            float tl, float tr, float br, float bl, int... colors) {
        RectangleGL.draw(projection, x, y, width, height, tl, tr, br, bl, Z_OVERRIDE, colors);
    }

    public static void blur(DrawContext context, float x, float y, float width, float height, float radius, float strength) {
        KawaseGL.draw(createProjection(), x, y, width, height, radius, strength, Z_OVERRIDE);
    }

    public static void blur(Matrix4f projection, float x, float y, float width, float height, float radius, float strength) {
        KawaseGL.draw(projection, x, y, width, height, radius, strength, Z_OVERRIDE);
    }

    public static void liquidGlass(DrawContext context, float x, float y, float width, float height,
                                   float squirt, float power, float radius, int color) {
        LiquidGlassGL.draw(createProjection(), x, y, width, height, squirt, power, radius, color, Z_OVERRIDE);
    }

    public static void liquidGlass(Matrix4f projection, float x, float y, float width, float height,
                                   float squirt, float power, float radius, int color) {
        LiquidGlassGL.draw(projection, x, y, width, height, squirt, power, radius, color, Z_OVERRIDE);
    }

    public static void arc(DrawContext context, float x, float y, float size, float thickness,
                           float degree, float rotation, int color) {
        ArcGL.draw(createProjection(), x, y, size, thickness, degree, rotation, Z_OVERRIDE, color);
    }

    public static void arc(DrawContext context, float x, float y, float size, float thickness,
                           float degree, float rotation, int... colors) {
        ArcGL.draw(createProjection(), x, y, size, thickness, degree, rotation, Z_OVERRIDE, colors);
    }

    public static void arc(Matrix4f projection, float x, float y, float size, float thickness,
                           float degree, float rotation, int... colors) {
        ArcGL.draw(projection, x, y, size, thickness, degree, rotation, Z_OVERRIDE, colors);
    }

    public static void glow(DrawContext context, float x, float y, float size, int color) {
        GlowGL.draw(createProjection(), x, y, size, color, Z_OVERRIDE);
    }

    public static void glow(Matrix4f projection, float x, float y, float size, int color) {
        GlowGL.draw(projection, x, y, size, color, Z_OVERRIDE);
    }

    public static void glowOutline(DrawContext context, float x, float y, float width, float height,
                                   float thickness, int color, float radius, float progress, float baseAlpha) {
        GlowOutlineGL.draw(createProjection(), x, y, width, height, thickness, color, radius, progress, baseAlpha, Z_OVERRIDE);
    }

    public static void glowOutline(Matrix4f projection, float x, float y, float width, float height,
                                   float thickness, int color, float radius, float progress, float baseAlpha) {
        GlowOutlineGL.draw(projection, x, y, width, height, thickness, color, radius, progress, baseAlpha, Z_OVERRIDE);
    }

    public static void outline(DrawContext context, float x, float y, float width, float height,
                               float radius, float thickness, int color) {
        outline(createProjection(), x, y, width, height, radius, radius, radius, radius, thickness, color);
    }

    public static void outline(DrawContext context, float x, float y, float width, float height,
                               float tl, float tr, float br, float bl, float thickness, int color) {
        outline(createProjection(), x, y, width, height, tl, tr, br, bl, thickness, color);
    }

    public static void outline(DrawContext context, float x, float y, float width, float height,
                               float radius, float thickness, int... colors) {
        outline(createProjection(), x, y, width, height, radius, radius, radius, radius, thickness, colors);
    }

    public static void outline(DrawContext context, float x, float y, float width, float height,
                               float tl, float tr, float br, float bl, float thickness, int... colors) {
        outline(createProjection(), x, y, width, height, tl, tr, br, bl, thickness, colors);
    }

    public static void outline(Matrix4f projection, float x, float y, float width, float height,
                               float tl, float tr, float br, float bl, float thickness, int... colors) {
        OutlineGL.draw(projection, x, y, width, height, tl, tr, br, bl, thickness, Z_OVERRIDE, colors);
    }

    public static void cleanup() {
        RectangleGL.cleanup();
        KawaseGL.cleanup();
        LiquidGlassGL.cleanup();
        ArcGL.cleanup();
        GlowGL.cleanup();
        GlowOutlineGL.cleanup();
        OutlineGL.cleanup();
    }

    public static void enableScissor(int x, int y, int width, int height) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int windowHeight = mc.getWindow().getHeight();
        int scale = (int) mc.getWindow().getScaleFactor();

        int scaledX = x * scale;
        int scaledY = windowHeight - (y + height) * scale;
        int scaledWidth = width * scale;
        int scaledHeight = height * scale;

        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scaledX, scaledY, scaledWidth, scaledHeight);
    }

    public static void disableScissor() {
        com.mojang.blaze3d.systems.RenderSystem.disableScissor();
    }

    public static void drawRect(float x, float y, float width, float height, int color, float radius) {
        rect(createProjection(), x, y, width, height, radius, radius, radius, radius, color);
    }

    public static void drawOutline(float x, float y, float width, float height, float thickness, int color, float radius) {
        outline(createProjection(), x, y, width, height, radius, radius, radius, radius, thickness, color);
    }

    public static void gradientRect(DrawContext context, float x, float y, float width, float height,
                                    int topLeft, int topRight, int bottomLeft, int bottomRight, float radius) {
        rect(createProjection(), x, y, width, height, radius, radius, radius, radius, topLeft, topRight, bottomRight, bottomLeft);
    }

    public static void texture(DrawContext context, net.minecraft.util.Identifier texture,
                               float x, float y, float width, float height,
                               float u0, float v0, float u1, float v1,
                               int color, float alpha, float radius) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, texture,
                (int)x, (int)y, 0f, 0f, (int)width, (int)height, (int)width, (int)height);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void restoreMinecraftState() {

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void prepareForTextRendering() {
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
    }

    public static void drawText(DrawContext context, String text, float x, float y, int color, float size) {
        prepareForTextRendering();
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        if (font != null) {
            font.drawString(context, text, x, y, color, size);
        }
    }

    public static void drawTextWithShadow(DrawContext context, String text, float x, float y, int color, float size) {
        prepareForTextRendering();
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        if (font != null) {
            font.drawString(context, text, x, y, color, true);
        }
    }

    public static void drawCenteredText(DrawContext context, String text, float x, float y, int color, float size) {
        prepareForTextRendering();
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        if (font != null) {
            float width = font.getStringWidth(text, size);
            font.drawString(context, text, x - width / 2, y, color, size);
        }
    }

    public static void drawCenteredTextWithShadow(DrawContext context, String text, float x, float y, int color, float size) {
        prepareForTextRendering();
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        if (font != null) {
            float width = font.getStringWidth(text, size);
            font.drawString(context, text, x - width / 2, y, color, true);
        }
    }

    public static float getTextWidth(String text, float size) {
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        return font != null ? font.getStringWidth(text, size) : 0;
    }

    public static float getTextHeight(float size) {
        var font = x7k2m9.rendersystem.font.FontManager.getFont((int)(size));
        return font != null ? font.getStringHeight(size) : size;
    }
}