package wanted.modules.visual;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.BooleanSetting;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;

/** Частицы и звук крита при ударе по любой сущности — чисто клиентский эффект. */
public class CritEffectModule extends Module {
    public final ModeSetting particle = register(new ModeSetting("Частицы", "Какие частицы спавнить",
            "Крит", "Крит", "Магия", "Обе", "Без частиц"));
    public final NumberSetting count = register(new NumberSetting("Количество", "Сколько частиц за удар",
            14, 1, 60, 1).visibleWhen(() -> !particle.is("Без частиц")));
    public final NumberSetting spread = register(new NumberSetting("Разлёт", "Скорость разлёта частиц",
            0.35, 0.0, 1.5, 0.05).visibleWhen(() -> !particle.is("Без частиц")));

    public final BooleanSetting sound = register(new BooleanSetting("Звук", "Играть звук крита", true));
    public final ModeSetting soundType = register(new ModeSetting("Тип звука", "Какой звук играть",
            "Крит", "Крит", "Сильный удар", "Магический крит").visibleWhen(sound::get));
    public final NumberSetting volume = register(new NumberSetting("Громкость", "Громкость звука",
            1.0, 0.1, 2.0, 0.1).visibleWhen(sound::get));
    public final NumberSetting pitch = register(new NumberSetting("Тон", "Высота звука",
            1.0, 0.5, 2.0, 0.05).visibleWhen(sound::get));

    public CritEffectModule() {
        super("CritEffect", "Криты по любой сущности", Category.VISUAL);
    }

    @Override
    public String getInfo() {
        return sound.get() ? soundType.get() : particle.get();
    }

    /** Вызывается из обработчика атаки. */
    public void onAttack(Entity target) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        spawnParticles(target);

        if (sound.get()) {
            mc.player.playSound(soundFor(soundType.get()), volume.getFloat(), pitch.getFloat());
        }
    }

    private void spawnParticles(Entity target) {
        if (particle.is("Без частиц")) return;

        double width = Math.max(0.3, target.getWidth());
        double height = Math.max(0.5, target.getHeight());
        double velocity = spread.get();

        for (int i = 0; i < count.getInt(); i++) {
            double x = target.getX() + (mc.world.random.nextDouble() - 0.5) * width * 1.4;
            double y = target.getY() + mc.world.random.nextDouble() * height;
            double z = target.getZ() + (mc.world.random.nextDouble() - 0.5) * width * 1.4;

            mc.world.addParticle(particleFor(i),
                    x, y, z,
                    (mc.world.random.nextDouble() - 0.5) * velocity,
                    mc.world.random.nextDouble() * velocity,
                    (mc.world.random.nextDouble() - 0.5) * velocity);
        }
    }

    private ParticleEffect particleFor(int index) {
        return switch (particle.get()) {
            case "Магия" -> ParticleTypes.ENCHANTED_HIT;
            case "Обе" -> index % 2 == 0 ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT;
            default -> ParticleTypes.CRIT;
        };
    }

    private static SoundEvent soundFor(String type) {
        return switch (type) {
            case "Сильный удар" -> SoundEvents.ENTITY_PLAYER_ATTACK_STRONG;
            case "Магический крит" -> SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK;
            default -> SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
        };
    }
}
