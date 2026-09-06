package wanted.module;

/** Категории с японскими глифами для боковой панели ClickGUI. */
public enum Category {
    VISUAL("Visual", "視", 0xFFFF3B5C),
    WORLD("World", "空", 0xFF3BB2FF),
    HUD("HUD", "面", 0xFFFFC53B),
    MISC("Misc", "他", 0xFF9B6BFF);

    private final String displayName;
    private final String glyph;
    private final int accent;

    Category(String displayName, String glyph, int accent) {
        this.displayName = displayName;
        this.glyph = glyph;
        this.accent = accent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGlyph() {
        return glyph;
    }

    public int getAccent() {
        return accent;
    }
}
