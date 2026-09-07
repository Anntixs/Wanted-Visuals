package wanted.setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description);
        this.value = defaultValue;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    /** Ковариантный возврат, чтобы цепочка сохраняла конкретный тип настройки. */
    @Override
    public BooleanSetting visibleWhen(Supplier<Boolean> condition) {
        super.visibleWhen(condition);
        return this;
    }
}
