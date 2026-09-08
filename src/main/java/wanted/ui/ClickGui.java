package wanted.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import wanted.module.Category;
import wanted.module.Module;
import wanted.module.ModuleManager;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;
import wanted.setting.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Современный ClickGUI: слева — рейл категорий с кандзи, по центру — карточки модулей,
 * справа — панель настроек выбранного модуля.
 */
public class ClickGui extends Screen {
    private static final float MAX_PANEL_WIDTH = 470f;
    private static final float MAX_PANEL_HEIGHT = 280f;
    private static final float RAIL_WIDTH = 58f;
    private static final float MAX_SETTINGS_WIDTH = 168f;
    private static final float HEADER_HEIGHT = 40f;
    private static final float CARD_HEIGHT = 30f;

    private static Category selectedCategory = Category.VISUAL;
    private static Module selectedModule;

    private final Petals petals = new Petals(40);

    private float panelX;
    private float panelY;
    /** Размеры пересчитываются под экран: на GUI scale 2-3 фиксированная панель не влезала. */
    private float panelWidth;
    private float panelHeight;
    private float settingsWidth;
    private float railIndicator = -1f;
    private float listScroll;
    private float settingsScroll;

    private String search = "";
    private boolean searchFocused;
    private Module bindingModule;

    /** Активный ползунок: настройка + признак, что тянут именно цветовой ползунок. */
    private Setting draggedSetting;
    private int draggedComponent;

