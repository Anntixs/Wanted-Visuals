package wanted.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import wanted.WantedClient;

import java.util.ArrayList;
import java.util.List;

/** Кастомное главное меню: тёмный градиент, красное солнце, лепестки и свои кнопки. */
public class WantedTitleScreen extends Screen {
    private static final int BG_TOP = 0xFF0B0C10;
    private static final int BG_BOTTOM = 0xFF1A0E14;

    private final Petals petals = new Petals(60);
    private final List<MenuButton> buttons = new ArrayList<>();

    public WantedTitleScreen() {
        super(Text.literal("Wanted Visuals"));
    }

    @Override
    protected void init() {
        buttons.clear();
        buttons.add(new MenuButton("Одиночная игра", "シングル",
                () -> client.setScreen(new SelectWorldScreen(this))));
        buttons.add(new MenuButton("Сетевая игра", "マルチ",
                () -> client.setScreen(new MultiplayerScreen(this))));
        buttons.add(new MenuButton("Настройки", "設定",
                () -> client.setScreen(new OptionsScreen(this, client.options))));
        buttons.add(new MenuButton("Ванильное меню", "既定",
                () -> {
                    WantedClient.customMainMenu = false;
                    client.setScreen(new TitleScreen());
                }));
        buttons.add(new MenuButton("Выход", "終了", () -> client.scheduleStop()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackdrop(context);

        float panelX = width * 0.08f;
        float titleY = height * 0.22f;

        // Заголовок
        context.getMatrices().push();
        context.getMatrices().translate(panelX, titleY, 0);
        context.getMatrices().scale(4.0f, 4.0f, 1f);
        context.drawText(textRenderer, "WANTED", 0, 0, Theme.TEXT, false);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(panelX + 2, titleY + 36, 0);
        context.getMatrices().scale(1.6f, 1.6f, 1f);
        context.drawText(textRenderer, "指名手配 · VISUALS", 0, 0, Theme.ACCENT, false);
        context.getMatrices().pop();

        Render2D.horizontalGradient(context, panelX, titleY + 58, 220, 1,
                Theme.ACCENT, 0x00FF3B5C);

        // Кнопки
        float buttonY = titleY + 74;
        for (MenuButton button : buttons) {
            button.render(context, panelX, buttonY, mouseX, mouseY);
            buttonY += 30;
        }

        // Информационная карточка справа
        renderInfoCard(context);

        context.drawText(textRenderer, "Minecraft 1.21.1 · Fabric", 6, height - 20, Theme.TEXT_MUTED, false);
        context.drawText(textRenderer, "Wanted Visuals v1.0 — только визуал, клиент-сайд",
                6, height - 10, Theme.TEXT_MUTED, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBackdrop(DrawContext context) {
        context.fillGradient(0, 0, width, height, BG_TOP, BG_BOTTOM);

        // «Восходящее солнце» — большой мягкий круг справа.
        float sunX = width * 0.74f;
        float sunY = height * 0.42f;
        float radius = Math.min(width, height) * 0.26f;

        for (int i = 10; i > 0; i--) {
            float r = radius * (1f + i * 0.06f);
            Render2D.circle(context, sunX, sunY, r, Theme.withAlpha(Theme.ACCENT, 0.012f * i));
        }
        Render2D.circle(context, sunX, sunY, radius, Theme.withAlpha(Theme.ACCENT, 0.85f));

        // Лучи в стиле «кёкудзицу».
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 + (System.currentTimeMillis() % 60000L) / 300.0);
            float x = sunX + (float) Math.cos(angle) * radius * 1.35f;
            float y = sunY + (float) Math.sin(angle) * radius * 1.35f;
            Render2D.circle(context, x, y, 2.5f, Theme.withAlpha(Theme.ACCENT_ALT, 0.35f));
        }

        petals.render(context, 0xFFB7C5);
        context.fillGradient(0, height / 2, width, height, 0x00000000, 0x99000000);
    }

    private void renderInfoCard(DrawContext context) {
        float cardWidth = 150;
        float x = width - cardWidth - 14;
        float y = 14;

        Render2D.roundedRect(context, x, y, cardWidth, 74, 8, Theme.withAlpha(Theme.PANEL, 0.85f));
        Render2D.roundedRect(context, x, y, 3, 74, 1.5f, Theme.ACCENT);

        context.drawText(textRenderer, "КЛИЕНТ", (int) x + 10, (int) y + 8, Theme.TEXT_DIM, false);
        context.drawText(textRenderer, "Kasa · Sky · ESP", (int) x + 10, (int) y + 22, Theme.TEXT, false);
        context.drawText(textRenderer, "ClickGUI: RIGHT SHIFT", (int) x + 10, (int) y + 34, Theme.TEXT_DIM, false);
        context.drawText(textRenderer, "Модулей: " + wanted.module.ModuleManager.getModules().size(),
                (int) x + 10, (int) y + 46, Theme.TEXT_DIM, false);
        context.drawText(textRenderer, "指名手配", (int) x + 10, (int) y + 58, Theme.ACCENT, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (MenuButton menuButton : buttons) {
                if (menuButton.hovered(mouseX, mouseY)) {
                    menuButton.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /** Кнопка меню с плавной анимацией наведения. */
    private final class MenuButton {
        private static final float WIDTH = 190;
        private static final float HEIGHT = 24;

        private final String label;
        private final String kanji;
        private final Runnable action;

        private float x;
        private float y;
        private float hover;

        private MenuButton(String label, String kanji, Runnable action) {
            this.label = label;
            this.kanji = kanji;
            this.action = action;
        }

        private void render(DrawContext context, float x, float y, int mouseX, int mouseY) {
            this.x = x;
            this.y = y;

            boolean isHovered = hovered(mouseX, mouseY);
            hover = Render2D.approach(hover, isHovered ? 1f : 0f, 0.2f);

            Render2D.roundedRect(context, x, y, WIDTH + 20 * hover, HEIGHT, 6,
                    Theme.lerpColor(0x66141620, Theme.withAlpha(Theme.ACCENT, 0.22f), hover));
            Render2D.roundedRect(context, x, y, 2 + 2 * hover, HEIGHT, 1.5f,
                    Theme.lerpColor(0x55FFFFFF, Theme.ACCENT, hover));

            context.drawText(textRenderer, label, (int) (x + 12 + 4 * hover), (int) y + 8,
                    Theme.lerpColor(Theme.TEXT_DIM, Theme.TEXT, hover), false);
            context.drawText(textRenderer, kanji,
                    (int) (x + WIDTH - 8 - textRenderer.getWidth(kanji) + 20 * hover), (int) y + 8,
                    Theme.lerpColor(Theme.TEXT_MUTED, Theme.ACCENT, hover), false);
        }

        private boolean hovered(double mouseX, double mouseY) {
            return Render2D.hovered(mouseX, mouseY, x, y, WIDTH, HEIGHT);
        }
    }
}
