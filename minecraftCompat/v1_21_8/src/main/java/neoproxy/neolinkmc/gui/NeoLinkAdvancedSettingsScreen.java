package neoproxy.neolinkmc.gui;

import neoproxy.neolinkmc.config.ConfigRules;
import neoproxy.neolinkmc.config.NeoLinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 内网穿透高级设置界面
 * <p>
 * 提供内网穿透高级配置选项：
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

        int centerX = this.width / 2;
        int startY = 60;

        // 远程服务器输入
        int row1Y = startY;
        this.remoteServerEditBox = addRenderableWidget(
                createEditBox(centerX - INPUT_WIDTH / 2, row1Y, INPUT_WIDTH + 50, BUTTON_HEIGHT,
                        NeoLinkConfig.getRemoteDomain())
        );
        this.remoteServerEditBox.setMaxLength(100);

        // Hook 端口输入
        int row2Y = row1Y + ROW_HEIGHT + PADDING;
        this.hookPortEditBox = addRenderableWidget(
                createFilteredEditBox(centerX - INPUT_WIDTH / 2, row2Y, INPUT_WIDTH + 50, BUTTON_HEIGHT,
                        String.valueOf(NeoLinkConfig.getHookPort()),
                        this::isValidPortInput, 5)
        );

        // 主机连接端口输入
        int row3Y = row2Y + ROW_HEIGHT + PADDING;
        this.hostConnectPortEditBox = addRenderableWidget(
                createFilteredEditBox(centerX - INPUT_WIDTH / 2, row3Y, INPUT_WIDTH + 50, BUTTON_HEIGHT,
                        String.valueOf(NeoLinkConfig.getHostConnectPort()),
                        this::isValidPortInput, 5)
        );

        // 密钥输入（不保存到文件）
        int row4Y = row3Y + ROW_HEIGHT + PADDING;
        String keyDisplay = ConfigRules.displayKey(currentKey);
        this.keyEditBox = addRenderableWidget(
                createEditBox(centerX - INPUT_WIDTH / 2, row4Y, INPUT_WIDTH + 50, BUTTON_HEIGHT, keyDisplay)
        );
        this.keyEditBox.setMaxLength(64);

        // 底部按钮
        int buttonY = this.height - 40;
        int buttonSpacing = 20;

        addRenderableWidget(createButton(centerX - BUTTON_WIDTH - buttonSpacing / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                CONFIRM, this::onConfirmClick));

        addRenderableWidget(createButton(centerX + buttonSpacing / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                BACK, this::onBackClick));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int labelX = centerX - INPUT_WIDTH / 2 - LABEL_WIDTH - 5;

        drawCenteredString(guiGraphics, this.title, centerX, 20, 0xFFFFFFFF);
        drawString(guiGraphics, REMOTE_SERVER_LABEL, labelX, 60 + 6, 0xFFFFFFFF);
        drawString(guiGraphics, HOOK_PORT_LABEL, labelX, 60 + ROW_HEIGHT + PADDING + 6, 0xFFFFFFFF);
        drawString(guiGraphics, HOST_CONNECT_PORT_LABEL, labelX, 60 + (ROW_HEIGHT + PADDING) * 2 + 6, 0xFFFFFFFF);
        drawString(guiGraphics, KEY_LABEL, labelX, 60 + (ROW_HEIGHT + PADDING) * 3 + 6, 0xFFFFFFFF);
        drawCenteredString(guiGraphics, KEY_HINT, centerX, 60 + (ROW_HEIGHT + PADDING) * 4 + 5, 0xFF888888);
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
        if (configCallback != null) {
            configCallback.accept(new AdvancedConfig(remoteServer, hookPort, hostConnectPort, key));
        }

        goBack();
    }

    private void onBackClick(Button button) {
        goBack();
    }

    private int parsePort(String value, int defaultValue) {
        return ConfigRules.parsePortOrDefault(value, defaultValue);
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
}
