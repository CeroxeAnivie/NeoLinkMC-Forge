package neoproxy.neolinkmc.service;

import neoproxy.neolinkmc.NeoLinkMC;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Minecraft 消息处理器实现 - 简化版本
 * <p>
 * 实现 MessageHandler 接口，负责：
 * 1. 向 Minecraft 聊天栏发送消息
 * 2. 向 NeoLinkMC 日志系统记录日志
 *
 * @author NeoProxy Team
 * @version 0.3.1
 */
public final class MinecraftMessageHandler implements MessageHandler {

    private static final String PREFIX = "[NLMC] ";
    private static final String CHAT_PREFIX = "§b";

    @Override
    public void send(@NotNull String message, @NotNull MessageType type) {
        String colorCode = switch (type) {
            case SUCCESS -> "§a";
            case WARNING -> "§e";
            case ERROR -> "§c";
            default -> "§b"; // INFO
        };

        sendToChat(colorCode + PREFIX + (type == MessageType.ERROR ? "§f" : "") + message);
    }

    @Override
    public void log(@NotNull String message, @NotNull LogLevel level) {
        switch (level) {
            case DEBUG -> NeoLinkMC.LOGGER.debug("[DEBUG] {}", message);
            case INFO -> NeoLinkMC.LOGGER.info("[NeoLink] {}", message);
            case WARN -> NeoLinkMC.LOGGER.warn(message);
            case ERROR -> NeoLinkMC.LOGGER.error(message);
        }
    }

    @Override
    public void log(@NotNull String message, @NotNull LogLevel level, @NotNull Throwable throwable) {
        switch (level) {
            case DEBUG -> NeoLinkMC.LOGGER.debug("[DEBUG] {}", message, throwable);
            case INFO -> NeoLinkMC.LOGGER.info("[NeoLink] {}", message, throwable);
            case WARN -> NeoLinkMC.LOGGER.warn(message, throwable);
            case ERROR -> NeoLinkMC.LOGGER.error(message, throwable);
        }
    }

    /**
     * 发送消息到 Minecraft 聊天栏
     */
    private void sendToChat(String message) {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.execute(() -> {
                    if (client.player != null) {
                        // 26.1 使用 sendSystemMessage 代替 displayClientMessage
                        client.player.sendSystemMessage(Component.literal(message));
                    }
                });
            }
        } catch (Exception e) {
            NeoLinkMC.LOGGER.debug("Failed to send chat message.", e);
        }
    }
}
