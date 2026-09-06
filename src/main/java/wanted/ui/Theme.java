package wanted.ui;

/** Единая палитра клиента: тёмный «сумидзури» фон + красный «ханамару» акцент. */
public final class Theme {
    public static final int BACKDROP = 0xE60B0C10;
    public static final int PANEL = 0xF2141620;
    public static final int PANEL_SOFT = 0xF01B1E2B;
    public static final int RAIL = 0xF20E1017;
    public static final int CARD = 0xFF1F2331;
    public static final int CARD_HOVER = 0xFF2A2F41;
    public static final int OUTLINE = 0x33FFFFFF;
    public static final int OUTLINE_SOFT = 0x1AFFFFFF;

    public static final int ACCENT = 0xFFFF3B5C;
    public static final int ACCENT_ALT = 0xFFFF8A3B;
    public static final int ACCENT_COOL = 0xFF5C7CFF;

    public static final int TEXT = 0xFFF2F4FA;
    public static final int TEXT_DIM = 0xFF8C93A8;
    public static final int TEXT_MUTED = 0xFF5A6076;

    private Theme() {
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
