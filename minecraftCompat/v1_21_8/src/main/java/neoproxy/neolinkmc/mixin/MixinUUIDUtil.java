package neoproxy.neolinkmc.mixin;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.util.UUIDFixer;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * UUIDUtil Mixin
 * 注入离线玩家UUID创建过程，实现UUID修复功能
 * 与 LanServerProperties 保持一致
 *
 * @author NeoProxy Team
 */
@Mixin(UUIDUtil.class)
public abstract class MixinUUIDUtil {

    /**
     * 在创建离线玩家UUID时注入
     * 如果启用了UUID修复，优先尝试获取正版UUID
     *
     * @param playerName 玩家名称
     * @param ci         回调信息
     */
    @Inject(method = "createOfflinePlayerUUID(Ljava/lang/String;)Ljava/util/UUID;", at = @At("HEAD"), cancellable = true)
    private static void neolinkmc$onCreateOfflinePlayerUUID(String playerName, CallbackInfoReturnable<UUID> ci) {
        NeoLinkMC.LOGGER.debug("[DEBUG] MixinUUIDUtil.onCreateOfflinePlayerUUID() 注入点触发，玩家: {}", playerName);
        UUID uuid = UUIDFixer.hookEntry(playerName);
        if (uuid != null) {
            NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复成功，玩家: {} -> UUID: {}", playerName, uuid);
            ci.setReturnValue(uuid);
            ci.cancel();
        } else {
            NeoLinkMC.LOGGER.debug("[DEBUG] UUID修复未生效，使用默认离线UUID生成，玩家: {}", playerName);
        }
    }
}
