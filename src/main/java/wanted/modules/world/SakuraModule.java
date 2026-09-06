package wanted.modules.world;

import net.minecraft.particle.ParticleTypes;
import wanted.module.Category;
import wanted.module.Module;
import wanted.setting.NumberSetting;

/** Падающие лепестки сакуры вокруг игрока. */
public class SakuraModule extends Module {
    public final NumberSetting density = register(new NumberSetting("Плотность", "Лепестков за тик",
            3.0, 1.0, 20.0, 1.0));
    public final NumberSetting radius = register(new NumberSetting("Радиус", "Радиус спавна вокруг игрока",
            12.0, 4.0, 32.0, 1.0));
    public final NumberSetting altitude = register(new NumberSetting("Высота", "На сколько блоков выше игрока",
            10.0, 2.0, 24.0, 1.0));

    public SakuraModule() {
        super("Sakura", "Лепестки сакуры вокруг игрока", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        for (int i = 0; i < density.getInt(); i++) {
            double x = mc.player.getX() + (mc.world.random.nextDouble() - 0.5) * radius.get() * 2;
            double z = mc.player.getZ() + (mc.world.random.nextDouble() - 0.5) * radius.get() * 2;
            double y = mc.player.getY() + altitude.get() * mc.world.random.nextDouble();
            mc.world.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, 0.0, -0.02, 0.0);
        }
    }
}
