package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听返回标题界面事件，并关闭残留的内网穿透状态。
 */
@Mixin(targets = "net.minecraft.client.gui.screens.TitleScreen")
public abstract class MixinTitleScreen {

    @Inject(method = "init()V", at = @At("RETURN"))
    private void neolinkmc$afterTitleScreenInit(CallbackInfo ci) {
        NeoLinkCore.onTitleScreenTick();
    }
}
