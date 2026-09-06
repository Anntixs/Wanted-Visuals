package wanted.modules.visual;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;

/**
 * Японская соломенная шляпа (каса), которая рисуется на головах игроков.
 * Сама геометрия строится в {@link wanted.render.KasaFeatureRenderer}.
 */
public class KasaHatModule extends Module {
    public final ModeSetting shape = register(new ModeSetting("Форма", "Силуэт шляпы",
            "Sugegasa", "Sugegasa", "Jingasa", "Sandogasa", "Halo"));
    public final ModeSetting targets = register(new ModeSetting("Кому", "На ком рисовать шляпу",
            "Все", "Все", "Только я", "Кроме меня"));
    public final NumberSetting size = register(new NumberSetting("Размер", "Радиус полей шляпы",
            1.0, 0.4, 2.0, 0.05));
    public final NumberSetting height = register(new NumberSetting("Высота", "Высота конуса",
            0.35, 0.05, 1.0, 0.05));
    public final NumberSetting offset = register(new NumberSetting("Отступ", "Подъём над головой",
            0.05, -0.2, 0.6, 0.01));
    public final ColorSetting color = register(new ColorSetting("Цвет", "Цвет шляпы", 0xFFE0C078));
    public final ColorSetting trimColor = register(new ColorSetting("Кант", "Цвет канта по краю", 0xFFFF3B5C));
    public final BooleanSetting trim = register(new BooleanSetting("Кант", "Красная окантовка по краю полей", true));
    public final BooleanSetting spin = register(new BooleanSetting("Вращение", "Медленно вращать шляпу", false));
    public final NumberSetting spinSpeed = register(new NumberSetting("Скорость", "Скорость вращения",
            1.0, 0.1, 5.0, 0.1).visibleWhen(spin::get));
    public final BooleanSetting bob = register(new BooleanSetting("Парение", "Шляпа плавно покачивается", true));
    public final BooleanSetting glow = register(new BooleanSetting("Свечение", "Игнорировать освещение", false));

    public KasaHatModule() {
        super("Kasa", "Японские шляпы на игроках", Category.VISUAL);
    }

    @Override
    public String getInfo() {
        return shape.get();
    }
}
