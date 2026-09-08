package wanted.modules.hud;

import net.minecraft.client.gui.DrawContext;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.ui.Render2D;
import wanted.ui.Theme;

/** Ватермарка клиента в левом верхнем углу. */
public class WatermarkModule extends Module {
    public final BooleanSetting showFps = register(new BooleanSetting("FPS", "Показывать FPS", true));
    public final BooleanSetting showPing = register(new BooleanSetting("Пинг", "Показывать задержку", true));
    public final ColorSetting accent = register(new ColorSetting("Акцент", "Цвет подсветки", 0xFFFF3B5C));

    public WatermarkModule() {
        super("Watermark", "Ватермарка клиента", Category.HUD);
    }

    public void render(DrawContext context) {
        if (!isEnabled()) return;

        String title = "WANTED";
        String tag = "VISUALS";
        StringBuilder stats = new StringBuilder();
        if (showFps.get()) stats.append(mc.getCurrentFps()).append(" fps");
        if (showPing.get() && mc.getNetworkHandler() != null && mc.player != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                if (!stats.isEmpty()) stats.append("  ·  ");
                stats.append(entry.getLatency()).append(" ms");
            }
        }

        int titleWidth = mc.textRenderer.getWidth(title);
        int tagWidth = mc.textRenderer.getWidth(tag);
        int statsWidth = stats.isEmpty() ? 0 : mc.textRenderer.getWidth(stats.toString());
        float width = 14 + titleWidth + 6 + tagWidth + (statsWidth > 0 ? statsWidth + 12 : 0) + 10;

        Render2D.roundedRect(context, 6, 6, width, 20, 6, Theme.PANEL);
        Render2D.roundedRect(context, 6, 6, 3, 20, 1.5f, accent.getArgb());

        context.drawText(mc.textRenderer, title, 15, 12, Theme.TEXT, false);
        context.drawText(mc.textRenderer, tag, 15 + titleWidth + 6, 12, accent.getArgb(), false);
        if (statsWidth > 0) {
            context.drawText(mc.textRenderer, stats.toString(),
                    15 + titleWidth + 6 + tagWidth + 10, 12, Theme.TEXT_DIM, false);
        }
    }
}
