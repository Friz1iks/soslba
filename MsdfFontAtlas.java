package x7k2m9.rendersystem.font.msdf;

import com.google.gson.annotations.SerializedName;

public class MsdfFontAtlas {
    @SerializedName("distanceRange")
    private float range;
    private float width;
    private float height;

    public float getRange() {
        return this.range;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }
}
