package wanted.modules.world;

import net.minecraft.util.math.Vec3d;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.NumberSetting;
import wanted.setting.ModeSetting;

/**
 * Смена цвета неба. Значения читает {@link wanted.mixin.ClientWorldMixin}.
 */
public class SkyModule extends Module {
    public final ModeSetting preset = register(new ModeSetting("Пресет", "Готовые палитры неба",
            "Sakura", "Sakura", "Sunset", "Void", "Amethyst", "Aurora", "Ink", "Custom"));
    public final ColorSetting custom = register(new ColorSetting("Свой цвет", "Цвет неба в режиме Custom",
            0xFFFF3B5C).visibleWhen(() -> preset.is("Custom")));
    public final NumberSetting blend = register(new NumberSetting("Насыщенность",
            "Насколько сильно перекрашивать небо", 1.0, 0.0, 1.0, 0.05));
    public final BooleanSetting pulse = register(new BooleanSetting("Пульсация",
            "Плавно менять яркость неба", false));

    public SkyModule() {
        super("Sky", "Смена цвета неба", Category.WORLD);
    }

    @Override
    public String getInfo() {
        return preset.get();
    }

    /** Итоговый цвет неба; null — не вмешиваться. */
    public Vec3d getSkyColor(Vec3d vanilla) {
        int rgb = switch (preset.get()) {
            case "Sakura" -> 0xFFB7C5;
            case "Sunset" -> 0xFF7A3C;
            case "Void" -> 0x0A0A12;
            case "Amethyst" -> 0x8A5CFF;
            case "Aurora" -> 0x3BFFC1;
            case "Ink" -> 0x1B2233;
            default -> custom.getArgb() & 0xFFFFFF;
        };

        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;

        if (pulse.get()) {
            double wave = 0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 900.0);
            r *= wave;
            g *= wave;
            b *= wave;
        }

        double t = blend.get();
        return new Vec3d(
                vanilla.x + (r - vanilla.x) * t,
                vanilla.y + (g - vanilla.y) * t,
                vanilla.z + (b - vanilla.z) * t);
    }
}
