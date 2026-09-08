package wanted.modules.visual;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;

/**
 * Шляпа, которая рисуется над головами игроков.
 * Геометрия строится в {@link wanted.render.KasaFeatureRenderer}.
 */
public class KasaHatModule extends Module {
    public final ModeSetting style = register(new ModeSetting("Стиль", "Как рисовать шляпу",
            "Каркас", "Каркас", "Каркас+сетка", "Заливка"));
    public final ModeSetting shape = register(new ModeSetting("Форма", "Силуэт шляпы",
            "Коническая", "Коническая", "Плоская", "Купол", "Кольцо"));
    public final ModeSetting targets = register(new ModeSetting("Кому", "На ком рисовать шляпу",
            "Все", "Все", "Только я", "Кроме меня"));
    public final NumberSetting size = register(new NumberSetting("Размер", "Радиус полей шляпы",
            1.0, 0.4, 2.0, 0.05));
    public final NumberSetting height = register(new NumberSetting("Высота", "Высота конуса",
            0.35, 0.05, 1.0, 0.05));
    public final NumberSetting offset = register(new NumberSetting("Отступ", "Подъём над головой",
            0.05, -0.2, 0.6, 0.01));
    public final ColorSetting color = register(new ColorSetting("Цвет", "Цвет шляпы", 0xCC3BFF9E));
    public final ColorSetting gridColor = register(new ColorSetting("Цвет сетки", "Цвет решётки под полями",
            0xB33BFFD6).visibleWhen(() -> style.is("Каркас+сетка")));
    public final NumberSetting segments = register(new NumberSetting("Сегменты", "Плотность каркаса",
            16, 6, 32, 1));
    public final ColorSetting trimColor = register(new ColorSetting("Кант", "Цвет канта по краю", 0xFFFFFFFF));
    public final BooleanSetting trim = register(new BooleanSetting("Кант", "Окантовка по краю полей", false));
    public final BooleanSetting spin = register(new BooleanSetting("Вращение", "Медленно вращать шляпу", false));
    public final NumberSetting spinSpeed = register(new NumberSetting("Скорость", "Скорость вращения",
            1.0, 0.1, 5.0, 0.1).visibleWhen(spin::get));
    public final BooleanSetting bob = register(new BooleanSetting("Парение", "Шляпа плавно покачивается", true));
    public final BooleanSetting glow = register(new BooleanSetting("Свечение", "Игнорировать освещение", true));

    public KasaHatModule() {
        super("Kasa", "Шляпы над игроками", Category.VISUAL);
    }

    @Override
    public String getInfo() {
        return style.get();
    }
}
