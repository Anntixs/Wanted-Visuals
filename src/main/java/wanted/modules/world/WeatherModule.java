package wanted.modules.world;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.ModeSetting;

/** Клиентская погода: ясно / дождь / гроза. */
public class WeatherModule extends Module {
    public final ModeSetting mode = register(new ModeSetting("Погода", "Что показывать",
            "Ясно", "Ясно", "Дождь", "Гроза"));

    public WeatherModule() {
        super("Weather", "Клиентская погода", Category.WORLD);
    }

    @Override
    public String getInfo() {
        return mode.get();
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        switch (mode.get()) {
            case "Дождь" -> {
                mc.world.setRainGradient(1.0f);
                mc.world.setThunderGradient(0.0f);
            }
            case "Гроза" -> {
                mc.world.setRainGradient(1.0f);
                mc.world.setThunderGradient(1.0f);
            }
            default -> {
                mc.world.setRainGradient(0.0f);
                mc.world.setThunderGradient(0.0f);
            }
        }
    }
}
