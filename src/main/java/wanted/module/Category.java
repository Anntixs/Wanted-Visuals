package wanted.module;

import wanted.ui.Theme;

/** Категории для боковой панели ClickGUI. */
public enum Category {
    VISUAL("Visual", "V"),
    WORLD("World", "W"),
    HUD("HUD", "H"),
    MISC("Misc", "M");

    private final String displayName;
    private final String glyph;

    Category(String displayName, String glyph) {
        this.displayName = displayName;
        this.glyph = glyph;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGlyph() {
        return glyph;
    }

    /** Цвет берётся из активной темы, поэтому переключение палитры видно сразу. */
    public int getAccent() {
        return switch (this) {
            case VISUAL -> Theme.CATEGORY_VISUAL;
            case WORLD -> Theme.CATEGORY_WORLD;
            case HUD -> Theme.CATEGORY_HUD;
            case MISC -> Theme.CATEGORY_MISC;
        };
    }
}
