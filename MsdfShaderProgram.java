package x7k2m9.rendersystem.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class MsdfShaderProgram {
    private static final String VERTEX_SHADER = """
        #version 150
        
        in vec3 Position;
        in vec2 UV0;
        in vec4 Color;
        
        uniform mat4 ModelViewMat;
        uniform mat4 ProjMat;
        
        out vec2 texCoord0;
        out vec4 vertexColor;
        
        void main() {
            gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
            texCoord0 = UV0;
            vertexColor = Color;
        }
        """;
    
    private static final String FRAGMENT_SHADER = """
        #version 150
        
        uniform sampler2D Sampler0;
        uniform float Range;
        uniform float Thickness;
        uniform float Smoothness;
        uniform int Outline;
        uniform float OutlineThickness;
        uniform vec4 OutlineColor;
        uniform vec4 TextColor;
        uniform int UseGradient;
        uniform vec4 GradientColor1;
        uniform vec4 GradientColor2;
        uniform vec2 GradientCenter;
        
        in vec2 texCoord0;
        in vec4 vertexColor;
        
        out vec4 fragColor;
        
        float median(float r, float g, float b) {
            return max(min(r, g), min(max(r, g), b));
        }
        
        float screenPxRange() {
            vec2 unitRange = vec2(Range) / vec2(textureSize(Sampler0, 0));
            vec2 screenTexSize = vec2(1.0) / fwidth(texCoord0);
            return max(0.5 * dot(unitRange, screenTexSize), 1.0);
        }
        
        void main() {
            vec4 mtsdf = texture(Sampler0, texCoord0);
            float sd = median(mtsdf.r, mtsdf.g, mtsdf.b);
            float pxRange = screenPxRange();
            
            float thickness = Thickness;
            float smoothness = Smoothness;
            
            float screenPxDist = pxRange * (sd - thickness);
            float alpha = clamp(screenPxDist / smoothness + 0.5, 0.0, 1.0);
            
            vec4 finalColor = TextColor;
            
            if (UseGradient == 1) {
                float gradientFactor = texCoord0.y;
                finalColor = mix(GradientColor1, GradientColor2, gradientFactor);
            }
            
            if (Outline == 1) {
                float outlineThickness = OutlineThickness;
                float outlineDist = pxRange * (sd - thickness + outlineThickness);
                float outlineAlpha = clamp(outlineDist / smoothness + 0.5, 0.0, 1.0);
                
                if (alpha < 0.5 && outlineAlpha > 0.5) {
                    finalColor = OutlineColor;
                    alpha = outlineAlpha;
                }
            }
            
            if (alpha < 0.01) discard;
            
            fragColor = vec4(finalColor.rgb, finalColor.a * alpha * vertexColor.a);
        }
        """;
    
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    
    private int modelViewMatLocation;
    private int projMatLocation;
    private int sampler0Location;
    private int rangeLocation;
    private int thicknessLocation;
    private int smoothnessLocation;
    private int outlineLocation;
    private int outlineThicknessLocation;
    private int outlineColorLocation;
    private int textColorLocation;
    private int useGradientLocation;
    private int gradientColor1Location;
    private int gradientColor2Location;
    private int gradientCenterLocation;
    
    public MsdfShaderProgram() {
        compile();
    }
    
    private void compile() {
        vertexShaderId = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        fragmentShaderId = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        
        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        
        GL20.glBindAttribLocation(programId, 0, "Position");
        GL20.glBindAttribLocation(programId, 1, "UV0");
        GL20.glBindAttribLocation(programId, 2, "Color");
        
        GL20.glLinkProgram(programId);
        
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programId);
            throw new RuntimeException("Failed to link MSDF shader program: " + log);
        }
        
        modelViewMatLocation = GL20.glGetUniformLocation(programId, "ModelViewMat");
        projMatLocation = GL20.glGetUniformLocation(programId, "ProjMat");
        sampler0Location = GL20.glGetUniformLocation(programId, "Sampler0");
        rangeLocation = GL20.glGetUniformLocation(programId, "Range");
        thicknessLocation = GL20.glGetUniformLocation(programId, "Thickness");
        smoothnessLocation = GL20.glGetUniformLocation(programId, "Smoothness");
        outlineLocation = GL20.glGetUniformLocation(programId, "Outline");
        outlineThicknessLocation = GL20.glGetUniformLocation(programId, "OutlineThickness");
        outlineColorLocation = GL20.glGetUniformLocation(programId, "OutlineColor");
        textColorLocation = GL20.glGetUniformLocation(programId, "TextColor");
        useGradientLocation = GL20.glGetUniformLocation(programId, "UseGradient");
        gradientColor1Location = GL20.glGetUniformLocation(programId, "GradientColor1");
        gradientColor2Location = GL20.glGetUniformLocation(programId, "GradientColor2");
        gradientCenterLocation = GL20.glGetUniformLocation(programId, "GradientCenter");
    }
    
    private int compileShader(int type, String source) {
        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);
        
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId);
            String shaderType = type == GL20.GL_VERTEX_SHADER ? "vertex" : "fragment";
            throw new RuntimeException("Failed to compile MSDF " + shaderType + " shader: " + log);
        }
        
        return shaderId;
    }
    
    public void use() {
        GL20.glUseProgram(programId);
    }
    
    public void setModelViewMat(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(modelViewMatLocation, false, buffer);
    }
    
    public void setProjMat(Matrix4f matrix) {
        float[] buffer = new float[16];
        matrix.get(buffer);
        GL20.glUniformMatrix4fv(projMatLocation, false, buffer);
    }
    
    public void setSampler0(int textureUnit) {
        GL20.glUniform1i(sampler0Location, textureUnit);
    }
    
    public void setRange(float range) {
        GL20.glUniform1f(rangeLocation, range);
    }
    
    public void setThickness(float thickness) {
        GL20.glUniform1f(thicknessLocation, thickness);
    }
    
    public void setSmoothness(float smoothness) {
        GL20.glUniform1f(smoothnessLocation, smoothness);
    }
    
    public void setOutline(boolean outline) {
        GL20.glUniform1i(outlineLocation, outline ? 1 : 0);
    }
    
    public void setOutlineThickness(float thickness) {
        GL20.glUniform1f(outlineThicknessLocation, thickness);
    }
    
    public void setOutlineColor(float r, float g, float b, float a) {
        GL20.glUniform4f(outlineColorLocation, r, g, b, a);
    }
    
    public void setTextColor(float r, float g, float b, float a) {
        GL20.glUniform4f(textColorLocation, r, g, b, a);
    }
    
    public void setUseGradient(boolean useGradient) {
        GL20.glUniform1i(useGradientLocation, useGradient ? 1 : 0);
    }
    
    public void setGradientColor1(float r, float g, float b, float a) {
        GL20.glUniform4f(gradientColor1Location, r, g, b, a);
    }
    
    public void setGradientColor2(float r, float g, float b, float a) {
        GL20.glUniform4f(gradientColor2Location, r, g, b, a);
    }
    
    public void setGradientCenter(float x, float y) {
        GL20.glUniform2f(gradientCenterLocation, x, y);
    }
    
    public void delete() {
        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);
        GL20.glDeleteProgram(programId);
    }
}
