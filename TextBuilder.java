package x7k2m9.rendersystem.render2d.text;

import x7k2m9.rendersystem.font.msdf.MsdfFont;

public class TextBuilder {
    private MsdfFont font;
    private String text;
    private float size;
    private float thickness = 0.5f;
    private int color = -1;
    private float smoothness = 0.1f;
    private float spacing = 0.0f;
    private int outlineColor = 0;
    private float outlineThickness = 0.0f;
    private boolean useGradient = false;
    private int gradientColor1 = -14737633;
    private int gradientColor2 = -1;
    private float gradientCenterX = 0.5f;
    private float gradientCenterY = 1.0f;
    private float glowRadius = 0.0f;
    private float glowIntensity = 0.0f;
    private float shadowOffsetX = 0.0f;
    private float shadowOffsetY = 0.0f;
    private float shadowBlur = 0.0f;
    private float shadowIntensity = 0.0f;

    public TextBuilder font(MsdfFont font) {
        this.font = font;
        return this;
    }

    public TextBuilder text(String text) {
        this.text = text;
        return this;
    }

    public TextBuilder size(float size) {
        this.size = size;
        return this;
    }

    public TextBuilder thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public TextBuilder color(int color) {
        this.color = color;
        return this;
    }

    public TextBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public TextBuilder spacing(float spacing) {
        this.spacing = spacing;
        return this;
    }

    public TextBuilder outline(int color, float thickness) {
        this.outlineColor = color;
        this.outlineThickness = thickness;
        return this;
    }

    public TextBuilder gradient(boolean enabled) {
        this.useGradient = enabled;
        return this;
    }

    public TextBuilder gradientColors(int color1, int color2) {
        this.gradientColor1 = color1;
        this.gradientColor2 = color2;
        return this;
    }

    public TextBuilder gradientCenter(float x, float y) {
        this.gradientCenterX = x;
        this.gradientCenterY = y;
        return this;
    }

    public TextBuilder glow(float radius, float intensity) {
        this.glowRadius = radius;
        this.glowIntensity = intensity;
        return this;
    }

    public TextBuilder shadow(float offsetX, float offsetY, float blur, float intensity) {
        this.shadowOffsetX = offsetX;
        this.shadowOffsetY = offsetY;
        this.shadowBlur = blur;
        this.shadowIntensity = intensity;
        return this;
    }

    public BuiltText build() {
        return new BuiltText(
            font, text, size, thickness, color, smoothness, spacing,
            outlineColor, outlineThickness, useGradient,
            gradientColor1, gradientColor2, gradientCenterX, gradientCenterY,
            glowRadius, glowIntensity,
            shadowOffsetX, shadowOffsetY, shadowBlur, shadowIntensity
        );
    }
}
