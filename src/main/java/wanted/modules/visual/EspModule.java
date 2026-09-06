package wanted.modules.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.NumberSetting;
import wanted.util.Render3D;

/** Боксы вокруг сущностей. */
public class EspModule extends Module {
    public final BooleanSetting players = register(new BooleanSetting("Игроки", "Боксы на игроках", true));
    public final BooleanSetting hostile = register(new BooleanSetting("Мобы", "Боксы на враждебных мобах", false));
    public final BooleanSetting passive = register(new BooleanSetting("Животные", "Боксы на пассивных мобах", false));
    public final NumberSetting range = register(new NumberSetting("Дальность", "Радиус отрисовки",
            96.0, 8.0, 256.0, 8.0));
    public final ColorSetting playerColor = register(new ColorSetting("Цвет игроков", "Цвет боксов игроков", 0xFFFF3B5C));
    public final ColorSetting mobColor = register(new ColorSetting("Цвет мобов", "Цвет боксов мобов", 0xFF5C7CFF));

    public EspModule() {
        super("ESP", "Боксы вокруг сущностей", Category.VISUAL);
    }

    public void render(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        float tickDelta = context.tickCounter().getTickDelta(true);
        double rangeSq = range.get() * range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (mc.player.squaredDistanceTo(entity) > rangeSq) continue;

            Integer color = colorFor(entity);
            if (color == null) continue;

            Vec3d interpolated = Render3D.lerpPos(entity, tickDelta);
            Box box = entity.getBoundingBox().offset(interpolated.subtract(entity.getPos()));
            Render3D.drawBox(context, box, color);
        }
    }

    private Integer colorFor(Entity entity) {
        if (entity instanceof PlayerEntity) return players.get() ? playerColor.getArgb() : null;
        if (entity instanceof HostileEntity) return hostile.get() ? mobColor.getArgb() : null;
        if (entity instanceof PassiveEntity) return passive.get() ? mobColor.getArgb() : null;
        return null;
    }
}
