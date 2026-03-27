package x7k2m9.rendersystem.font;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import x7k2m9.rendersystem.font.msdf.MsdfFont;
import x7k2m9.rendersystem.font.msdf.MsdfBuiltText;

public class MSDFFontRendererImpl implements FontRenderer {
    private final MsdfFont font;
    private final float defaultSize;

    public MSDFFontRendererImpl(MsdfFont font, float defaultSize) {
        this.font = font;
        this.defaultSize = defaultSize;
    }

    @Override
    public float drawString(DrawContext context, String text, float x, float y, int color, boolean shadow) {
        return drawString(context, text, x, y, color, defaultSize);
    }

    @Override
    public float drawString(DrawContext context, String text, float x, float y, int color, float size) {
        if (text == null || text.isEmpty()) {
            return x;
        }

        try {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            
            MsdfBuiltText builtText = new MsdfBuiltText(font, text, size, 0.5f, color, 0.1f, 0.0f);
            builtText.render(matrix, x, y, 0.0f);
            
            float scale = size / 32.0f;
            return x + font.getWidth(text, scale);
        } catch (Exception e) {
            System.err.println("[MSDFFontRenderer] Error drawing string '" + text + "': " + e.getMessage());
            e.printStackTrace();
            return x;
        }
    }

    @Override
    public float getWidth(String text) {
        return getStringWidth(text, defaultSize);
    }

    @Override
    public float getStringWidth(String text, float size) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        float scale = size / 32.0f;
        return font.getWidth(text, scale);
    }

    @Override
    public float getStringHeight(float size) {
        float scale = size / 32.0f;
        return font.getMetrics().getLineHeight() * scale;
    }

    @Override
    public float getHeight() {
        float scale = defaultSize / 32.0f;
        return font.getMetrics().getLineHeight() * scale;
    }

    @Override
    public void cleanup() {
        MsdfBuiltText.cleanup();
    }

    @Override
    public void processPendingGlyphs() {
    }

    @Override
    public void regenerateCache() {
    }

    @Override
    public void ensureAtlasReady() {
    }
}