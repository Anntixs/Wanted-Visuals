package wanted.modules.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.NumberSetting;
import wanted.util.Render3D;

/** Кастомные ник-теги над игроками: масштаб, здоровье, дистанция. */
public class NameTagsModule extends Module {
    public final NumberSetting scale = register(new NumberSetting("Масштаб", "Размер текста",
            1.0, 0.4, 3.0, 0.1));
    public final BooleanSetting health = register(new BooleanSetting("Здоровье", "Показывать HP", true));
    public final BooleanSetting distance = register(new BooleanSetting("Дистанция", "Показывать расстояние", true));
    public final BooleanSetting background = register(new BooleanSetting("Подложка", "Тёмная подложка под текстом", true));
    public final NumberSetting range = register(new NumberSetting("Дальность", "Радиус отрисовки",
            96.0, 8.0, 256.0, 8.0));
    public final ColorSetting color = register(new ColorSetting("Цвет", "Цвет текста", 0xFFFFFFFF));

    public NameTagsModule() {
        super("NameTags", "Ник-теги над игроками", Category.VISUAL);
    }

    public void render(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        float tickDelta = context.tickCounter().getTickDelta(true);
        Vec3d cam = context.camera().getPos();
        TextRenderer textRenderer = mc.textRenderer;
        double rangeSq = range.get() * range.get();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (mc.player.squaredDistanceTo(player) > rangeSq) continue;

            Vec3d pos = Render3D.lerpPos(player, tickDelta).add(0, player.getHeight() + 0.45, 0);
            String text = buildText(player);

            matrices.push();
            matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
            matrices.multiply(context.camera().getRotation());
            float size = 0.025f * scale.getFloat();
            matrices.scale(-size, -size, size);

            float width = -textRenderer.getWidth(text) / 2f;
            int backgroundColor = background.get()
                    ? (int) (mc.options.getTextBackgroundOpacity(0.3f) * 255f) << 24
                    : 0;

            textRenderer.draw(text, width, 0f, color.getArgb(), false,
                    matrices.peek().getPositionMatrix(), consumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, 0xF000F0);

            matrices.pop();
        }
    }

    private String buildText(PlayerEntity player) {
        StringBuilder builder = new StringBuilder(player.getGameProfile().getName());
        if (health.get()) {
            builder.append(" §c").append((int) (player.getHealth() + player.getAbsorptionAmount())).append("§r");
        }
        if (distance.get() && mc.player != null) {
            builder.append(" §7").append((int) mc.player.distanceTo(player)).append("м§r");
        }
        return builder.toString();
    }
}