    public ClickGui() {
        super(Text.literal("Wanted Visuals"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        wanted.config.ConfigManager.save();
        super.close();
    }

    private void layout() {
        panelWidth = Math.min(MAX_PANEL_WIDTH, width - 16f);
        panelHeight = Math.min(MAX_PANEL_HEIGHT, height - 16f);
        settingsWidth = Math.min(MAX_SETTINGS_WIDTH, panelWidth * 0.36f);
        panelX = (width - panelWidth) / 2f;
        panelY = (height - panelHeight) / 2f;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, Theme.BACKDROP);
        petals.render(context, Theme.DECOR);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        layout();
        // super.render сам вызывает renderBackground — иначе фон рисуется поверх панели.
        super.render(context, mouseX, mouseY, delta);

        Render2D.shadow(context, panelX, panelY, panelWidth, panelHeight, 10);
        Render2D.roundedRect(context, panelX, panelY, panelWidth, panelHeight, 10, Theme.PANEL);

        renderRail(context, mouseX, mouseY);
        renderHeader(context, mouseX, mouseY);
        renderModules(context, mouseX, mouseY);
        renderSettings(context, mouseX, mouseY);
    }

    // ------------------------------------------------------------------ рейл

    private void renderRail(DrawContext context, int mouseX, int mouseY) {
        Render2D.roundedRect(context, panelX, panelY, RAIL_WIDTH, panelHeight, 10, Theme.RAIL);

        Category[] categories = Category.values();
        float y = panelY + HEADER_HEIGHT + 6;
        float targetIndicator = y;

        for (Category category : categories) {
            boolean active = category == selectedCategory;
            boolean hovered = Render2D.hovered(mouseX, mouseY, panelX + 6, y, RAIL_WIDTH - 12, 40);

            if (active) targetIndicator = y;

            int background = active ? Theme.withAlpha(category.getAccent(), 0.16f)
                    : hovered ? Theme.OUTLINE_SOFT : 0x00000000;
            Render2D.roundedRect(context, panelX + 6, y, RAIL_WIDTH - 12, 40, 8, background);

            int glyphColor = active ? category.getAccent() : Theme.TEXT_DIM;
            drawCentered(context, category.getGlyph(), panelX + RAIL_WIDTH / 2f, y + 10, glyphColor, 1.4f);
            drawCentered(context, category.getDisplayName().toUpperCase(),
                    panelX + RAIL_WIDTH / 2f, y + 27, active ? Theme.TEXT : Theme.TEXT_MUTED, 0.65f);

            y += 46;
        }

        if (railIndicator < 0) railIndicator = targetIndicator;
        railIndicator = Render2D.approach(railIndicator, targetIndicator, 0.3f);
        Render2D.roundedRect(context, panelX + 1.5f, railIndicator + 8, 3, 24, 1.5f, selectedCategory.getAccent());

        drawCentered(context, "WV", panelX + RAIL_WIDTH / 2f, panelY + panelHeight - 26,
                Theme.TEXT_MUTED, 1.0f);
    }

    // ---------------------------------------------------------------- шапка

    private void renderHeader(DrawContext context, int mouseX, int mouseY) {
        float x = panelX + RAIL_WIDTH;
        float width = panelWidth - RAIL_WIDTH;

        Render2D.horizontalGradient(context, x, panelY + HEADER_HEIGHT - 1, width, 1,
                Theme.withAlpha(selectedCategory.getAccent(), 0.65f), 0x00000000);

        context.getMatrices().push();
        context.getMatrices().translate(x + 14, panelY + 11, 0);
        context.getMatrices().scale(1.25f, 1.25f, 1f);
        context.drawText(textRenderer, "WANTED", 0, 0, Theme.TEXT, false);
        context.getMatrices().pop();

        int titleWidth = (int) (textRenderer.getWidth("WANTED") * 1.25f);
        context.drawText(textRenderer, "VISUALS", (int) x + 18 + titleWidth, (int) panelY + 14,
                selectedCategory.getAccent(), false);
        context.drawText(textRenderer, "v1.0 · 1.21.1", (int) x + 18 + titleWidth, (int) panelY + 25,
                Theme.TEXT_MUTED, false);

        // Поле поиска
        float searchWidth = Math.min(150f, panelWidth * 0.32f);
        float searchX = panelX + panelWidth - searchWidth - 12;
        float searchY = panelY + 11;
        boolean hovered = Render2D.hovered(mouseX, mouseY, searchX, searchY, searchWidth, 18);

        Render2D.roundedRect(context, searchX, searchY, searchWidth, 18, 9,
                searchFocused ? Theme.CARD_HOVER : hovered ? Theme.CARD : Theme.PANEL_SOFT);
        if (searchFocused) {
            Render2D.roundedOutline(context, searchX, searchY, searchWidth, 18, 9, 1f,
                    Theme.withAlpha(selectedCategory.getAccent(), 0.7f));
        }

        String label = search.isEmpty() && !searchFocused ? "поиск модуля…" : search;
        int labelColor = search.isEmpty() ? Theme.TEXT_MUTED : Theme.TEXT;
        context.drawText(textRenderer, label + (searchFocused ? "_" : ""),
                (int) searchX + 9, (int) searchY + 5, labelColor, false);
    }

    // -------------------------------------------------------------- модули

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : ModuleManager.getModules()) {
            boolean matchesSearch = !search.isEmpty()
                    && module.getName().toLowerCase().contains(search.toLowerCase());
            if (search.isEmpty() ? module.getCategory() == selectedCategory : matchesSearch) {
                result.add(module);
            }
        }
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    private float listX() {
        return panelX + RAIL_WIDTH + 10;
    }

    private float listWidth() {
        return panelWidth - RAIL_WIDTH - settingsWidth - 28;
    }

    private float contentY() {
        return panelY + HEADER_HEIGHT + 8;
    }

    private float contentHeight() {
        return panelHeight - HEADER_HEIGHT - 18;
    }

    private void renderModules(DrawContext context, int mouseX, int mouseY) {
        float x = listX();
        float width = listWidth();
        float top = contentY();
        float height = contentHeight();

        context.enableScissor((int) x, (int) top, (int) (x + width), (int) (top + height));

        float y = top - listScroll;
        for (Module module : visibleModules()) {
            if (y + CARD_HEIGHT >= top && y <= top + height) {
                renderModuleCard(context, module, x, y, width, mouseX, mouseY);
            }
            y += CARD_HEIGHT + 5;
        }

        context.disableScissor();
    }

