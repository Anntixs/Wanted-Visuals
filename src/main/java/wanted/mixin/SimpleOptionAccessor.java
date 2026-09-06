package wanted.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Прямая запись значения настройки в обход валидации (нужно для Fullbright). */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void wanted$setValue(T value);
}
