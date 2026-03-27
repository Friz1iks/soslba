package x7k2m9.rendersystem.font;

import net.minecraft.client.gui.DrawContext;

public interface FontRenderer {
    float drawString(DrawContext context, String text, float x, float y, int color, boolean shadow);
    float drawString(DrawContext context, String text, float x, float y, int color, float size);
    float getWidth(String text);
    float getStringWidth(String text, float size);
    float getStringHeight(float size);
    float getHeight();
    void cleanup();
    void processPendingGlyphs();
    void regenerateCache();
    default void ensureAtlasReady() {}
}