package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkMC;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Screen 的 Mixin 注入
 * 用于确保当任何屏幕关闭时，NeoLinkMC 服务都会被停止
 */
@Mixin(Screen.class)
public class MixinScreen {

    /**
     * 屏幕关闭时注入 - 停止NeoLinkMC服务
     * 这是一个全局的保险机制，确保无论如何服务都会被停止
     */
    @Inject(method = "onClose()V", at = @At("HEAD"))
    private void neolinkmc$onScreenClose(CallbackInfo ci) {
        // 检查当前屏幕是否是 ShareToLanScreen 或其子类
        Screen screen = (Screen) (Object) this;
        String screenClassName = screen.getClass().getSimpleName();
        String fullClassName = screen.getClass().getName();

        NeoLinkMC.LOGGER.debug("[DEBUG] MixinScreen.onClose() 被调用，屏幕类名: {}, 完整类名: {}", screenClassName, fullClassName);

        // 如果是 ShareToLanScreen 或其子类，停止服务
        if (screenClassName.contains("ShareToLan") || screenClassName.contains("Lan")) {
            NeoLinkMC.LOGGER.info("[NeoLinkMC] 检测到局域网屏幕关闭 [{}]，准备停止服务...", screenClassName);
            try {
                NeoLinkMC.stopService();
                NeoLinkMC.LOGGER.info("[NeoLinkMC] 局域网屏幕关闭，服务已停止");
            } catch (Exception e) {
                NeoLinkMC.LOGGER.error("[NeoLinkMC] 屏幕关闭时停止服务出错", e);
                NeoLinkMC.LOGGER.debug("[DEBUG] 停止服务异常堆栈: ", e);
            }
        } else {
            NeoLinkMC.LOGGER.debug("[DEBUG] 屏幕 {} 不是局域网屏幕，跳过停止服务", screenClassName);
        }
    }
}