    private void renderModuleCard(DrawContext context, Module module, float x, float y, float width,
                                  int mouseX, int mouseY) {
        boolean hovered = Render2D.hovered(mouseX, mouseY, x, y, width, CARD_HEIGHT);
        boolean selected = module == selectedModule;
        int accent = module.getCategory().getAccent();

        int background = Theme.lerpColor(hovered ? Theme.CARD_HOVER : Theme.CARD,
                Theme.withAlpha(accent, 0.22f), module.getAnimation() * 0.8f);
        Render2D.roundedRect(context, x, y, width, CARD_HEIGHT, 7, background);

        if (selected) {
            Render2D.roundedOutline(context, x, y, width, CARD_HEIGHT, 7, 1f, Theme.withAlpha(accent, 0.8f));
        }
        Render2D.roundedRect(context, x + 3, y + 6, 2.5f, CARD_HEIGHT - 12, 1.25f,
                Theme.withAlpha(accent, 0.25f + module.getAnimation() * 0.75f));

        context.drawText(textRenderer, module.getName(), (int) x + 12, (int) y + 6,
                module.isEnabled() ? Theme.TEXT : Theme.TEXT_DIM, false);

        String subtitle = bindingModule == module
                ? "нажмите клавишу…"
                : module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN
                    ? "[" + keyName(module.getKeyCode()) + "] " + module.getDescription()
                    : module.getDescription();
        context.drawText(textRenderer, trim(subtitle, (int) width - 60), (int) x + 12, (int) y + 17,
                Theme.TEXT_MUTED, false);

        // Переключатель
        float toggleWidth = 24;
        float toggleX = x + width - toggleWidth - 8;
        float toggleY = y + CARD_HEIGHT / 2f - 6;
        Render2D.roundedRect(context, toggleX, toggleY, toggleWidth, 12, 6,
                Theme.lerpColor(0xFF2A2F41, accent, module.getAnimation()));
        Render2D.circle(context, toggleX + 6 + 12 * module.getAnimation(), toggleY + 6, 4.5f, Theme.TEXT);
    }

    // ------------------------------------------------------------ настройки

    private float settingsX() {
        return panelX + panelWidth - settingsWidth - 10;
    }

    private void renderSettings(DrawContext context, int mouseX, int mouseY) {
        float x = settingsX();
        float top = contentY();
        float height = contentHeight();

        Render2D.roundedRect(context, x, top, settingsWidth, height, 8, Theme.PANEL_SOFT);

        if (selectedModule == null) {
            drawCentered(context, "выберите модуль", x + settingsWidth / 2f, top + height / 2f - 8,
                    Theme.TEXT_MUTED, 1f);
            drawCentered(context, "SETTINGS", x + settingsWidth / 2f, top + height / 2f + 6,
                    Theme.TEXT_MUTED, 1f);
            return;
        }

        context.enableScissor((int) x, (int) top, (int) (x + settingsWidth), (int) (top + height));

        float y = top + 10 - settingsScroll;
        context.drawText(textRenderer, selectedModule.getName(), (int) x + 10, (int) y, Theme.TEXT, false);
        y += 11;
        context.drawText(textRenderer, trim(selectedModule.getDescription(), (int) settingsWidth - 20),
                (int) x + 10, (int) y, Theme.TEXT_MUTED, false);
        y += 16;

        for (Setting setting : selectedModule.getSettings()) {
            if (!setting.isVisible()) continue;
            y = renderSetting(context, setting, x + 10, y, settingsWidth - 20, mouseX, mouseY);
        }

        context.disableScissor();
    }

    private float renderSetting(DrawContext context, Setting setting, float x, float y, float width,
                                int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting bool) {
            context.drawText(textRenderer, setting.getName(), (int) x, (int) y + 2, Theme.TEXT_DIM, false);
            float toggleX = x + width - 22;
            Render2D.roundedRect(context, toggleX, y, 22, 11, 5.5f,
                    bool.get() ? selectedModule.getCategory().getAccent() : 0xFF2A2F41);
            Render2D.circle(context, toggleX + (bool.get() ? 16.5f : 5.5f), y + 5.5f, 4f, Theme.TEXT);
            return y + 18;
        }

