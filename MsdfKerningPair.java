package x7k2m9.rendersystem.font.msdf;

import com.google.gson.annotations.SerializedName;

public class MsdfKerningPair {
    @SerializedName("unicode1")
    private int leftChar;
    @SerializedName("unicode2")
    private int rightChar;
    private float advance;

    public int getLeftChar() {
        return this.leftChar;
    }

    public int getRightChar() {
        return this.rightChar;
    }

    public float getAdvance() {
        return this.advance;
    }
}
