package x7k2m9.rendersystem.font.msdf;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import org.joml.Matrix4f;
import java.util.Map;

public class MsdfFont {
    private final String name;
    private final AbstractTexture texture;
    private final MsdfFontAtlas atlas;
    private final MsdfFontMetrics metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;

    public MsdfFont(String name, AbstractTexture texture, MsdfFontAtlas atlas, MsdfFontMetrics metrics, 
                    Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
        this.name = name;
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public int getTextureId() {
        return this.texture.getGlId();
    }

    public void render(Matrix4f matrix, VertexConsumer consumer, String text, float scale, 
                      float letterSpacing, float spacing, float x, float y, float z, int color) {
        int prevChar = -1;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int unicode = (int) c;
            MsdfGlyph glyph = this.glyphs.get(unicode);
            
            if (glyph == null && unicode == 95) {
                glyph = this.glyphs.get(45);
            }
            
            if (glyph != null) {
                Map<Integer, Float> kerningMap = this.kernings.get(prevChar);
                if (kerningMap != null) {
                    x += kerningMap.getOrDefault(unicode, 0.0F) * scale;
                }

                x += glyph.render(matrix, consumer, scale, x, y, z, color) + letterSpacing + spacing;
                prevChar = unicode;
            } else {
                MsdfGlyph spaceGlyph = this.glyphs.get(32);
                if (spaceGlyph != null) {
                    x += spaceGlyph.getAdvance(scale);
                }
            }
        }
    }

    public float getWidth(String text, float scale) {
        int prevChar = -1;
        float width = 0.0F;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int unicode = (int) c;
            MsdfGlyph glyph = this.glyphs.get(unicode);
            
            if (glyph == null && unicode == 95) {
                glyph = this.glyphs.get(45);
            }
            
            if (glyph != null) {
                Map<Integer, Float> kerningMap = this.kernings.get(prevChar);
                if (kerningMap != null) {
                    width += kerningMap.getOrDefault(unicode, 0.0F) * scale;
                }

                width += glyph.getAdvance(scale);
                prevChar = unicode;
            } else {
                MsdfGlyph spaceGlyph = this.glyphs.get(32);
                if (spaceGlyph != null) {
                    width += spaceGlyph.getAdvance(scale);
                }
            }
        }

        return width;
    }

    public MsdfFontAtlas getAtlas() {
        return this.atlas;
    }

    public MsdfFontMetrics getMetrics() {
        return this.metrics;
    }

    public String getName() {
        return this.name;
    }
    
    public Map<Integer, MsdfGlyph> getGlyphs() {
        return this.glyphs;
    }
}
