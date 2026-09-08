package wanted.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wanted.WantedClient;
import wanted.ui.WantedTitleScreen;

/** Замена ванильного главного меню на кастомное. */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void wanted$replaceMenu(CallbackInfo ci) {
        if (!WantedClient.customMainMenu) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof WantedTitleScreen) return;

        ci.cancel();
        client.execute(() -> client.setScreen(new WantedTitleScreen()));
    }
}
