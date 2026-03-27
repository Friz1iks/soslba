package x7k2m9.rendersystem.font.msdf;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MsdfFontDefinition {
    private MsdfFontAtlas atlas;
    private MsdfFontMetrics metrics;
    private List<MsdfGlyphDescriptor> glyphs;
    @SerializedName("kerning")
    private List<MsdfKerningPair> kernings;

    public MsdfFontAtlas getAtlas() {
        return this.atlas;
    }

    public MsdfFontMetrics getMetrics() {
        return this.metrics;
    }

    public List<MsdfGlyphDescriptor> getGlyphs() {
        return this.glyphs;
    }

    public List<MsdfKerningPair> getKernings() {
        return this.kernings;
    }
}
