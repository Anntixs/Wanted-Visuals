package wanted.modules.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.ColorSetting;
import wanted.setting.NumberSetting;
import wanted.ui.Theme;
import wanted.util.Render3D;

import java.util.ArrayDeque;
import java.util.Deque;

/** Затухающий след за игроком. */
public class TrailModule extends Module {
    public final NumberSetting length = register(new NumberSetting("Длина", "Сколько точек хранить",
            60.0, 10.0, 200.0, 5.0));
    public final ColorSetting color = register(new ColorSetting("Цвет", "Цвет следа", 0xFFFF8A3B));

    private final Deque<Vec3d> points = new ArrayDeque<>();

    public TrailModule() {
        super("Trail", "След за игроком", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        points.addLast(mc.player.getPos());
        while (points.size() > length.getInt()) {
            points.removeFirst();
        }
    }

    @Override
    public void onDisable() {
        points.clear();
    }

    public void render(WorldRenderContext context) {
        if (!isEnabled() || points.size() < 2) return;

        Vec3d previous = null;
        int index = 0;
        int total = points.size();
        for (Vec3d point : points) {
            if (previous != null) {
                float fade = (float) index / total;
                Render3D.drawLine(context, previous, point, Theme.withAlpha(color.getArgb(), fade));
            }
            previous = point;
            index++;
        }
    }
}
