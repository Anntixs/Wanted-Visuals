package wanted.setting;

import java.util.function.Supplier;

import net.minecraft.util.math.MathHelper;

public class NumberSetting extends Setting {
    private final double min;
    private final double max;
    private final double step;
    private double value;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = defaultValue;
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void set(double newValue) {
        double clamped = MathHelper.clamp(newValue, min, max);
        this.value = Math.round(clamped / step) * step;
    }

    /** 0..1 — позиция ползунка в GUI. */
    public double getFraction() {
        return (value - min) / (max - min);
    }

    public void setFraction(double fraction) {
        set(min + MathHelper.clamp(fraction, 0.0, 1.0) * (max - min));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /** Ковариантный возврат, чтобы цепочка сохраняла конкретный тип настройки. */
    @Override
    public NumberSetting visibleWhen(Supplier<Boolean> condition) {
        super.visibleWhen(condition);
        return this;
    }
}
