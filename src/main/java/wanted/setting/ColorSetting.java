package wanted.setting;

import java.util.function.Supplier;

import java.awt.Color;

/** HSB-цвет с опциональной радугой. */
public class ColorSetting extends Setting {
    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;
    private boolean rainbow;
    private float rainbowSpeed = 1.0f;

    public ColorSetting(String name, String description, int argb) {
        super(name, description);
        Color color = new Color(argb, true);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = color.getAlpha() / 255f;
    }

    public int getArgb() {
        float h = hue;
        if (rainbow) {
            h = (float) (((System.currentTimeMillis() % (long) (4000 / rainbowSpeed)) / (4000.0 / rainbowSpeed)) % 1.0);
        }
        int rgb = Color.HSBtoRGB(h, saturation, brightness) & 0x00FFFFFF;
        return ((int) (alpha * 255) << 24) | rgb;
    }

    /** Тот же цвет, но с другой прозрачностью (0..1). */
    public int getArgb(float customAlpha) {
        return (getArgb() & 0x00FFFFFF) | ((int) (Math.max(0f, Math.min(1f, customAlpha)) * 255) << 24);
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = clamp01(hue);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = clamp01(saturation);
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = clamp01(brightness);
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = clamp01(alpha);
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(float speed) {
        this.rainbowSpeed = Math.max(0.1f, speed);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    /** Ковариантный возврат, чтобы цепочка сохраняла конкретный тип настройки. */
    @Override
    public ColorSetting visibleWhen(Supplier<Boolean> condition) {
        super.visibleWhen(condition);
        return this;
    }
}
