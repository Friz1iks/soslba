package x7k2m9.rendersystem.font.msdf;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public class MsdfGlyph {
    private final int unicode;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float advance;
    private final float topPosition;
    private final float width;
    private final float height;

    public MsdfGlyph(MsdfGlyphDescriptor descriptor, float atlasWidth, float atlasHeight) {
        this.unicode = descriptor.getUnicode();
        this.advance = descriptor.getAdvance();
        
        MsdfRectBounds atlasBounds = descriptor.getAtlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.getLeft() / atlasWidth;
            this.maxU = atlasBounds.getRight() / atlasWidth;
            this.minV = 1.0F - atlasBounds.getBottom() / atlasHeight;
            this.maxV = 1.0F - atlasBounds.getTop() / atlasHeight;
        } else {
            this.minU = this.maxU = this.minV = this.maxV = 0.0F;
        }

        MsdfRectBounds planeBounds = descriptor.getPlaneBounds();
        if (planeBounds != null) {
            this.width = planeBounds.getRight() - planeBounds.getLeft();
            this.height = planeBounds.getBottom() - planeBounds.getTop();
            this.topPosition = planeBounds.getBottom();
        } else {
            this.width = this.height = this.topPosition = 0.0F;
        }
    }

    public float render(Matrix4f matrix, VertexConsumer consumer, float scale, float x, float y, float z, int color) {
        y -= this.topPosition * scale;
        float w = this.width * scale;
        float h = this.height * scale;
        
        consumer.vertex(matrix, x, y, z).texture(this.minU, this.minV).color(color);
        consumer.vertex(matrix, x, y + h, z).texture(this.minU, this.maxV).color(color);
        consumer.vertex(matrix, x + w, y + h, z).texture(this.maxU, this.maxV).color(color);
        consumer.vertex(matrix, x + w, y, z).texture(this.maxU, this.minV).color(color);
        
        return this.advance * scale;
    }

    public float getAdvance(float scale) {
        return this.advance * scale;
    }

    public int getUnicode() {
        return this.unicode;
    }

    public float getMinU() {
        return this.minU;
    }

    public float getMaxU() {
        return this.maxU;
    }

    public float getMinV() {
        return this.minV;
    }

    public float getMaxV() {
        return this.maxV;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getTopPosition() {
        return this.topPosition;
    }
}
