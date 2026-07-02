package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.NeoLinkMC;
import neoproxy.neolinkmc.config.ConfigRules;
import neoproxy.neolinkmc.config.ConnectionConfig;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import neoproxy.neolinkmc.config.OnlineMode;
import neoproxy.neolinkmc.gui.LanScreenLayout.Positions;
import neoproxy.neolinkmc.service.ConnectionService;
import neoproxy.neolinkmc.service.MessageHandler;
import neoproxy.neolinkmc.service.MinecraftMessageHandler;
import neoproxy.neolinkmc.util.ConfigFolderOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NeoLink 配置界面
 * <p>
 * 界面布局：
 * - 顶部：高级设置按钮 + 打开配置文件夹按钮
 * - 中部：内网穿透设置标题 + 游戏设置按钮网格
 * - 底部：端口输入框 + 最大玩家数输入框 + 操作按钮
 *
 * @author NeoProxy Team
 * @version 0.3.1
 */
public class NeoLinkConfigScreen extends Screen {

    // ==================== 界面文本常量 ====================

    private static final Component TITLE = Component.translatable("neolink.gui.title");
    private static final Component ADVANCED_SETTINGS = Component.translatable("neolink.gui.advanced_settings");
    private static final Component OPEN_CONFIG_FOLDER = Component.translatable("neolink.gui.open_config_folder");
    private static final Component TUNNEL_SETTINGS = Component.translatable("neolink.gui.tunnel_settings");
    private static final Component PLAYER_SETTINGS = Component.translatable("neolink.gui.player_settings");
    private static final Component START_TUNNEL = Component.translatable("neolink.gui.start_tunnel");
    private static final Component CANCEL = Component.translatable("neolink.gui.cancel");
    private static final Component PORT_LABEL = Component.translatable("neolink.gui.port");
    private static final Component MAX_PLAYERS_LABEL = Component.translatable("neolink.gui.max_players");

    // ==================== 布局常量 ====================

    // ==================== 配置状态 ====================

    private final Screen parentScreen;
    private final ConfigContainer config;

    // ==================== GUI 组件 ====================

    private Button advancedSettingsButton;
    private Button openConfigFolderButton;
    private Button gameModeButton;
    private Button allowCheatsButton;
    private Button onlineModeButton;
    private Button allowPvpButton;
    private Button startTunnelButton;
    private Button cancelButton;
    private EditBox portEditBox;
    private EditBox maxPlayersEditBox;

    // ==================== 动态计算的Y坐标（供render使用）====================

    private int titleY;
    private int subtitleY;
    private int inputLabelY;

