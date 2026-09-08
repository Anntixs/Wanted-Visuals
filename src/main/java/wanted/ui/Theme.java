package wanted.ui;

/** Палитра клиента. Цвета не final — их переписывает {@link #apply(Palette)}. */
public final class Theme {

    /** Доступные темы оформления. */
    public enum Palette {
        WANTED("Красная"),
        FROST("Бело-синяя"),
        MONO("Чёрно-белая");

        private final String displayName;

        Palette(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Palette byName(String name) {
            for (Palette palette : values()) {
                if (palette.displayName.equalsIgnoreCase(name)) return palette;
            }
            return WANTED;
        }
    }

    public static int BACKDROP = 0xE60B0C10;
    public static int PANEL = 0xF2141620;
    public static int PANEL_SOFT = 0xF01B1E2B;
    public static int RAIL = 0xF20E1017;
    public static int CARD = 0xFF1F2331;
    public static int CARD_HOVER = 0xFF2A2F41;
    public static int OUTLINE = 0x33FFFFFF;
    public static int OUTLINE_SOFT = 0x1AFFFFFF;

    public static int ACCENT = 0xFFFF3B5C;
    public static int ACCENT_ALT = 0xFFFF8A3B;
    public static int ACCENT_COOL = 0xFF5C7CFF;

    public static int TEXT = 0xFFF2F4FA;
    public static int TEXT_DIM = 0xFF8C93A8;
    public static int TEXT_MUTED = 0xFF5A6076;

    /** Цвета категорий ClickGUI — тоже зависят от темы. */
    public static int CATEGORY_VISUAL = 0xFFFF3B5C;
    public static int CATEGORY_WORLD = 0xFF3BB2FF;
    public static int CATEGORY_HUD = 0xFFFFC53B;
    public static int CATEGORY_MISC = 0xFF9B6BFF;

    /** Фон главного меню. */
    public static int MENU_TOP = 0xFF0B0C10;
    public static int MENU_BOTTOM = 0xFF1A0E14;
    /** Цвет «солнца» и падающих частиц. */
    public static int DECOR = 0xFFFFB7C5;

    private static Palette current = Palette.WANTED;

    private Theme() {
    }

    public static Palette getPalette() {
        return current;
    }

    public static void apply(Palette palette) {
        current = palette;
        switch (palette) {
            case FROST -> {
                BACKDROP = 0xE6070C18;
                PANEL = 0xF2101A2C;
                PANEL_SOFT = 0xF016233A;
                RAIL = 0xF20A1424;
                CARD = 0xFF17263E;
                CARD_HOVER = 0xFF22364F;
                ACCENT = 0xFF4FA8FF;
                ACCENT_ALT = 0xFF9BD4FF;
                ACCENT_COOL = 0xFFE8F3FF;
                TEXT = 0xFFF2F7FF;
                TEXT_DIM = 0xFF9DB4CE;
                TEXT_MUTED = 0xFF63799A;
                CATEGORY_VISUAL = 0xFF4FA8FF;
                CATEGORY_WORLD = 0xFF7FD0FF;
                CATEGORY_HUD = 0xFFB9E2FF;
                CATEGORY_MISC = 0xFF6E86FF;
                MENU_TOP = 0xFF060B16;
                MENU_BOTTOM = 0xFF0D2038;
                DECOR = 0xFFCFE8FF;
            }
            case MONO -> {
                BACKDROP = 0xE60A0A0A;
                PANEL = 0xF2141414;
                PANEL_SOFT = 0xF01C1C1C;
                RAIL = 0xF20E0E0E;
                CARD = 0xFF1E1E1E;
                CARD_HOVER = 0xFF2C2C2C;
                ACCENT = 0xFFFFFFFF;
                ACCENT_ALT = 0xFFBFBFBF;
                ACCENT_COOL = 0xFF8A8A8A;
                TEXT = 0xFFFFFFFF;
                TEXT_DIM = 0xFF9A9A9A;
                TEXT_MUTED = 0xFF636363;
                CATEGORY_VISUAL = 0xFFFFFFFF;
                CATEGORY_WORLD = 0xFFD0D0D0;
                CATEGORY_HUD = 0xFFA0A0A0;
                CATEGORY_MISC = 0xFF767676;
                MENU_TOP = 0xFF060606;
                MENU_BOTTOM = 0xFF161616;
                DECOR = 0xFFDADADA;
            }
            default -> {
                BACKDROP = 0xE60B0C10;
                PANEL = 0xF2141620;
                PANEL_SOFT = 0xF01B1E2B;
                RAIL = 0xF20E1017;
                CARD = 0xFF1F2331;
                CARD_HOVER = 0xFF2A2F41;
                ACCENT = 0xFFFF3B5C;
                ACCENT_ALT = 0xFFFF8A3B;
                ACCENT_COOL = 0xFF5C7CFF;
                TEXT = 0xFFF2F4FA;
                TEXT_DIM = 0xFF8C93A8;
                TEXT_MUTED = 0xFF5A6076;
                CATEGORY_VISUAL = 0xFFFF3B5C;
                CATEGORY_WORLD = 0xFF3BB2FF;
                CATEGORY_HUD = 0xFFFFC53B;
                CATEGORY_MISC = 0xFF9B6BFF;
                MENU_TOP = 0xFF0B0C10;
                MENU_BOTTOM = 0xFF1A0E14;
                DECOR = 0xFFFFB7C5;
            }
        }
    }

    /** Линейная интерполяция двух ARGB-цветов. */
    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((from >> 24) & 0xFF) + (((to >> 24) & 0xFF) - ((from >> 24) & 0xFF)) * t);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, float alpha) {
        int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}
