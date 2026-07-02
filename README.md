# NeoLinkMC-Forge

NeoLinkMC-Forge 是 NeoLinkMC 的 Forge 独立构建仓库，只维护 Forge loader 入口、Forge 版本矩阵和 Forge 打包链路。

## 依赖

- 通过 Maven 坐标消费 `NeoLinkMC-Common`
- 通过 `top.ceroxe.api:neolinkapi-desktop` 复用 NeoLink JVM 隧道客户端

## 本地验证

```cmd
gradlew.bat :forge:v1_21_8:jar -PuseMavenLocal=true
gradlew.bat :forge:v26_1_2:jar -PuseMavenLocal=true
```
