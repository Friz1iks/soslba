package x7k2m9.rendersystem.render2d;

import x7k2m9.rendersystem.render2d.rectangle.RectangleGL;
import x7k2m9.rendersystem.render2d.outline.OutlineGL;
import x7k2m9.rendersystem.render2d.blur.KawaseGL;
import x7k2m9.rendersystem.render2d.glass.LiquidGlassGL;
import x7k2m9.rendersystem.render2d.arc.ArcGL;
import x7k2m9.rendersystem.render2d.glow.GlowGL;
import x7k2m9.rendersystem.render2d.glow.GlowOutlineGL;
import x7k2m9.rendersystem.render2d.text.TextRenderer;

public class Render2DManager {

    public static void cleanup() {
        Render2DGL.cleanup();
        TextRenderer.cleanup();
    }
}
