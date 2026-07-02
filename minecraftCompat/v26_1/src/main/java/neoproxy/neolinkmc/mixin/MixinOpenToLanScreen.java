package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.gui.NeoLinkConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the 26.1 LAN screen with the NeoLink configuration screen.
 *
 * @author NeoProxy Team
 */
@Mixin(targets = "net.minecraft.client.gui.screens.ShareToLanScreen")
public abstract class MixinOpenToLanScreen extends Screen {

    @Unique
    private Screen neolinkmc$lastScreen;

    protected MixinOpenToLanScreen(Component title) {
        super(title);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"), require = 0)
    private void neolinkmc$captureShareToLanLastScreen(Screen lastScreen, CallbackInfo ci) {
        this.neolinkmc$lastScreen = lastScreen;
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void neolinkmc$onInit(CallbackInfo ci) {
        NeoLinkMC.LOGGER.debug("ShareToLanScreen init intercepted; opening NeoLink config screen.");
        try {
            if (this.minecraft == null) {
                NeoLinkMC.LOGGER.error("[NeoLinkMC] Minecraft instance is null; falling back to vanilla LAN screen.");
                return;
            }

            this.minecraft.setScreenAndShow(new NeoLinkConfigScreen(this.neolinkmc$lastScreen));
            ci.cancel();
            NeoLinkMC.LOGGER.debug("Original ShareToLanScreen init cancelled.");
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("[NeoLinkMC] Failed to open config screen; falling back to vanilla LAN screen.", e);
        }
    }
}
