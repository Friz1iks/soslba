package x7k2m9.rendersystem.font.msdf;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MsdfFontLoader {
    private String name = "?";
    private byte[] atlasBytes;
    private String jsonData;

    public MsdfFontLoader setName(String name) {
        this.name = name;
        return this;
    }

    public MsdfFontLoader setAtlasBytes(byte[] bytes) {
        this.atlasBytes = bytes;
        return this;
    }

    public MsdfFontLoader setJsonData(String json) {
        this.jsonData = json;
        return this;
    }

    private NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, false);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nativeImage.setColorArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }

        return nativeImage;
    }

    public MsdfFont load() {
        try {
            Gson gson = new Gson();
            MsdfFontDefinition definition = gson.fromJson(jsonData, MsdfFontDefinition.class);

            if (definition == null) {
                throw new RuntimeException("Font definition is null");
            }
            
            System.out.println("[MSDFFontLoader] Loading font: " + name + " with " + 
                (definition.getGlyphs() != null ? definition.getGlyphs().size() : 0) + " glyphs");

            AbstractTexture texture;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(atlasBytes);
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                
                if (bufferedImage == null) {
                    throw new RuntimeException("Failed to read image from bytes");
                }

                texture = new NativeImageBackedTexture(convertToNativeImage(bufferedImage));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load atlas texture", e);
            }

            RenderSystem.recordRenderCall(() -> {
                texture.setFilter(true, false);
            });

            float atlasWidth = definition.getAtlas().getWidth();
            float atlasHeight = definition.getAtlas().getHeight();

            Map<Integer, MsdfGlyph> glyphMap = definition.getGlyphs().stream()
                .filter(desc -> desc.getUnicode() > 0)
                .collect(Collectors.toMap(
                    MsdfGlyphDescriptor::getUnicode,
                    desc -> new MsdfGlyph(desc, atlasWidth, atlasHeight)
                ));
            
            System.out.println("[MSDFFontLoader] Successfully loaded " + glyphMap.size() + " glyphs for font: " + name);

            Map<Integer, Map<Integer, Float>> kerningMap = new HashMap<>();
            if (definition.getKernings() != null) {
                definition.getKernings().forEach(pair -> {
                    Map<Integer, Float> charKerning = kerningMap.computeIfAbsent(
                        pair.getLeftChar(), k -> new HashMap<>()
                    );
                    charKerning.put(pair.getRightChar(), pair.getAdvance());
                });
            }

            return new MsdfFont(name, texture, definition.getAtlas(), definition.getMetrics(), glyphMap, kerningMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load MSDF font: " + name, e);
        }
    }
}
