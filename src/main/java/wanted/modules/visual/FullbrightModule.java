package wanted.modules.visual;

import wanted.mixin.SimpleOptionAccessor;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.NumberSetting;

/** Полная яркость через прямую запись гаммы (в обход лимита 0..1). */
public class FullbrightModule extends Module {
    public final NumberSetting gamma = register(new NumberSetting("Гамма", "Уровень яркости",
            10.0, 1.0, 20.0, 0.5));

    private double previousGamma = 0.5;

    public FullbrightModule() {
        super("Fullbright", "Полная яркость мира", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        previousGamma = mc.options.getGamma().getValue();
    }

    @Override
    public void onTick() {
        setGamma(gamma.get());
    }

    @Override
    public void onDisable() {
        setGamma(previousGamma);
    }

    @SuppressWarnings("unchecked")
    private void setGamma(double value) {
        ((SimpleOptionAccessor<Double>) (Object) mc.options.getGamma()).wanted$setValue(value);
    }
}
