package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkCore;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Watches for a return to the title screen and shuts down any leftover tunnel state.
 *
 * <p>This keeps the cleanup hook narrowly scoped to the title screen instead of intercepting
 * every screen close event, which avoids swallowing unrelated UI interactions.</p>
 */
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen {

    @Inject(method = "init()V", at = @At("RETURN"))
    private void neolinkmc$afterTitleScreenInit(CallbackInfo ci) {
        NeoLinkCore.onTitleScreenTick();
    }
}
