package x7k2m9.rendersystem.render2d.text;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import x7k2m9.rendersystem.font.msdf.MsdfFont;
import x7k2m9.rendersystem.shader.MsdfShaderProgram;

import java.nio.ByteBuffer;

public class BuiltText {
    private static MsdfShaderProgram shader;
    private static final float[] colorBuffer = new float[4];
    private static final float[] glowOffsets = new float[16];
    private static final float[] glowOffsetsY = new float[16];
    
    static {
        for (int i = 0; i < 16; i++) {
            double angle = i * 2.0 * Math.PI / 16.0;
            glowOffsets[i] = (float) Math.cos(angle);
            glowOffsetsY[i] = (float) Math.sin(angle);
        }
    }
    
    private final MsdfFont font;
    private final String text;
    private final float size;
    private final float thickness;
    private final int color;
    private final float smoothness;
    private final float spacing;
    private final int outlineColor;
    private final float outlineThickness;
    private final boolean useGradient;
    private final int gradientColor1;
    private final int gradientColor2;
    private final float gradientCenterX;
    private final float gradientCenterY;
    private final float glowRadius;
    private final float glowIntensity;
    private final float shadowOffsetX;
    private final float shadowOffsetY;
    private final float shadowBlur;
    private final float shadowIntensity;
    
    public BuiltText(MsdfFont font, String text, float size, float thickness, int color,
                     float smoothness, float spacing, int outlineColor, float outlineThickness,
                     boolean useGradient, int gradientColor1, int gradientColor2,
                     float gradientCenterX, float gradientCenterY,
                     float glowRadius, float glowIntensity,
                     float shadowOffsetX, float shadowOffsetY, float shadowBlur, float shadowIntensity) {
        this.font = font;
        this.text = text;
        this.size = size;
        this.thickness = thickness;
        this.color = color;
        this.smoothness = smoothness;
        this.spacing = spacing;
        this.outlineColor = outlineColor;
        this.outlineThickness = outlineThickness;
        this.useGradient = useGradient;
        this.gradientColor1 = gradientColor1;
        this.gradientColor2 = gradientColor2;
        this.gradientCenterX = gradientCenterX;
        this.gradientCenterY = gradientCenterY;
        this.glowRadius = glowRadius;
        this.glowIntensity = glowIntensity;
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;
        this.shadowBlur = shadowBlur;
        this.shadowIntensity = shadowIntensity;
    }
    
