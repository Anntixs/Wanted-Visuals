package wanted.modules.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.NumberSetting;
import wanted.util.Render3D;

/** Линии от камеры к сущностям. */
public class TracersModule extends Module {
    public final BooleanSetting players = register(new BooleanSetting("Игроки", "Линии к игрокам", true));
    public final BooleanSetting hostile = register(new BooleanSetting("Мобы", "Линии к враждебным мобам", false));
    public final NumberSetting range = register(new NumberSetting("Дальность", "Радиус отрисовки",
            96.0, 8.0, 256.0, 8.0));
    public final ColorSetting color = register(new ColorSetting("Цвет", "Цвет линий", 0xB3FF3B5C));

    public TracersModule() {
        super("Tracers", "Линии к сущностям", Category.VISUAL);
    }

    public void render(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        float tickDelta = context.tickCounter().getTickDelta(true);
        Vec3d start = context.camera().getPos()
                .add(new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(context.camera().getPitch()))
                        .rotateY(-(float) Math.toRadians(context.camera().getYaw())));
        double rangeSq = range.get() * range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (mc.player.squaredDistanceTo(entity) > rangeSq) continue;
            if (!isTarget(entity)) continue;

            Vec3d end = Render3D.lerpPos(entity, tickDelta).add(0, entity.getHeight() / 2.0, 0);
            Render3D.drawLine(context, start, end, color.getArgb());
        }
    }

    private boolean isTarget(Entity entity) {
        if (entity instanceof PlayerEntity) return players.get();
        if (entity instanceof HostileEntity) return hostile.get();
        return false;
    }
}
