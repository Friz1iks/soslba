package x7k2m9.rendersystem.font.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import x7k2m9.rendersystem.shader.MsdfShaderProgram;

public class MsdfBuiltText {
    private static MsdfShaderProgram shader;
    
    private final MsdfFont font;
    private final String text;
    private final float size;
    private final float thickness;
    private final int color;
    private final float smoothness;
    private final float spacing;

    public MsdfBuiltText(MsdfFont font, String text, float size, float thickness, int color, float smoothness, float spacing) {
        this.font = font;
        this.text = text;
        this.size = size;
        this.thickness = thickness;
        this.color = color;
        this.smoothness = smoothness;
        this.spacing = spacing;
    }
    
    private static void ensureShader() {
        if (shader == null) {
            try {
                System.out.println("[MsdfBuiltText] Creating MSDF shader...");
                shader = new MsdfShaderProgram();
                System.out.println("[MsdfBuiltText] MSDF shader created successfully!");
            } catch (Exception e) {
                System.err.println("[MsdfBuiltText] Failed to create shader: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void render(Matrix4f matrix, float x, float y, float z) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        ensureShader();
        
        if (shader == null) {
            System.err.println("[MsdfBuiltText] Shader is null, cannot render");
            return;
        }

        int textureId = font.getTextureId();
        if (textureId <= 0) {
            System.err.println("[MsdfBuiltText] Invalid texture ID: " + textureId);
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, textureId);

        float scale = size / 32.0f;
        float baselineOffset = font.getMetrics().getLineHeight() + font.getMetrics().getDescender();
        
        // Use MSDF shader
        shader.use();
        shader.setModelViewMat(matrix);
        shader.setProjMat(RenderSystem.getProjectionMatrix());
        shader.setSampler0(0);
        shader.setRange(font.getAtlas().getRange());
        shader.setThickness(thickness);
        shader.setSmoothness(smoothness);
        shader.setOutline(false);
        shader.setUseGradient(false);
        
        // Set text color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        shader.setTextColor(r, g, b, a);

        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        font.render(matrix, buffer, text, scale, 0.0f, spacing, x, y + baselineOffset * scale, z, -1);
        
        try {
            var builtBuffer = buffer.end();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
        } catch (IllegalStateException e) {
            // Buffer was empty - no glyphs were rendered (missing characters in font)
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void cleanup() {
        if (shader != null) {
            shader.delete();
            shader = null;
        }
    }
}
