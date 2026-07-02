package neoproxy.neolinkmc.config;

import net.minecraft.world.level.GameType;

import java.nio.file.Path;

/**
 * 面向 Fabric 的兼容 facade，用于 Minecraft-specific enum 转换。
 *
 * <p>所有持久化和 primitive 校验都位于 common。本类存在的唯一原因是 GUI
 * 天然使用 Mojang 的 {@link GameType} enum。</p>
 */
public final class NeoLinkConfig {
    public static final String DEFAULT_REMOTE_DOMAIN = SharedNeoLinkConfig.DEFAULT_REMOTE_DOMAIN;
    public static final int DEFAULT_LOCAL_PORT = SharedNeoLinkConfig.DEFAULT_LOCAL_PORT;
    public static final int DEFAULT_HOOK_PORT = SharedNeoLinkConfig.DEFAULT_HOOK_PORT;
    public static final int DEFAULT_HOST_CONNECT_PORT = SharedNeoLinkConfig.DEFAULT_HOST_CONNECT_PORT;
    public static final boolean DEFAULT_PVP_ALLOWED = SharedNeoLinkConfig.DEFAULT_PVP_ALLOWED;
    public static final boolean DEFAULT_ALLOW_CHEATS = SharedNeoLinkConfig.DEFAULT_ALLOW_CHEATS;
    public static final int DEFAULT_MAX_PLAYERS = SharedNeoLinkConfig.DEFAULT_MAX_PLAYERS;
    public static final GameType DEFAULT_GAME_TYPE = GameType.SURVIVAL;
    public static final OnlineMode DEFAULT_ONLINE_MODE = SharedNeoLinkConfig.DEFAULT_ONLINE_MODE;

    private NeoLinkConfig() {
    }

    public static void init(Path configDir) {
        SharedNeoLinkConfig.init(configDir);
    }

    public static void load() {
        SharedNeoLinkConfig.load();
    }

    public static void save() {
        SharedNeoLinkConfig.save();
    }

    public static Path getConfigDir() {
        return SharedNeoLinkConfig.getConfigDir();
    }

    public static Path getModConfigDir() {
        return SharedNeoLinkConfig.getModConfigDir();
    }

    public static String getRemoteDomain() {
        return SharedNeoLinkConfig.getRemoteDomain();
    }

    public static int getLocalPort() {
        return SharedNeoLinkConfig.getLocalPort();
    }

    public static int getHookPort() {
        return SharedNeoLinkConfig.getHookPort();
    }

    public static int getHostConnectPort() {
        return SharedNeoLinkConfig.getHostConnectPort();
    }

    public static boolean isPvpAllowed() {
        return SharedNeoLinkConfig.isPvpAllowed();
    }

    public static boolean isAllowCheats() {
        return SharedNeoLinkConfig.isAllowCheats();
    }

    public static int getMaxPlayers() {
        return SharedNeoLinkConfig.getMaxPlayers();
    }

    public static GameType getGameType() {
        try {
            return GameType.valueOf(SharedNeoLinkConfig.getGameTypeName());
        } catch (IllegalArgumentException e) {
            return DEFAULT_GAME_TYPE;
        }
    }

    public static OnlineMode getOnlineMode() {
        return SharedNeoLinkConfig.getOnlineMode();
    }

    public static void setRemoteDomain(String domain) {
        SharedNeoLinkConfig.setRemoteDomain(domain);
    }

    public static void setLocalPort(int port) {
        SharedNeoLinkConfig.setLocalPort(port);
    }

    public static void setHookPort(int port) {
        SharedNeoLinkConfig.setHookPort(port);
    }

    public static void setHostConnectPort(int port) {
        SharedNeoLinkConfig.setHostConnectPort(port);
    }

    public static void setPvpAllowed(boolean allowed) {
        SharedNeoLinkConfig.setPvpAllowed(allowed);
    }

    public static void setAllowCheats(boolean allow) {
        SharedNeoLinkConfig.setAllowCheats(allow);
    }

    public static void setMaxPlayers(int players) {
        SharedNeoLinkConfig.setMaxPlayers(players);
    }

    public static void setGameType(GameType gameType) {
        SharedNeoLinkConfig.setGameTypeName(gameType.name());
    }

    public static void setOnlineMode(OnlineMode mode) {
        SharedNeoLinkConfig.setOnlineMode(mode);
    }

    public static void setLocalDomain(String localDomain) {
        SharedNeoLinkConfig.setLocalDomain(localDomain);
    }
}
