package x7k2m9.rendersystem.font.msdf;

public class MsdfGlyphDescriptor {
    private int unicode;
    private int index;
    private float advance;
    private MsdfRectBounds planeBounds;
    private MsdfRectBounds atlasBounds;

    public int getUnicode() {
        return this.unicode;
    }

    public float getAdvance() {
        return this.advance;
    }

    public MsdfRectBounds getPlaneBounds() {
        return this.planeBounds;
    }

    public MsdfRectBounds getAtlasBounds() {
        return this.atlasBounds;
    }
}
