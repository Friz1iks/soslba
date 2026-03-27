package x7k2m9.rendersystem.font.msdf;

import x7k2m9.rendersystem.font.icons.msdf.BoldMsdfAtlas;
import x7k2m9.rendersystem.font.icons.msdf.BoldMsdfJson;

public class MSDFFonts {
    public static MsdfFont BOLD;
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        try {
            byte[] atlasBytes = BoldMsdfAtlas.getBytes();
            String jsonData = BoldMsdfJson.getJson();
            
            if (atlasBytes == null || atlasBytes.length == 0) {
                throw new RuntimeException("Bold atlas bytes are null or empty");
            }
            
            if (jsonData == null || jsonData.isEmpty()) {
                throw new RuntimeException("Bold JSON data is null or empty");
            }
            
            BOLD = new MsdfFontLoader()
                .setName("Bold")
                .setAtlasBytes(atlasBytes)
                .setJsonData(jsonData)
                .load();
            
            initialized = true;
        } catch (Exception e) {
            System.err.println("[MSDFFonts] Failed to initialize MSDF fonts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize MSDF fonts", e);
        }
    }

    public static void cleanup() {
        BOLD = null;
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
