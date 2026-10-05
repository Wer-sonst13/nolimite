package gg.nolimite;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NoLimiteClient implements ClientModInitializer {
    public static KeyBinding MENU_KEY;

    @Override
    public void onInitializeClient() {
        HudConfig.load();
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nolimite.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.nolimite"));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (MENU_KEY.wasPressed()) mc.setScreen(new NoLimiteMenuScreen(mc.currentScreen));
        });
        HudRenderCallback.EVENT.register((ctx, tick) -> HudRenderer.render(ctx));
    }
}
