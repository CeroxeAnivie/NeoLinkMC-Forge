package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.ConfigRules;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NeoLink 高级设置界面
 * <p>
 * 提供 NeoLink 高级配置选项：
 * - 远程服务器地址
 * - Hook 端口
 * - 主机连接端口
 * - 密钥输入（不保存到文件）
 * <p>
 * 注意：此界面不直接保存配置到文件，而是通过回调将值传回主界面，
 * 只有在主界面点击"开启内网穿透"时才统一写入共享配置文件。
 */
public class NeoLinkAdvancedSettingsScreen extends Screen {

    private static final Component TITLE = Component.translatable("neolink.gui.remote_server.title");
    private static final Component REMOTE_SERVER_LABEL = Component.translatable("neolink.gui.remote_server.address");
    private static final Component HOOK_PORT_LABEL = Component.translatable("neolink.gui.remote_server.hook_port");
    private static final Component HOST_CONNECT_PORT_LABEL = Component.translatable("neolink.gui.remote_server.host_connect_port");
    private static final Component KEY_LABEL = Component.literal("密钥");
    private static final Component CONFIRM = Component.translatable("neolink.gui.confirm");
    private static final Component BACK = Component.translatable("neolink.gui.back");
    private static final Component KEY_HINT = Component.literal("密钥仅用于本次连接，不会保存到配置文件");

    // ==================== 布局常量 ====================

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 150;
    private static final int LABEL_WIDTH = 120;
    private static final int PADDING = 10;
    private static final int ROW_HEIGHT = 25;

    private EditBox remoteServerEditBox;
    private EditBox hookPortEditBox;
    private EditBox hostConnectPortEditBox;
    private EditBox keyEditBox;

    private final Screen parentScreen;
    private final Consumer<AdvancedConfig> configCallback;
    private final String currentKey;

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    /**
     * 高级配置数据类
     */
    public record AdvancedConfig(String remoteServer, int hookPort, int hostConnectPort, String key) {
    }

    public NeoLinkAdvancedSettingsScreen(Screen parentScreen, ConfigContainer config, Consumer<AdvancedConfig> configCallback) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.configCallback = configCallback;
        this.currentKey = config.key;
    }

    @Override
    protected void init() {
        super.init();

        // 使用 HeaderAndFooterLayout 布局
        this.layout.addToHeader(new StringWidget(this.title, this.font));

        // 创建内容区域
        GridLayout gridLayout = new GridLayout();
        gridLayout.columnSpacing(8).rowSpacing(4);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        int centerX = this.width / 2;
        int startY = 60;

        // 远程服务器输入
        rowHelper.addChild(new StringWidget(REMOTE_SERVER_LABEL, this.font));
        this.remoteServerEditBox = createEditBox(INPUT_WIDTH + 50, BUTTON_HEIGHT, NeoLinkConfig.getRemoteDomain());
        this.remoteServerEditBox.setMaxLength(100);
        rowHelper.addChild(this.remoteServerEditBox);

        // Hook 端口输入
        rowHelper.addChild(new StringWidget(HOOK_PORT_LABEL, this.font));
        this.hookPortEditBox = createFilteredEditBox(INPUT_WIDTH + 50, BUTTON_HEIGHT,
                String.valueOf(NeoLinkConfig.getHookPort()), this::isValidPortInput, 5);
        rowHelper.addChild(this.hookPortEditBox);

        // 主机连接端口输入
        rowHelper.addChild(new StringWidget(HOST_CONNECT_PORT_LABEL, this.font));
        this.hostConnectPortEditBox = createFilteredEditBox(INPUT_WIDTH + 50, BUTTON_HEIGHT,
                String.valueOf(NeoLinkConfig.getHostConnectPort()), this::isValidPortInput, 5);
        rowHelper.addChild(this.hostConnectPortEditBox);

        // 密钥输入（不保存到文件）
        rowHelper.addChild(new StringWidget(KEY_LABEL, this.font));
        String keyDisplay = ConfigRules.displayKey(currentKey);
        this.keyEditBox = createEditBox(INPUT_WIDTH + 50, BUTTON_HEIGHT, keyDisplay);
        this.keyEditBox.setMaxLength(64);
        rowHelper.addChild(this.keyEditBox);

        // 密钥提示
        rowHelper.addChild(new StringWidget(KEY_HINT, this.font), 2);

        this.layout.addToContents(gridLayout);

        // 底部按钮
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CONFIRM, this::onConfirmClick).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(BACK, this::onBackClick).width(BUTTON_WIDTH).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    // ==================== 组件创建辅助方法 ====================

    private EditBox createEditBox(int width, int height, String initial) {
        EditBox editBox = new EditBox(
                this.font,
                0, 0,
                width, height,
                Component.literal("")
        );
        editBox.setValue(initial);
        return editBox;
    }

    private EditBox createFilteredEditBox(int width, int height, String initial,
                                          Predicate<String> filter, int maxLength) {
        EditBox editBox = createEditBox(width, height, initial);
        // 26.1 移除了 setFilter，使用 setResponder 验证
        editBox.setResponder((value) -> {
            if (!filter.test(value)) {
                // 如果输入无效，恢复上一个有效值
                String validValue = value.isEmpty() ? "" : value.replaceAll("[^0-9]", "");
                if (!validValue.equals(value)) {
                    editBox.setValue(validValue);
                }
            }
        });
        editBox.setMaxLength(maxLength);
        return editBox;
    }

    // ==================== 事件处理 ====================

    private boolean isValidPortInput(String input) {
        return ConfigRules.isValidPortInput(input);
    }

    private void onConfirmClick(Button button) {
        // 解析配置值
        String remoteServer = this.remoteServerEditBox.getValue().trim();
        if (remoteServer.isEmpty()) {
            remoteServer = NeoLinkConfig.getRemoteDomain();
        }

        int hookPort = parsePort(this.hookPortEditBox.getValue(), NeoLinkConfig.getHookPort());
        int hostConnectPort = parsePort(this.hostConnectPortEditBox.getValue(), NeoLinkConfig.getHostConnectPort());

        String key = ConfigRules.transientKeyFromDisplay(this.keyEditBox.getValue());

        // 通过回调传回主界面（不立即保存到文件）
        configCallback.accept(new AdvancedConfig(remoteServer, hookPort, hostConnectPort, key));

        // 返回主界面
        Minecraft.getInstance().setScreenAndShow(parentScreen);
    }

    private void onBackClick(Button button) {
        Minecraft.getInstance().setScreenAndShow(parentScreen);
    }

    private int parsePort(String value, int defaultValue) {
        return ConfigRules.parsePortOrDefault(value, defaultValue);
    }
}
