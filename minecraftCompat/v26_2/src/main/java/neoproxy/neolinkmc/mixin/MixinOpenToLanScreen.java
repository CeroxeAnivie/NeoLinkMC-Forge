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
 * 将 26.x 的多人选项界面替换为内网穿透配置界面。
 *
 * @author NeoProxy Team
 */
@Mixin(targets = "net.minecraft.client.gui.screens.MultiplayerOptionsScreen")
public abstract class MixinOpenToLanScreen extends Screen {

    @Unique
    private Screen neolinkmc$lastScreen;

    protected MixinOpenToLanScreen(Component title) {
        super(title);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"), require = 0)
    private void neolinkmc$captureMultiplayerOptionsLastScreen(Screen lastScreen, CallbackInfo ci) {
        this.neolinkmc$lastScreen = lastScreen;
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void neolinkmc$onInit(CallbackInfo ci) {
        NeoLinkMC.LOGGER.debug("已拦截多人选项界面，正在打开内网穿透配置界面。");
        try {
            if (this.minecraft == null) {
                NeoLinkMC.LOGGER.error("[内网穿透] Minecraft 实例为空，回退到原版多人选项界面。");
                return;
            }

            this.minecraft.setScreenAndShow(new NeoLinkConfigScreen(this.neolinkmc$lastScreen));
            ci.cancel();
            NeoLinkMC.LOGGER.debug("已取消原版多人选项界面初始化。");
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("[内网穿透] 打开配置界面失败，回退到原版多人选项界面。", e);
        }
    }
}