    public NeoLinkConfigScreen(Screen parentScreen) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.config = new ConfigContainer();
        loadConfig();
    }

    /**
     * 从共享配置文件加载配置。
     */
    private void loadConfig() {
        this.config.loadFromConfig();
    }

    @Override
    protected void init() {
        super.init();

        Positions layout = LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth());

        // 保存Y坐标供render使用
        this.titleY = layout.titleY();
        this.subtitleY = layout.playerSettingsY();
        this.inputLabelY = layout.inputLabelY();

        // ==================== 第一行：顶部按钮 ====================
        this.advancedSettingsButton = addRenderableWidget(
                createButton(layout.leftColumnX(), layout.topRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        ADVANCED_SETTINGS, this::onAdvancedSettingsClick)
        );
        this.openConfigFolderButton = addRenderableWidget(
                createButton(layout.rightColumnX(), layout.topRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        OPEN_CONFIG_FOLDER, this::onOpenConfigFolderClick)
        );

        // ==================== 第二/三行：标题区域（在render中绘制）====================

        // ==================== 第四行：游戏设置按钮网格 ====================
        this.gameModeButton = addRenderableWidget(
                createButton(layout.leftColumnX(), layout.firstOptionRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        getGameModeDisplayText(), this::onGameModeClick)
        );
        this.onlineModeButton = addRenderableWidget(
                createButton(layout.rightColumnX(), layout.firstOptionRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        getOnlineModeDisplayText(), this::onOnlineModeClick)
        );

        this.allowCheatsButton = addRenderableWidget(
                createButton(layout.leftColumnX(), layout.secondOptionRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        getAllowCheatsDisplayText(), this::onAllowCheatsClick)
        );
        this.allowPvpButton = addRenderableWidget(
                createButton(layout.rightColumnX(), layout.secondOptionRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        getAllowPvpDisplayText(), this::onAllowPvpClick)
        );

        // ==================== 第五/六行：输入框标签和输入框 ====================

        this.portEditBox = addRenderableWidget(
                createFilteredEditBox(layout.leftColumnX(), layout.inputRowY(), layout.inputWidth(), layout.buttonHeight(),
                        String.valueOf(this.config.localPort),
                        this::isValidPortInput, 5)
        );
        this.maxPlayersEditBox = addRenderableWidget(
                createFilteredEditBox(layout.rightColumnX(), layout.inputRowY(), layout.inputWidth(), layout.buttonHeight(),
                        String.valueOf(this.config.maxPlayers),
                        this::isValidMaxPlayersInput, 3)
        );

        // ==================== 第七行：底部按钮 ====================
        this.startTunnelButton = addRenderableWidget(
                createButton(layout.leftColumnX(), layout.bottomRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        START_TUNNEL, this::onStartTunnelClick)
        );
        this.cancelButton = addRenderableWidget(
                createButton(layout.rightColumnX(), layout.bottomRowY(), layout.buttonWidth(), layout.buttonHeight(),
                        CANCEL, this::onCancelClick)
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染背景和组件（super.render 会处理背景模糊和半透明遮罩）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 然后在背景之上绘制自定义文本
        Positions layout = LanScreenLayout.calculate(this.width, this.height, preferredButtonTextWidth());

        // 渲染标题 - 使用init中动态计算的Y坐标
        drawCenteredString(guiGraphics, TUNNEL_SETTINGS, layout.centerX(), this.titleY, 0xFFFFFFFF);

        // 渲染副标题 - 使用init中动态计算的Y坐标，颜色使用白色更明显
        drawCenteredString(guiGraphics, PLAYER_SETTINGS, layout.centerX(), this.subtitleY, 0xFFFFFFFF);

        // 渲染输入框标签 - 使用init中动态计算的Y坐标
        drawString(guiGraphics, PORT_LABEL, layout.leftColumnX(), this.inputLabelY, 0xFFAAAAAA);
        drawString(guiGraphics, MAX_PLAYERS_LABEL, layout.rightColumnX(), this.inputLabelY, 0xFFAAAAAA);
    }

    // ==================== 组件创建辅助方法 ====================

    private Button createButton(int x, int y, int width, int height, Component message, Consumer<Button> onPress) {
        return Button.builder(message, onPress::accept)
                .pos(x, y)
                .size(width, height)
                .build();
    }

    private EditBox createEditBox(int x, int y, int width, int height, String initial) {
        EditBox editBox = new EditBox(
                this.font,
                x, y,
                width, height,
                Component.literal("")
        );
        editBox.setValue(initial);
        return editBox;
    }

    private EditBox createFilteredEditBox(int x, int y, int width, int height, String initial,
                                          Predicate<String> filter, int maxLength) {
        EditBox editBox = createEditBox(x, y, width, height, initial);
        editBox.setFilter(filter);
        editBox.setMaxLength(maxLength);
        return editBox;
    }

    // ==================== 渲染辅助方法 ====================

    private void drawCenteredString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        int textWidth = this.font.width(text);
        int drawX = x - textWidth / 2;
        guiGraphics.drawString(this.font, text, drawX, y, color, true);
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x, y, color, true);
    }

    private int preferredButtonTextWidth() {
        int maxWidth = 0;
        maxWidth = Math.max(maxWidth, this.font.width(ADVANCED_SETTINGS));
        maxWidth = Math.max(maxWidth, this.font.width(OPEN_CONFIG_FOLDER));
        maxWidth = Math.max(maxWidth, this.font.width(getGameModeDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getOnlineModeDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getAllowCheatsDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(getAllowPvpDisplayText()));
        maxWidth = Math.max(maxWidth, this.font.width(START_TUNNEL));
        maxWidth = Math.max(maxWidth, this.font.width(CANCEL));
        return maxWidth;
    }

    // ==================== 按钮文本生成 ====================

    private Component getGameModeDisplayText() {
        String modeName = switch (this.config.gameType) {
            case SURVIVAL -> Component.translatable("neolink.gui.gamemode.survival").getString();
            case CREATIVE -> Component.translatable("neolink.gui.gamemode.creative").getString();
            case ADVENTURE -> Component.translatable("neolink.gui.gamemode.adventure").getString();
            case SPECTATOR -> Component.translatable("neolink.gui.gamemode.spectator").getString();
        };
        return Component.literal("游戏模式：" + modeName);
    }

    private Component getAllowCheatsDisplayText() {
        String status = this.config.allowCheats
                ? Component.translatable("neolink.gui.status.on").getString()
                : Component.translatable("neolink.gui.status.off").getString();
        return Component.literal("允许作弊：" + status);
    }

    private Component getOnlineModeDisplayText() {
        String modeName = switch (this.config.onlineMode) {
            case ONLINE_ONLINE_UUID_ONLY -> Component.translatable("neolink.gui.online_mode.online").getString();
            case OFFLINE_TRY_ONLINE_UUID_FIRST -> Component.translatable("neolink.gui.online_mode.offline_fixed").getString();
            case OFFLINE_OFFLINE_UUID_ONLY -> Component.translatable("neolink.gui.online_mode.offline_vanilla").getString();
        };
        return Component.literal(modeName);
    }

    private Component getAllowPvpDisplayText() {
        String status = this.config.pvpAllowed
                ? Component.translatable("neolink.gui.status.on").getString()
                : Component.translatable("neolink.gui.status.off").getString();
        return Component.literal("允许 PVP：" + status);
    }

    // ==================== 输入验证 ====================

    private boolean isValidPortInput(String input) {
        return ConfigRules.isValidPortInput(input);
    }

    private boolean isValidMaxPlayersInput(String input) {
        return ConfigRules.isValidMaxPlayersInput(input);
    }

    // ==================== 事件处理 ====================

    private void onAdvancedSettingsClick(Button button) {
        Minecraft.getInstance().setScreen(new NeoLinkAdvancedSettingsScreen(this, this.config, advancedConfig -> {
            this.config.remoteServer = advancedConfig.remoteServer();
            this.config.hookPort = advancedConfig.hookPort();
            this.config.hostConnectPort = advancedConfig.hostConnectPort();
            this.config.key = advancedConfig.key();
            NeoLinkMC.LOGGER.debug("从高级设置界面接收到配置: remote={}, hookPort={}, hostConnectPort={}, key={}",
                    advancedConfig.remoteServer(), advancedConfig.hookPort(), advancedConfig.hostConnectPort(),
                    advancedConfig.key().isEmpty() ? "(空)" : "(已设置)");
        }));
    }

    private void onOpenConfigFolderClick(Button button) {
        ConfigFolderOpener.openAsync(NeoLinkConfig.getModConfigDir());
    }

    private void onGameModeClick(Button button) {
        this.config.gameType = toGameType(ConfigRules.nextGameTypeName(this.config.gameType.name()));
        this.gameModeButton.setMessage(getGameModeDisplayText());
    }

    private void onAllowCheatsClick(Button button) {
        this.config.allowCheats = !this.config.allowCheats;
        this.allowCheatsButton.setMessage(getAllowCheatsDisplayText());
    }

    private void onOnlineModeClick(Button button) {
        this.config.onlineMode = ConfigRules.nextOnlineMode(this.config.onlineMode);
        this.onlineModeButton.setMessage(getOnlineModeDisplayText());
    }

    private void onAllowPvpClick(Button button) {
        this.config.pvpAllowed = !this.config.pvpAllowed;
        this.allowPvpButton.setMessage(getAllowPvpDisplayText());
    }

    private void onStartTunnelClick(Button button) {
        try {
            startTunnelOrLogFailure();
        } catch (Exception e) {
            NeoLinkMC.LOGGER.error("启动 NeoLinkMC LAN 隧道失败", e);
            new MinecraftMessageHandler().send("启动内网穿透失败，完整异常堆栈已写入日志。", MessageHandler.MessageType.ERROR);
        }
    }

    private void startTunnelOrLogFailure() {
        // 保存共享配置；key 只用于本次连接，绝不持久化。
        saveConfig();

        // 2. 获取密钥（从高级设置界面传入的密钥，空字符串则使用 "Free"）
        String key = ConnectionConfig.normalizeKey(this.config.key);

        // 3. 根据配置的端口建议值开启 LAN
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            NeoLinkMC.LOGGER.error("无法获取单人游戏服务器实例");
            return;
        }

        // 应用游戏设置到服务器
        this.config.applyToCurrentServer(server);

        // 获取建议端口（用户输入的）
        int suggestedPort = parsePortInput();

        // 开启 LAN，传入建议端口，Minecraft 会尝试使用，如果不可用会分配其他端口
        server.publishServer(this.config.gameType, this.config.allowCheats, suggestedPort);

        // 获取实际分配的 LAN 端口
        int actualPort = server.getPort();
        NeoLinkMC.LOGGER.info("LAN 已开启，建议端口: {}, 实际端口: {}", suggestedPort, actualPort);

        ConnectionService connectionService = new ConnectionService(new MinecraftMessageHandler());
        connectionService.start(ConfigRules.toConnectionConfig(this.config.snapshotState(), key, actualPort));

        // 保存到全局服务引用
        NeoLinkMC.updateConnectionService(connectionService);

        // 返回上级界面
        goBack();
    }

    private void onCancelClick(Button button) {
        goBack();
    }

    // ==================== 辅助方法 ====================

    private int parsePortInput() {
        return ConfigRules.parsePortOrDefault(this.portEditBox.getValue(), this.config.localPort);
    }

    private int parseMaxPlayersInput() {
        return ConfigRules.parseMaxPlayersOrDefault(this.maxPlayersEditBox.getValue(), this.config.maxPlayers);
    }

    private void saveConfig() {
        this.config.localPort = parsePortInput();
        this.config.maxPlayers = parseMaxPlayersInput();
        this.config.saveConfig();
    }

    private void goBack() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void onClose() {
        goBack();
    }

    /**
     * 打开文件夹
     *
     * @param path 文件夹路径
     * @return 是否成功打开
     */
    private static GameType toGameType(String gameTypeName) {
        try {
            return GameType.valueOf(gameTypeName);
        } catch (IllegalArgumentException e) {
            return NeoLinkConfig.DEFAULT_GAME_TYPE;
        }
    }
}
