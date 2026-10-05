package gg.nolimite;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;

/** NoLimite-Menü: Elemente mit der Maus verschieben, Mausrad = Größe, Buttons = an/aus. */
public class NoLimiteMenuScreen extends Screen {
    private static final Map<String, String> NAMES = Map.of("fps", "FPS", "coords", "Koordinaten", "scoreboard", "Scoreboard");
    private final Screen parent;
    private String dragging;

    public NoLimiteMenuScreen(Screen parent) {
        super(Text.literal("NoLimite Menü"));
        this.parent = parent;
    }

    private Text label(String id) {
        return Text.literal(NAMES.get(id) + ": " + (HudConfig.get(id).enabled ? "AN" : "AUS"));
    }

    @Override
    protected void init() {
        int y = 40;
        for (String id : new String[]{"fps", "coords", "scoreboard"}) {
            addDrawableChild(ButtonWidget.builder(label(id), b -> {
                HudConfig.Entry e = HudConfig.get(id);
                e.enabled = !e.enabled;
                b.setMessage(label(id));
            }).dimensions(10, y, 130, 20).build());
            y += 24;
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Zurücksetzen"), b -> HudConfig.reset())
                .dimensions(10, y + 6, 130, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> close())
                .dimensions(10, y + 32, 130, 20).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x88000000);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawText(textRenderer, "NoLimite Menü", 10, 10, 0xFFB79CFF, true);
        ctx.drawText(textRenderer, "Ziehen = verschieben  |  Mausrad = Größe", 10, 24, 0xFFAAAAAA, true);
        for (String id : HudConfig.ENTRIES.keySet()) {
            HudConfig.Entry e = HudConfig.get(id);
            int[] b = HudRenderer.bounds(id, client);
            boolean hover = mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3];
            int col = !e.enabled ? 0xFF555555 : (hover || id.equals(dragging)) ? 0xFFB79CFF : 0xFFFFFFFF;
            ctx.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], 0x66000000);
            ctx.drawBorder(b[0], b[1], b[2], b[3], col);
            String text = id.equals("scoreboard") ? "Scoreboard" : HudRenderer.sample(id, client);
            ctx.drawText(textRenderer, text, b[0] + 3, b[1] + 3, col, true);
        }
    }

    private String hit(double mx, double my) {
        for (String id : HudConfig.ENTRIES.keySet()) {
            int[] b = HudRenderer.bounds(id, client);
            if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3]) return id;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        dragging = hit(mx, my);
        return dragging != null;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            HudConfig.Entry e = HudConfig.get(dragging);
            e.x += dx;
            e.y += dy;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        String id = hit(mx, my);
        if (id != null) {
            HudConfig.Entry e = HudConfig.get(id);
            e.scale = Math.max(0.5f, Math.min(3f, e.scale + (float) vAmount * 0.1f));
            return true;
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    @Override
    public void close() {
        HudConfig.save();
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
