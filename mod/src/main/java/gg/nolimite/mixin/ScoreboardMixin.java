package gg.nolimite.mixin;

import gg.nolimite.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Verschiebt/skaliert das Scoreboard. Pivot = rechte Bildschirmkante, vertikale Mitte. */
@Mixin(InGameHud.class)
public class ScoreboardMixin {
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"), require = 0)
    private void nolimite$pre(DrawContext ctx, ScoreboardObjective obj, CallbackInfo ci) {
        HudConfig.Entry e = HudConfig.get("scoreboard");
        ctx.getMatrices().push();
        if (e == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        double px = mc.getWindow().getScaledWidth(), py = mc.getWindow().getScaledHeight() / 2.0;
        ctx.getMatrices().translate(px + e.x, py + e.y, 0);
        ctx.getMatrices().scale(e.scale, e.scale, 1f);
        ctx.getMatrices().translate(-px, -py, 0);
        if (!e.enabled) ctx.getMatrices().scale(0f, 0f, 0f);
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("RETURN"), require = 0)
    private void nolimite$post(DrawContext ctx, ScoreboardObjective obj, CallbackInfo ci) {
        ctx.getMatrices().pop();
    }
}
