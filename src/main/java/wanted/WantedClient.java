package wanted;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wanted.config.ConfigManager;
import wanted.module.Module;
import wanted.module.ModuleManager;
import wanted.render.KasaFeatureRenderer;
import wanted.ui.ClickGui;

import java.util.HashSet;
import java.util.Set;

public class WantedClient implements ClientModInitializer {
    public static final String MOD_ID = "wanted";
    public static final Logger LOGGER = LoggerFactory.getLogger("Wanted Visuals");

    /** Показывать кастомное главное меню вместо ванильного. */
    public static boolean customMainMenu = true;

    private static KeyBinding clickGuiKey;
    private final Set<Integer> heldKeys = new HashSet<>();

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ConfigManager.load();

        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wanted.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.wanted"));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register((context, tickCounter) -> onHudRender(context));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ModuleManager.esp().render(context);
            ModuleManager.tracers().render(context);
            ModuleManager.trail().render(context);
            ModuleManager.nameTags().render(context);
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, helper, context) -> {
                    if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                        helper.register(new KasaFeatureRenderer(playerRenderer));
                    }
                });

        // Эффект крита по любой сущности: событие приходит при атаке ЛКМ.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && player == MinecraftClient.getInstance().player) {
                ModuleManager.critEffect().onAttack(entity);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());

        LOGGER.info("Wanted Visuals загружен: {} модулей", ModuleManager.getModules().size());
    }

    private void onTick(MinecraftClient client) {
        ModuleManager.onTick();

        while (clickGuiKey.wasPressed()) {
            client.setScreen(new ClickGui());
        }

        if (client.currentScreen != null) return;

        long window = client.getWindow().getHandle();
        for (Module module : ModuleManager.getModules()) {
            int key = module.getKeyCode();
            if (key == GLFW.GLFW_KEY_UNKNOWN) continue;

            boolean down = InputUtil.isKeyPressed(window, key);
            if (down && heldKeys.add(key)) {
                module.toggle();
            } else if (!down) {
                heldKeys.remove(key);
            }
        }
    }

    private void onHudRender(net.minecraft.client.gui.DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;

        ModuleManager.watermark().render(context);
        ModuleManager.arrayList().render(context);

        Module coords = ModuleManager.getByName("Coords");
        if (coords instanceof wanted.modules.hud.CoordinatesModule coordinates) {
            coordinates.render(context);
        }
    }
}
