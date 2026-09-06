package wanted.modules.world;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;

/** Клиентская фиксация времени суток (влияет только на отрисовку). */
public class TimeChangerModule extends Module {
    public final ModeSetting preset = register(new ModeSetting("Время", "Фиксированное время суток",
            "Закат", "Рассвет", "День", "Закат", "Ночь", "Своё"));
    public final NumberSetting custom = register(new NumberSetting("Тики", "Время в игровых тиках",
            18000, 0, 23999, 100).visibleWhen(() -> preset.is("Своё")));

    public TimeChangerModule() {
        super("TimeChanger", "Фиксация времени суток", Category.WORLD);
    }

    @Override
    public String getInfo() {
        return preset.get();
    }

    public long getTime() {
        return switch (preset.get()) {
            case "Рассвет" -> 23000L;
            case "День" -> 6000L;
            case "Закат" -> 12800L;
            case "Ночь" -> 18000L;
            default -> custom.getInt();
        };
    }
}
