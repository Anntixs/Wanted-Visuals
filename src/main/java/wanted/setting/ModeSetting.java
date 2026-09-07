package wanted.setting;

import java.util.function.Supplier;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description);
        this.modes = Arrays.asList(modes);
        this.index = Math.max(0, this.modes.indexOf(defaultMode));
    }

    public String get() {
        return modes.get(index);
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    public List<String> getModes() {
        return modes;
    }

    public int getIndex() {
        return index;
    }

    public void set(String mode) {
        int found = modes.indexOf(mode);
        if (found >= 0) index = found;
    }

    public void cycle(int direction) {
        index = Math.floorMod(index + direction, modes.size());
    }

    /** Ковариантный возврат, чтобы цепочка сохраняла конкретный тип настройки. */
    @Override
    public ModeSetting visibleWhen(Supplier<Boolean> condition) {
        super.visibleWhen(condition);
        return this;
    }
}