    private static void ensureShader() {
        if (shader == null) {
            try {
                System.out.println("[BuiltText] Creating MSDF shader...");
                shader = new MsdfShaderProgram();
                System.out.println("[BuiltText] MSDF shader created successfully!");
            } catch (Exception e) {
                System.err.println("[BuiltText] Failed to create shader: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void unpackColor(int color, float[] buffer) {
        buffer[0] = ((color >> 16) & 0xFF) / 255.0f;
        buffer[1] = ((color >> 8) & 0xFF) / 255.0f;
        buffer[2] = (color & 0xFF) / 255.0f;
        buffer[3] = ((color >> 24) & 0xFF) / 255.0f;
    }
    
    public void render(Matrix4f matrix, float x, float y, float z) {
        render(matrix, x, y, z, 1.0f);
    }
    
    public void render(Matrix4f matrix, float x, float y, float z, float alpha) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        ensureShader();
        
        if (shader == null) {
            System.err.println("[BuiltText] Shader is null, cannot render");
            return;
        }
        
        int textureId = font.getTextureId();
        if (textureId <= 0) {
            System.err.println("[BuiltText] Invalid texture ID: " + textureId);
            return;
        }
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, textureId);
        
        float scale = size / 32.0f;
        float baselineOffset = font.getMetrics().getLineHeight() + font.getMetrics().getDescender();
        
        shader.use();
        shader.setModelViewMat(matrix);
        shader.setProjMat(RenderSystem.getProjectionMatrix());
        shader.setSampler0(0);
        shader.setRange(font.getAtlas().getRange());
        
        if (shadowIntensity > 0.0f && (shadowOffsetX != 0.0f || shadowOffsetY != 0.0f || shadowBlur > 0.0f)) {
            renderShadow(matrix, x, y, z, scale, baselineOffset, alpha);
        }
        
        if (glowIntensity > 0.0f && glowRadius > 0.0f) {
            renderGlow(matrix, x, y, z, scale, baselineOffset, alpha);
        }
        
        renderMain(matrix, x, y, z, scale, baselineOffset, alpha);
        
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    
    private void renderShadow(Matrix4f matrix, float x, float y, float z, float scale, float baselineOffset, float alpha) {
        shader.setThickness(thickness);
        shader.setSmoothness(smoothness + shadowBlur * 2.0f);
        shader.setOutline(false);
        shader.setUseGradient(false);
        
        float shadowAlpha = shadowIntensity * alpha;
        int passes = shadowBlur > 0.0f ? 9 : 1;
        float passAlpha = shadowAlpha / passes;
        
        unpackColor(0xFF000000, colorBuffer);
        colorBuffer[3] = passAlpha;
        shader.setTextColor(colorBuffer[0], colorBuffer[1], colorBuffer[2], colorBuffer[3]);
        
        for (int i = 0; i < passes; i++) {
            float offsetX = shadowOffsetX;
            float offsetY = shadowOffsetY;
            
            if (passes > 1) {
                int gridX = i / 3 - 1;
                int gridY = i % 3 - 1;
                offsetX += gridX * shadowBlur * size * 0.5f;
                offsetY += gridY * shadowBlur * size * 0.5f;
            }
            
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            font.render(matrix, buffer, text, scale, 0.0f, spacing, 
                       x + offsetX, y + offsetY + baselineOffset * scale, z - 0.01f, -1);
            
            var builtBuffer = buffer.end();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
        }
    }
    
    private void renderGlow(Matrix4f matrix, float x, float y, float z, float scale, float baselineOffset, float alpha) {
        shader.setThickness(thickness - glowRadius * 0.3f);
        shader.setSmoothness(smoothness + glowRadius * 3.0f);
        shader.setOutline(false);
        shader.setUseGradient(false);
        
        float glowAlpha = glowIntensity * alpha;
        int passes = 16;
        float passAlpha = glowAlpha / passes;
        
        colorBuffer[0] = colorBuffer[1] = colorBuffer[2] = 1.0f;
        colorBuffer[3] = passAlpha;
        shader.setTextColor(colorBuffer[0], colorBuffer[1], colorBuffer[2], colorBuffer[3]);
        
        float glowOffset = glowRadius * size;
        
        for (int i = 0; i < passes; i++) {
            float offsetX = glowOffsets[i] * glowOffset;
            float offsetY = glowOffsetsY[i] * glowOffset;
            
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            font.render(matrix, buffer, text, scale, 0.0f, spacing,
                       x + offsetX, y + offsetY + baselineOffset * scale, z - 0.005f, -1);
            
            var builtBuffer = buffer.end();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
        }
    }
    
    private void renderMain(Matrix4f matrix, float x, float y, float z, float scale, float baselineOffset, float alpha) {
        shader.setThickness(thickness);
        shader.setSmoothness(smoothness);
        shader.setOutline(outlineThickness > 0.0f);
        
        if (outlineThickness > 0.0f) {
            shader.setOutlineThickness(outlineThickness);
            unpackColor(outlineColor, colorBuffer);
            colorBuffer[3] *= alpha;
            shader.setOutlineColor(colorBuffer[0], colorBuffer[1], colorBuffer[2], colorBuffer[3]);
        }
        
        unpackColor(color, colorBuffer);
        colorBuffer[3] *= alpha;
        shader.setTextColor(colorBuffer[0], colorBuffer[1], colorBuffer[2], colorBuffer[3]);
        
        shader.setUseGradient(useGradient);
        if (useGradient) {
            float[] grad1 = new float[4];
            float[] grad2 = new float[4];
            unpackColor(gradientColor1, grad1);
            unpackColor(gradientColor2, grad2);
            shader.setGradientColor1(grad1[0], grad1[1], grad1[2], grad1[3]);
            shader.setGradientColor2(grad2[0], grad2[1], grad2[2], grad2[3]);
            shader.setGradientCenter(gradientCenterX, gradientCenterY);
        }
        
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        font.render(matrix, buffer, text, scale, 0.0f, spacing,
                   x, y + baselineOffset * scale, z, -1);
        
        var builtBuffer = buffer.end();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
    }
    
    public float getWidth() {
        float scale = size / 32.0f;
        return font.getWidth(text, scale);
    }
    
    public float getHeight() {
        float scale = size / 32.0f;
        return font.getMetrics().getLineHeight() * scale;
    }
    
    public static void cleanup() {
        if (shader != null) {
            shader.delete();
            shader = null;
        }
    }
}
