package wanted.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import wanted.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();

    private boolean enabled;
    private int keyCode = GLFW.GLFW_KEY_UNKNOWN;

    /** 0..1 — анимация подсветки карточки в GUI и строки в ArrayList. */
    private float animation;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public void onEnable() {
    }

    public void onDisable() {
        }

    /** Вызывается каждый клиентский тик, только когда модуль включён. */
    public void onTick() {
    }

    /** Суффикс в ArrayList, например "Sky [Sakura]". */
    public String getInfo() {
        return null;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public float getAnimation() {
        return animation;
    }

    public void setAnimation(float animation) {
        this.animation = Math.max(0f, Math.min(1f, animation));
    }
}
