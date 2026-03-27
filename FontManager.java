package x7k2m9.rendersystem.font;

import x7k2m9.rendersystem.font.embedded.HudIconsFont;
import x7k2m9.rendersystem.font.msdf.MSDFFonts;
import x7k2m9.rendersystem.font.msdf.MsdfFont;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FontManager {

    private static final Map<String, FontRenderer> fontCache = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> embeddedFonts = new ConcurrentHashMap<>();

    public static final int SIZE_SMALL  = 17;
    public static final int SIZE_NORMAL = 21;
    public static final int SIZE_LARGE  = 27;
    public static final int SIZE_TITLE  = 35;
    public static final int SIZE_ICONS  = 21;

    public static FontRenderer bold;
    public static FontRenderer regular;
    public static FontRenderer small;
    public static FontRenderer large;
    public static FontRenderer hudIcons;
    
    public static MsdfFont msdfBold;

    private static volatile boolean initialized = false;
    private static volatile boolean fontsLoaded = false;
    private static final AtomicInteger initAttempts = new AtomicInteger(0);
    private static final int MAX_INIT_ATTEMPTS = 10; 
    private static final int RETRY_DELAY_MS = 50; 
    private static volatile boolean forceRetry = true; 

    static {
        try {
            loadEmbeddedFonts();
        } catch (Throwable e) {
            System.err.println("[FontManager] Static init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static synchronized void loadEmbeddedFonts() {
        if (fontsLoaded && !forceRetry) return;

        System.out.println("[FontManager] Loading embedded fonts...");
        
        try {
            byte[] iconsBytes = HudIconsFont.getBytes();
            if (iconsBytes != null && iconsBytes.length > 0) {
                embeddedFonts.put("HudIcons", iconsBytes);
                System.out.println("[FontManager] ✓ Loaded HudIcons (" + iconsBytes.length + " bytes)");
            }
            fontsLoaded = true;
            System.out.println("[FontManager] Font loading completed successfully!");
        } catch (Throwable iconsError) {
            System.err.println("[FontManager] Failed to load HudIcons: " + iconsError.getMessage());
            iconsError.printStackTrace();
        }
    }

    public static synchronized void init() {
        if (initialized) {
            System.out.println("[FontManager] Already initialized, skipping");
            return;
        }

        String threadName = Thread.currentThread().getName();
        if (!threadName.contains("Render thread") && !threadName.contains("Client thread")) {
            System.err.println("[FontManager] WARNING: init() called from wrong thread: " + threadName);
            System.err.println("[FontManager] Deferring initialization to render thread");
            return;
        }

        int attempt = initAttempts.incrementAndGet();
        System.out.println("[FontManager] Initializing from thread: " + threadName + " (attempt " + attempt + ")");

        boolean success = false;
        int retryCount = 0;

        while (!success && retryCount < MAX_INIT_ATTEMPTS) {
            retryCount++;

            try {
                
                if (!fontsLoaded) {
                    System.out.println("[FontManager] Fonts not loaded, forcing load (retry " + retryCount + ")...");
                    loadEmbeddedFonts();
                }

                if (!fontsLoaded) {
                    System.err.println("[FontManager] Fonts still not loaded after loadEmbeddedFonts(), retrying...");
                    Thread.sleep(RETRY_DELAY_MS);
                    continue; 
                }

                System.out.println("[FontManager] Creating MSDF font instances...");
                try {
                    MSDFFonts.init();
                    msdfBold = MSDFFonts.BOLD;
                    
                    if (msdfBold == null) {
                        throw new RuntimeException("MSDF Bold font is null after init");
                    }
                    
                    bold = new MSDFFontRendererImpl(msdfBold, SIZE_NORMAL);
                    regular = new MSDFFontRendererImpl(msdfBold, SIZE_NORMAL);
                    small = new MSDFFontRendererImpl(msdfBold, SIZE_SMALL);
                    large = new MSDFFontRendererImpl(msdfBold, SIZE_LARGE);
                    
                    byte[] hudIconsBytes = HudIconsFont.getBytes();
                    if (hudIconsBytes != null && hudIconsBytes.length > 0) {
                        System.out.println("[FontManager] Using embedded HudIcons as fallback");
                    }
                    hudIcons = bold;
                    
                    System.out.println("[FontManager] MSDF fonts initialized successfully");
                } catch (Throwable msdfError) {
                    System.err.println("[FontManager] Failed to initialize MSDF fonts: " + msdfError.getMessage());
                    msdfError.printStackTrace();
                    throw msdfError;
                }

                if (bold == null && regular == null && small == null && large == null) {
                    System.err.println("[FontManager] Failed to create any fonts, retrying...");
                    Thread.sleep(RETRY_DELAY_MS);
                    continue; 
                }

                System.out.println("[FontManager] Fonts created successfully");

                initialized = true;
                success = true;
                System.out.println("[FontManager] ✓ Successfully initialized on attempt " + retryCount + "!");

            } catch (Throwable e) {
                System.err.println("[FontManager] Init attempt " + retryCount + " failed: " + e.getMessage());
                e.printStackTrace();

                try {
                    fontCache.clear();
                    bold = regular = small = large = null;
                    msdfBold = null;
                } catch (Throwable cleanupError) {
                    System.err.println("[FontManager] Cleanup error: " + cleanupError.getMessage());
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!success) {
            System.err.println("[FontManager] ========================================");
            System.err.println("[FontManager] CRITICAL: Failed to initialize after " + retryCount + " attempts!");
            System.err.println("[FontManager] Font system may not work properly!");
            System.err.println("[FontManager] ========================================");
        }
    }

    private static FontRenderer createFontSafe(String fontName, int size, String label) {
        return null;
    }

    public static FontRenderer getOrCreate(String fontName, int size) {
        return null;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isReady() {
        return initialized && (bold != null || regular != null);
    }

    public static void processPendingGlyphs() {
        try {
            fontCache.values().forEach(f -> {
                if (f != null) {
                    try {
                        f.processPendingGlyphs();
                    } catch (Throwable e) {
                        System.err.println("[FontManager] Error processing glyphs: " + e.getMessage());
                    }
                }
            });
        } catch (Throwable e) {
            System.err.println("[FontManager] Error in processPendingGlyphs: " + e.getMessage());
        }
    }

    public static void ensureAtlasesReady() {
        try {
            fontCache.values().forEach(f -> {
                if (f != null) {
                    try {
                        f.ensureAtlasReady();
                    } catch (Throwable e) {
                        System.err.println("[FontManager] Error ensuring atlas ready: " + e.getMessage());
                    }
                }
            });
        } catch (Throwable e) {
            System.err.println("[FontManager] Error in ensureAtlasesReady: " + e.getMessage());
        }
    }

    public static void cleanup() {
        try {
            System.out.println("[FontManager] Cleaning up...");
            fontCache.values().forEach(f -> {
                if (f != null) {
                    try {
                        f.cleanup();
                    } catch (Throwable e) {
                        System.err.println("[FontManager] Error cleaning up font: " + e.getMessage());
                    }
                }
            });
            fontCache.clear();
            bold = regular = small = large = hudIcons = null;
            msdfBold = null;
            MSDFFonts.cleanup();
            initialized = false;
            initAttempts.set(0);
            
            System.out.println("[FontManager] Cleanup complete");
        } catch (Throwable e) {
            System.err.println("[FontManager] Error in cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void clearCache() {
        cleanup();
    }

    public static void forceReload() {
        System.out.println("[FontManager] ========================================");
        System.out.println("[FontManager] FORCE RELOAD - clearing everything!");
        System.out.println("[FontManager] ========================================");

        try {
            cleanup();
            embeddedFonts.clear();
            fontsLoaded = false;
            forceRetry = true;

            loadEmbeddedFonts();

            if (fontsLoaded) {
                init();
            }
        } catch (Throwable e) {
            System.err.println("[FontManager] Force reload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void regenerateAll() {
        System.out.println("[FontManager] Regenerating all fonts...");
        cleanup();
        init();
    }

    public static boolean healthCheck() {
        boolean healthy = fontsLoaded && initialized && (bold != null || regular != null || msdfBold != null);

        if (!healthy) {
            System.err.println("[FontManager] Health check FAILED:");
            System.err.println("  - fontsLoaded: " + fontsLoaded);
            System.err.println("  - initialized: " + initialized);
            System.err.println("  - bold: " + (bold != null));
            System.err.println("  - regular: " + (regular != null));
            System.err.println("  - msdfBold: " + (msdfBold != null));
        }

        return healthy;
    }

    public static void autoRecover() {
        if (!healthCheck()) {
            System.out.println("[FontManager] Auto-recovery triggered!");
            forceReload();
        }
    }

    public static FontRenderer getFont(int size) {
        FontRenderer font = getOrCreate("Bold", size);
        return font != null ? font : bold;
    }

    public static FontRenderer getBold(int size) {
        FontRenderer font = getOrCreate("Bold", size);
        return font != null ? font : bold;
    }

    public static FontRenderer getRegular(int size) {
        FontRenderer font = getOrCreate("Bold", size);
        return font != null ? font : regular;
    }
}