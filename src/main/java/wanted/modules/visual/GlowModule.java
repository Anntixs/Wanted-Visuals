package wanted.modules.visual;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;

import java.util.ArrayList;
import java.util.List;

/** Подсветка контуров сущностей (клиентский glow-эффект). */
public class GlowModule extends Module {
    public final BooleanSetting players = register(new BooleanSetting("Игроки", "Подсвечивать игроков", true));
    public final BooleanSetting hostile = register(new BooleanSetting("Мобы", "Подсвечивать враждебных мобов", false));
    public final BooleanSetting passive = register(new BooleanSetting("Животные", "Подсвечивать пассивных мобов", false));
    public final BooleanSetting items = register(new BooleanSetting("Предметы", "Подсвечивать дропнутые предметы", false));

    private final List<Entity> affected = new ArrayList<>();

    public GlowModule() {
        super("Glow", "Контурная подсветка сущностей", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        clearAll();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!shouldGlow(entity)) continue;
            entity.setGlowing(true);
            affected.add(entity);
        }
    }

    private boolean shouldGlow(Entity entity) {
        if (entity instanceof PlayerEntity) return players.get();
        if (entity instanceof HostileEntity) return hostile.get();
        if (entity instanceof PassiveEntity) return passive.get();
        if (entity instanceof net.minecraft.entity.ItemEntity) return items.get();
        return false;
    }

    private void clearAll() {
        for (Entity entity : affected) {
            entity.setGlowing(false);
        }
        affected.clear();
    }

    @Override
    public void onDisable() {
        clearAll();
    }
}