        if (setting instanceof NumberSetting number) {
            context.drawText(textRenderer, setting.getName(), (int) x, (int) y, Theme.TEXT_DIM, false);
            String value = number.get() == Math.floor(number.get())
                    ? String.valueOf(number.getInt())
                    : String.format("%.2f", number.get());
            context.drawText(textRenderer, value,
                    (int) (x + width - textRenderer.getWidth(value)), (int) y, Theme.TEXT, false);

            float trackY = y + 12;
            Render2D.roundedRect(context, x, trackY, width, 4, 2, 0xFF2A2F41);
            float fill = (float) (width * number.getFraction());
            Render2D.roundedRect(context, x, trackY, fill, 4, 2, selectedModule.getCategory().getAccent());
            Render2D.circle(context, x + fill, trackY + 2, 4f, Theme.TEXT);
            return y + 24;
        }

        if (setting instanceof ModeSetting mode) {
            context.drawText(textRenderer, setting.getName(), (int) x, (int) y, Theme.TEXT_DIM, false);
            float pillY = y + 11;
            Render2D.roundedRect(context, x, pillY, width, 14, 7, Theme.CARD);
            drawCentered(context, "‹  " + mode.get() + "  ›", x + width / 2f, pillY + 3, Theme.TEXT, 1f);
            return y + 30;
        }

        if (setting instanceof ColorSetting color) {
            context.drawText(textRenderer, setting.getName(), (int) x, (int) y, Theme.TEXT_DIM, false);
            Render2D.roundedRect(context, x + width - 16, y - 1, 14, 10, 3, color.getArgb());

            float hueY = y + 12;
            for (int i = 0; i < (int) width; i++) {
                int hueColor = java.awt.Color.HSBtoRGB(i / width, 0.85f, 1f) | 0xFF000000;
                context.fill((int) x + i, (int) hueY, (int) x + i + 1, (int) hueY + 5, hueColor);
            }
            Render2D.circle(context, x + width * color.getHue(), hueY + 2.5f, 3.5f, Theme.TEXT);

            float alphaY = y + 22;
            Render2D.roundedRect(context, x, alphaY, width, 4, 2, 0xFF2A2F41);
            Render2D.roundedRect(context, x, alphaY, width * color.getAlpha(), 4, 2, color.getArgb(1f));
            Render2D.circle(context, x + width * color.getAlpha(), alphaY + 2, 3.5f, Theme.TEXT);

            float rainbowY = y + 31;
            context.drawText(textRenderer, "радуга", (int) x, (int) rainbowY, Theme.TEXT_MUTED, false);
            Render2D.roundedRect(context, x + width - 20, rainbowY - 1, 18, 9, 4.5f,
                    color.isRainbow() ? selectedModule.getCategory().getAccent() : 0xFF2A2F41);
            Render2D.circle(context, x + width - 20 + (color.isRainbow() ? 13.5f : 4.5f), rainbowY + 3.5f,
                    3f, Theme.TEXT);
            return y + 46;
        }

