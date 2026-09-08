package wanted.modules.hud;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.ModeSetting;
import wanted.ui.Theme;

/** Переключение палитры клиента. Работает и когда модуль выключен — важен сам выбор. */
public class ThemeModule extends Module {
    public final ModeSetting palette = register(new ModeSetting("Тема", "Цветовая схема клиента",
            Theme.Palette.WANTED.getDisplayName(),
            Theme.Palette.WANTED.getDisplayName(),
            Theme.Palette.FROST.getDisplayName(),
            Theme.Palette.MONO.getDisplayName()));

    private String applied;

    public ThemeModule() {
        super("Theme", "Цветовая схема интерфейса", Category.HUD);
        setEnabled(true);
    }

    @Override
    public String getInfo() {
        return palette.get();
    }

    /** Вызывается каждый тик из менеджера, чтобы подхватить смену прямо в GUI. */
    public void sync() {
        if (!palette.get().equals(applied)) {
            applied = palette.get();
            Theme.apply(Theme.Palette.byName(applied));
        }
    }

    @Override
    public void onTick() {
        sync();
    }
}
