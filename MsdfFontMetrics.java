package x7k2m9.rendersystem.font.msdf;

import com.google.gson.annotations.SerializedName;

public class MsdfFontMetrics {
    @SerializedName("lineHeight")
    private float lineHeight;
    @SerializedName("ascender")
    private float ascender;
    @SerializedName("descender")
    private float descender;

    public float getLineHeight() {
        return this.lineHeight;
    }

    public float getAscender() {
        return this.ascender;
    }

    public float getDescender() {
        return this.descender;
    }

    public float getBaselineOffset() {
        return this.lineHeight + this.descender;
    }
}