        return y + 14;
    }

    // ------------------------------------------------------------- ввод

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        layout();

        // Рейл категорий
        float y = panelY + HEADER_HEIGHT + 6;
        for (Category category : Category.values()) {
            if (Render2D.hovered(mouseX, mouseY, panelX + 6, y, RAIL_WIDTH - 12, 40)) {
                selectedCategory = category;
                listScroll = 0;
                search = "";
                return true;
            }
            y += 46;
        }

        // Поиск
        float searchWidth = Math.min(150f, panelWidth * 0.32f);
        float searchX = panelX + panelWidth - searchWidth - 12;
        searchFocused = Render2D.hovered(mouseX, mouseY, searchX, panelY + 11, searchWidth, 18);
        if (searchFocused) return true;

        if (handleModuleClick(mouseX, mouseY, button)) return true;
        if (handleSettingsClick(mouseX, mouseY, button)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleModuleClick(double mouseX, double mouseY, int button) {
        float x = listX();
        float width = listWidth();
        float top = contentY();
        if (!Render2D.hovered(mouseX, mouseY, x, top, width, contentHeight())) return false;

        float y = top - listScroll;
        for (Module module : visibleModules()) {
            if (Render2D.hovered(mouseX, mouseY, x, y, width, CARD_HEIGHT)) {
                switch (button) {
                    case 0 -> {
                        module.toggle();
                        selectedModule = module;
                    }
                    case 1 -> selectedModule = module;
                    case 2 -> bindingModule = module;
                    default -> {
                    }
                }
                return true;
            }
            y += CARD_HEIGHT + 5;
        }
        return true;
    }

    private boolean handleSettingsClick(double mouseX, double mouseY, int button) {
        if (selectedModule == null) return false;

        float x = settingsX() + 10;
        float width = settingsWidth - 20;
        float top = contentY();
        if (!Render2D.hovered(mouseX, mouseY, settingsX(), top, settingsWidth, contentHeight())) return false;

        float y = top + 10 - settingsScroll + 27;
        for (Setting setting : selectedModule.getSettings()) {
            if (!setting.isVisible()) continue;

            if (setting instanceof BooleanSetting bool) {
                if (Render2D.hovered(mouseX, mouseY, x, y - 2, width, 15)) {
                    bool.toggle();
                    return true;
                }
                y += 18;
            } else if (setting instanceof NumberSetting number) {
                if (Render2D.hovered(mouseX, mouseY, x, y + 8, width, 12)) {
                    number.setFraction((mouseX - x) / width);
                    draggedSetting = setting;
                    draggedComponent = 0;
                    return true;
                }
                y += 24;
            } else if (setting instanceof ModeSetting mode) {
                if (Render2D.hovered(mouseX, mouseY, x, y + 11, width, 14)) {
                    mode.cycle(button == 1 ? -1 : 1);
                    return true;
                }
                y += 30;
            } else if (setting instanceof ColorSetting color) {
                if (Render2D.hovered(mouseX, mouseY, x, y + 10, width, 9)) {
                    color.setHue((float) ((mouseX - x) / width));
                    draggedSetting = setting;
                    draggedComponent = 1;
                    return true;
                }
                if (Render2D.hovered(mouseX, mouseY, x, y + 20, width, 8)) {
                    color.setAlpha((float) ((mouseX - x) / width));
                    draggedSetting = setting;
                    draggedComponent = 2;
                    return true;
                }
                if (Render2D.hovered(mouseX, mouseY, x, y + 29, width, 12)) {
                    color.setRainbow(!color.isRainbow());
                    return true;
                }
                y += 46;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggedSetting instanceof NumberSetting number && draggedComponent == 0) {
            float x = settingsX() + 10;
            float width = settingsWidth - 20;
            number.setFraction((mouseX - x) / width);
            return true;
        }
        if (draggedSetting instanceof ColorSetting color) {
            float x = settingsX() + 10;
            float width = settingsWidth - 20;
            float fraction = (float) Math.max(0, Math.min(1, (mouseX - x) / width));
            if (draggedComponent == 1) color.setHue(fraction);
            if (draggedComponent == 2) color.setAlpha(fraction);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggedSetting = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        layout();
        float amount = (float) verticalAmount * 14f;

        if (Render2D.hovered(mouseX, mouseY, settingsX(), contentY(), settingsWidth, contentHeight())) {
            settingsScroll = Math.max(0, settingsScroll - amount);
        } else {
            float contentSize = visibleModules().size() * (CARD_HEIGHT + 5);
            float max = Math.max(0, contentSize - contentHeight());
            listScroll = Math.max(0, Math.min(max, listScroll - amount));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            bindingModule.setKeyCode(keyCode == GLFW.GLFW_KEY_DELETE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            bindingModule = null;
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            search += chr;
            listScroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // ------------------------------------------------------------ помощники

    private void drawCentered(DrawContext context, String text, float centerX, float y, int color, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX, y, 0);
        context.getMatrices().scale(scale, scale, 1f);
        context.drawText(textRenderer, text, -textRenderer.getWidth(text) / 2, 0, color, false);
        context.getMatrices().pop();
    }

    private String trim(String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        return textRenderer.trimToWidth(text, Math.max(8, maxWidth - 6)) + "…";
    }

    private static String keyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name == null ? "KEY " + keyCode : name.toUpperCase();
    }
}
