package wanted.setting;

import java.util.function.Supplier;

/** База для всех настроек модуля. */
public abstract class Setting {
    private final String name;
    private final String description;
    private Supplier<Boolean> visibility = () -> true;

    protected Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /** Настройка показывается в GUI только когда условие истинно. */
    public Setting visibleWhen(Supplier<Boolean> condition) {
        this.visibility = condition;
        return this;
    }

    public boolean isVisible() {
        return visibility.get();
    }
}
