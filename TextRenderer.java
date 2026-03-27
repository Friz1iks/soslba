package x7k2m9.rendersystem.render2d.text;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import x7k2m9.rendersystem.font.FontManager;
import x7k2m9.rendersystem.font.msdf.MsdfFont;

public class TextRenderer {
    
    public static TextBuilder builder() {
        return new TextBuilder();
    }
    
    public static void drawString(DrawContext context, String text, float x, float y, int color) {
        drawString(context, text, x, y, color, FontManager.SIZE_NORMAL);
    }
    
    public static void drawString(DrawContext context, String text, float x, float y, int color, float size) {
        if (text == null || text.isEmpty()) return;
        if (FontManager.msdfBold == null) return;
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        BuiltText builtText = builder()
            .font(FontManager.msdfBold)
            .text(text)
            .size(size)
            .color(color)
            .build();
        
        builtText.render(matrix, x, y, 0.0f);
    }
    
    public static void drawStringWithShadow(DrawContext context, String text, float x, float y, int color) {
        drawStringWithShadow(context, text, x, y, color, FontManager.SIZE_NORMAL);
    }
    
    public static void drawStringWithShadow(DrawContext context, String text, float x, float y, int color, float size) {
        if (text == null || text.isEmpty()) return;
        if (FontManager.msdfBold == null) return;
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        BuiltText builtText = builder()
            .font(FontManager.msdfBold)
            .text(text)
            .size(size)
            .color(color)
            .shadow(0.1f, 0.15f, 0.25f, 0.5f)
            .build();
        
        builtText.render(matrix, x, y, 0.0f);
    }
    
    public static void drawStringWithOutline(DrawContext context, String text, float x, float y, int color, int outlineColor) {
        drawStringWithOutline(context, text, x, y, color, outlineColor, FontManager.SIZE_NORMAL);
    }
    
    public static void drawStringWithOutline(DrawContext context, String text, float x, float y, int color, int outlineColor, float size) {
        if (text == null || text.isEmpty()) return;
        if (FontManager.msdfBold == null) return;
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        BuiltText builtText = builder()
            .font(FontManager.msdfBold)
            .text(text)
            .size(size)
            .color(color)
            .outline(outlineColor, 0.15f)
            .build();
        
        builtText.render(matrix, x, y, 0.0f);
    }
    
    public static void drawGradientString(DrawContext context, String text, float x, float y, int color1, int color2) {
        drawGradientString(context, text, x, y, color1, color2, FontManager.SIZE_NORMAL);
    }
    
    public static void drawGradientString(DrawContext context, String text, float x, float y, int color1, int color2, float size) {
        if (text == null || text.isEmpty()) return;
        if (FontManager.msdfBold == null) return;
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        BuiltText builtText = builder()
            .font(FontManager.msdfBold)
            .text(text)
            .size(size)
            .color(-1)
            .gradient(true)
            .gradientColors(color1, color2)
            .build();
        
        builtText.render(matrix, x, y, 0.0f);
    }
    
    public static void drawCenteredString(DrawContext context, String text, float x, float y, int color) {
        drawCenteredString(context, text, x, y, color, FontManager.SIZE_NORMAL);
    }
    
    public static void drawCenteredString(DrawContext context, String text, float x, float y, int color, float size) {
        float width = getStringWidth(text, size);
        drawString(context, text, x - width / 2, y, color, size);
    }
    
    public static float getStringWidth(String text) {
        return getStringWidth(text, FontManager.SIZE_NORMAL);
    }
    
    public static float getStringWidth(String text, float size) {
        if (text == null || text.isEmpty()) return 0;
        if (FontManager.msdfBold == null) return 0;
        
        float scale = size / 32.0f;
        return FontManager.msdfBold.getWidth(text, scale);
    }
    
    public static float getStringHeight(float size) {
        if (FontManager.msdfBold == null) return size;
        
        float scale = size / 32.0f;
        return FontManager.msdfBold.getMetrics().getLineHeight() * scale;
    }
    
    public static void cleanup() {
        BuiltText.cleanup();
    }
}
