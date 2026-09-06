package wanted.modules.hud;

import net.minecraft.client.gui.DrawContext;
import wanted.module.Category;
import wanted.module.Module;
import wanted.ui.Render2D;
import wanted.ui.Theme;

/** Координаты и направление в левом нижнем углу. */
public class CoordinatesModule extends Module {
    public CoordinatesModule() {
        super("Coords", "Координаты игрока", Category.HUD);
    }

    public void render(DrawContext context) {
        if (!isEnabled() || mc.player == null) return;

        String text = String.format("XYZ %.1f  %.1f  %.1f  §7%s",
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                mc.player.getHorizontalFacing().getName().toUpperCase());

        int width = mc.textRenderer.getWidth(text);
        int y = context.getScaledWindowHeight() - 20;

        Render2D.roundedRect(context, 6, y, width + 12, 14, 4, Theme.withAlpha(Theme.PANEL, 0.8f));
        context.drawText(mc.textRenderer, text, 12, y + 3, Theme.TEXT, false);
    }
}
