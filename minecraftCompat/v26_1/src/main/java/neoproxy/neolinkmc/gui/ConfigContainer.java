package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.NeoLinkConfigState;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import neoproxy.neolinkmc.config.OnlineMode;
import neoproxy.neolinkmc.util.UUIDFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 配置容器类 - 管理GUI配置状态
 * <p>
 * 核心职责：
 * 1. 存储GUI中的所有配置字段
 * 2. 从配置文件加载配置（打开GUI时）
 * 3. 保存配置到文件（点击开启时，不含key）
 * 4. 应用配置到Minecraft服务器
 * <p>
 * 注意：key永远不会被持久化存储
 */
public final class ConfigContainer {
    private final NeoLinkConfigState state = new NeoLinkConfigState();

    // 服务端连接配置
    public String key = "";
    public String remoteServer = NeoLinkConfig.DEFAULT_REMOTE_DOMAIN;
    public int localPort = NeoLinkConfig.DEFAULT_LOCAL_PORT;
    public int hookPort = NeoLinkConfig.DEFAULT_HOOK_PORT;
    public int hostConnectPort = NeoLinkConfig.DEFAULT_HOST_CONNECT_PORT;

    // 游戏设置
    public boolean pvpAllowed = NeoLinkConfig.DEFAULT_PVP_ALLOWED;
    public OnlineMode onlineMode = OnlineMode.OFFLINE_TRY_ONLINE_UUID_FIRST;
    public boolean allowCheats = NeoLinkConfig.DEFAULT_ALLOW_CHEATS;
    public int maxPlayers = NeoLinkConfig.DEFAULT_MAX_PLAYERS;
    public GameType gameType = NeoLinkConfig.DEFAULT_GAME_TYPE;

    /**
     * 从共享配置文件加载配置。
     */
    public void loadFromConfig() {
        this.state.loadPersisted();
        syncFromState();
        // 密钥永远不读取，GUI上显示为空
        this.key = "";
    }

    public NeoLinkConfigState snapshotState() {
        syncToState();
        return this.state;
    }

    private void syncFromState() {
        this.remoteServer = this.state.remoteServer;
        this.hookPort = this.state.hookPort;
        this.hostConnectPort = this.state.hostConnectPort;
        this.localPort = this.state.localPort;
        this.gameType = toGameType(this.state.gameTypeName);
        this.onlineMode = this.state.onlineMode;
        this.pvpAllowed = this.state.pvpAllowed;
        this.allowCheats = this.state.allowCheats;
        this.maxPlayers = this.state.maxPlayers;
    }

    private void syncToState() {
        this.state.remoteServer = this.remoteServer;
        this.state.hookPort = this.hookPort;
        this.state.hostConnectPort = this.hostConnectPort;
        this.state.localPort = this.localPort;
        this.state.gameTypeName = this.gameType.name();
        this.state.onlineMode = this.onlineMode;
        this.state.pvpAllowed = this.pvpAllowed;
        this.state.allowCheats = this.allowCheats;
        this.state.maxPlayers = this.maxPlayers;
    }

    private static GameType toGameType(String gameTypeName) {
        try {
            return GameType.valueOf(gameTypeName);
        } catch (IllegalArgumentException e) {
            return NeoLinkConfig.DEFAULT_GAME_TYPE;
        }
    }

    /**
     * 应用配置到当前服务器
     */
    public void applyToCurrentServer(IntegratedServer server) {
        server.setDefaultGameType(this.gameType);
        applyAllowCheats(server.getPlayerList(), this.allowCheats);
        server.setUsesAuthentication(this.onlineMode.onlineModeEnabled);
        // 26.1 使用 GameRules 设置 PvP
        server.getGameRules().set(GameRules.PVP, this.pvpAllowed, server);
        UUIDFixer.tryOnlineFirst = this.onlineMode.tryOnlineUUIDFirst;
    }

    private static void applyAllowCheats(PlayerList playerList, boolean allowCheats) {
        try {
            Method method = PlayerList.class.getMethod("setAllowCommandsForAllPlayers", boolean.class);
            method.invoke(playerList, allowCheats);
        } catch (NoSuchMethodException ignored) {
            // 较旧 Minecraft 版本未暴露该 runtime mutator。
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to apply allow-cheats setting.", e);
        }
    }

    /**
     * 保存配置到共享配置文件。
     * 注意：key不会被保存
     */
    public void saveConfig() {
        syncToState();
        this.state.savePersisted();
    }
}
