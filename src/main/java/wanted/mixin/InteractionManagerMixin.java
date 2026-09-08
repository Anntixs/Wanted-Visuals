package wanted.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wanted.module.ModuleManager;

/**
 * Триггер эффекта крита. Ловим саму атаку клиента, а не Fabric-колбэк:
 * так эффект срабатывает на любой сущности и при любом исходе удара.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class InteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void wanted$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (ModuleManager.critEffect() != null) {
            ModuleManager.critEffect().onAttack(target);
        }
    }
}
