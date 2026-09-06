package wanted.modules.hud;

import net.minecraft.client.gui.DrawContext;
import wanted.module.Category;
import wanted.module.Module;
import wanted.module.ModuleManager;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.ui.Render2D;
import wanted.ui.Theme;

import java.util.Comparator;
import java.util.List;

/** Список активных модулей справа сверху с анимацией выезда. */
public class ArrayListModule extends Module {
    public final BooleanSetting showInfo = register(new BooleanSetting("Суффиксы", "Показывать режим модуля", true));
    public final BooleanSetting gradient = register(new BooleanSetting("Градиент", "Красить строки градиентом", true));
    public final ColorSetting from = register(new ColorSetting("Цвет A", "Начало градиента", 0xFFFF3B5C));
    public final ColorSetting to = register(new ColorSetting("Цвет B", "Конец градиента", 0xFFFF8A3B));

    public ArrayListModule() {
        super("ArrayList", "Список активных модулей", Category.HUD);
    }

    public void render(DrawContext context) {
        if (!isEnabled()) return;

        List<Module> visible = ModuleManager.getModules().stream()
                .filter(module -> module.getAnimation() > 0.01f)
                .filter(module -> module.getCategory() != Category.HUD || module.isEnabled())
                .sorted(Comparator.comparingInt((Module module) -> mc.textRenderer.getWidth(label(module))).reversed())
                .toList();

        int screenWidth = context.getScaledWindowWidth();
        float y = 6;
        int index = 0;

        for (Module module : visible) {
            String text = label(module);
            int width = mc.textRenderer.getWidth(text);
            float animation = module.getAnimation();
            float x = screenWidth - 6 - (width + 8) * animation;

            int color = gradient.get()
                    ? Theme.lerpColor(from.getArgb(), to.getArgb(),
                        Math.min(1f, index / (float) Math.max(1, visible.size() - 1)))
                    : from.getArgb();

            Render2D.roundedRect(context, x - 4, y, width + 10, 12, 3,
                    Theme.withAlpha(Theme.PANEL, 0.75f * animation));
            Render2D.roundedRect(context, screenWidth - 6, y, 2, 12, 1,
                    Theme.withAlpha(color, animation));
            context.drawText(mc.textRenderer, text, (int) x, (int) y + 2,
                    Theme.withAlpha(color, animation), false);

            y += 13 * animation;
            index++;
        }
    }

    private String label(Module module) {
        String info = showInfo.get() ? module.getInfo() : null;
        return info == null ? module.getName() : module.getName() + " §7" + info;
    }
}
