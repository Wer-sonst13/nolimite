package gg.nolimite;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Zeichnet die NoLimite-HUD-Elemente (FPS, Koordinaten). Das Scoreboard wird per Mixin verschoben. */
public class HudRenderer {
    public static String sample(String id, MinecraftClient mc) {
        return switch (id) {
            case "fps" -> "FPS: " + mc.getCurrentFps();
            case "coords" -> mc.player == null ? "XYZ: 0 64 0"
                    : String.format("XYZ: %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            default -> "";
        };
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.currentScreen instanceof NoLimiteMenuScreen) return;
        for (String id : new String[]{"fps", "coords"}) draw(ctx, mc, id);
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, String id) {
        HudConfig.Entry e = HudConfig.get(id);
        if (e == null || !e.enabled) return;
        String text = sample(id, mc);
        int w = mc.textRenderer.getWidth(text);
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(e.x, e.y, 0);
        m.scale(e.scale, e.scale, 1f);
        ctx.fill(0, 0, w + 4, 12, 0x90000000);
        ctx.drawText(mc.textRenderer, text, 2, 2, 0xFFFFFFFF, true);
        m.pop();
    }

    /** Rechteck eines Elements im Editor: {x, y, breite, höhe} */
    public static int[] bounds(String id, MinecraftClient mc) {
        HudConfig.Entry e = HudConfig.get(id);
        if (id.equals("scoreboard")) {
            int w = (int) (110 * e.scale), h = (int) (70 * e.scale);
            int right = (int) (mc.getWindow().getScaledWidth() + e.x);
            int cy = (int) (mc.getWindow().getScaledHeight() / 2.0 + e.y);
            return new int[]{right - w, cy - h / 2, w, h};
        }
        int w = (int) ((mc.textRenderer.getWidth(sample(id, mc)) + 4) * e.scale);
        return new int[]{(int) e.x, (int) e.y, w, (int) (12 * e.scale)};
    }
}
