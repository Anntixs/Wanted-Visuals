package wanted.module;

import wanted.modules.hud.ArrayListModule;
import wanted.modules.hud.ThemeModule;
import wanted.modules.hud.CoordinatesModule;
import wanted.modules.hud.WatermarkModule;
import wanted.modules.visual.GlowModule;
import wanted.modules.visual.CritEffectModule;
import wanted.modules.visual.EspModule;
import wanted.modules.visual.FullbrightModule;
import wanted.modules.visual.KasaHatModule;
import wanted.modules.visual.NameTagsModule;
import wanted.modules.visual.TracersModule;
import wanted.modules.visual.TrailModule;
import wanted.modules.visual.ZoomModule;
import wanted.modules.world.SakuraModule;
import wanted.modules.world.SkyModule;
import wanted.modules.world.TimeChangerModule;
import wanted.modules.world.WeatherModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private static KasaHatModule kasaHat;
    private static SkyModule sky;
    private static EspModule esp;
    private static TracersModule tracers;
    private static GlowModule glow;
    private static NameTagsModule nameTags;
    private static TrailModule trail;
    private static ZoomModule zoom;
    private static SakuraModule sakura;
    private static TimeChangerModule timeChanger;
    private static WeatherModule weather;
    private static ArrayListModule arrayList;
    private static ThemeModule theme;
    private static CritEffectModule critEffect;
    private static WatermarkModule watermark;

    private ModuleManager() {
    }

    public static void init() {
        if (!MODULES.isEmpty()) return;

        add(kasaHat = new KasaHatModule());
        add(glow = new GlowModule());
        add(esp = new EspModule());
        add(tracers = new TracersModule());
        add(nameTags = new NameTagsModule());
        add(trail = new TrailModule());
        add(critEffect = new CritEffectModule());
        add(new FullbrightModule());
        add(zoom = new ZoomModule());

        add(sky = new SkyModule());
        add(timeChanger = new TimeChangerModule());
        add(weather = new WeatherModule());
        add(sakura = new SakuraModule());

        add(watermark = new WatermarkModule());
        add(arrayList = new ArrayListModule());
        add(new CoordinatesModule());
        add(theme = new ThemeModule());
    }

    private static void add(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static List<Module> getByCategory(Category category) {
        return MODULES.stream()
                .filter(module -> module.getCategory() == category)
                .sorted(Comparator.comparing(Module::getName))
                .toList();
    }

    public static Module getByName(String name) {
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    /** Модули по категориям в порядке объявления enum. */
    public static Map<Category, List<Module>> grouped() {
        Map<Category, List<Module>> map = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            map.put(category, getByCategory(category));
        }
        return map;
    }

    public static void onTick() {
        // Тема должна применяться даже когда модуль выключен — это просто выбор палитры.
        if (theme != null) theme.sync();

        for (Module module : MODULES) {
            float target = module.isEnabled() ? 1f : 0f;
            module.setAnimation(module.getAnimation() + (target - module.getAnimation()) * 0.25f);
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public static void onKeyPressed(int keyCode) {
        for (Module module : MODULES) {
            if (module.getKeyCode() == keyCode) {
                module.toggle();
            }
        }
    }

    public static KasaHatModule kasaHat() {
        return kasaHat;
    }

    public static SkyModule sky() {
        return sky;
    }

    public static EspModule esp() {
        return esp;
    }

    public static TracersModule tracers() {
        return tracers;
    }

    public static GlowModule glow() {
        return glow;
    }

    public static NameTagsModule nameTags() {
        return nameTags;
    }

    public static TrailModule trail() {
        return trail;
    }

    public static ZoomModule zoom() {
        return zoom;
    }

    public static SakuraModule sakura() {
        return sakura;
    }

    public static TimeChangerModule timeChanger() {
        return timeChanger;
    }

    public static WeatherModule weather() {
        return weather;
    }

    public static ThemeModule theme() {
        return theme;
    }

    public static CritEffectModule critEffect() {
        return critEffect;
    }

    public static ArrayListModule arrayList() {
        return arrayList;
    }

    public static WatermarkModule watermark() {
        return watermark;
    }
}
