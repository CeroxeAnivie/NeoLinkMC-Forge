# NeoLinkMC Forge

NeoLinkMC 是一个面向 Minecraft 客户端的 NeoLink 内网穿透 Mod，用于把单人游戏开启的局域网世界通过 NeoLink 暴露给其他玩家连接。

本仓库维护 Forge 版本。公共配置、隧道生命周期和连接规则由 `NeoLinkMC-Common` 提供，本仓库只负责 Forge 入口、资源声明、版本矩阵、Mixin 与 GUI 适配。

## Mod 声明

- Mod 名称：NeoLinkMC
- Mod ID：`neolinkmc`
- 运行侧：客户端
- 支持加载器：Forge
- 核心依赖：NeoLinkAPI Desktop、NeoLinkMC Common
- 主要用途：让房主在单人世界中打开局域网后，通过 NeoLink 隧道让外部玩家加入

## 支持版本

Forge 覆盖 `1.20`、`1.20.1`、`1.20.2`、`1.20.3`、`1.20.4`、`1.20.6`、`1.21.x` 与 `26.x` 兼容模块。

默认同步和代表性验证只包含轻量代表模块；完整矩阵需要显式启用：

```cmd
chcp 65001 >nul
gradlew.bat -Pneolinkmc.fullMatrix=true tasks
```

## 本地构建

先在 `NeoLinkMC-Common` 发布本地开发依赖：

```cmd
chcp 65001 >nul
cd /d ..\NeoLinkMC-Common
gradlew.bat publishLocalDevelopmentPublicationToLocalDevelopmentRepository
```

再回到本仓构建代表模块：

```cmd
chcp 65001 >nul
cd /d ..\NeoLinkMC-Forge
gradlew.bat -Pneolinkmc_common_repo=..\NeoLinkMC-Common\build\repos\local-development verifyRepresentativeForge
```

构建单个模块示例：

```cmd
chcp 65001 >nul
gradlew.bat -Pneolinkmc_common_repo=..\NeoLinkMC-Common\build\repos\local-development :forge:v1_20_1:jar
```
