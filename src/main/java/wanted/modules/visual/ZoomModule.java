package wanted.modules.visual;

import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.NumberSetting;

/** Плавный зум камеры. */
public class ZoomModule extends Module {
    public final NumberSetting factor = register(new NumberSetting("Кратность", "Во сколько раз приближать",
            4.0, 1.5, 10.0, 0.5));
    public final BooleanSetting smoothCamera = register(new BooleanSetting("Плавная мышь",
            "Снижать чувствительность при зуме", true));

    private int previousFov = 70;
    private boolean previousSmooth;

    public ZoomModule() {
        super("Zoom", "Приближение камеры", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        previousFov = mc.options.getFov().getValue();
        previousSmooth = mc.options.smoothCameraEnabled;
        mc.options.getFov().setValue((int) Math.max(1, Math.round(previousFov / factor.get())));
        if (smoothCamera.get()) mc.options.smoothCameraEnabled = true;
    }

    @Override
    public void onDisable() {
        mc.options.getFov().setValue(previousFov);
        mc.options.smoothCameraEnabled = previousSmooth;
    }

    @Override
    public String getInfo() {
        return String.format("x%.1f", factor.get());
    }
}
