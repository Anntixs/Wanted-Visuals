package wanted.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wanted.module.ModuleManager;
import wanted.modules.world.SkyModule;

/** Перекраска неба модулем Sky. */
@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void wanted$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        SkyModule sky = ModuleManager.sky();
        if (sky == null || !sky.isEnabled()) return;

        Vec3d vanilla = cir.getReturnValue();
        if (vanilla == null) return;
        cir.setReturnValue(sky.getSkyColor(vanilla));
    }
}
