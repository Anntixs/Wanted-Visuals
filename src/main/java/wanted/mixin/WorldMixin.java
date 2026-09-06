package wanted.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wanted.module.ModuleManager;
import wanted.modules.world.TimeChangerModule;

/** Клиентская подмена угла солнца для модуля TimeChanger. */
@Mixin(World.class)
public class WorldMixin {

    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
    private void wanted$skyAngle(float tickDelta, CallbackInfoReturnable<Float> cir) {
        TimeChangerModule timeChanger = ModuleManager.timeChanger();
        if (timeChanger == null || !timeChanger.isEnabled()) return;

        World self = (World) (Object) this;
        if (!self.isClient) return;

        cir.setReturnValue(self.getDimension().getSkyAngle(timeChanger.getTime()));
    }
}
