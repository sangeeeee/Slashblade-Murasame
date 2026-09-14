# Slashblade-Murasame

为 SlashBlade Resharped 添加名刀「丛雨丸」、觉醒形态、丛雨刀灵、丛雨祝福、空间斩与樱花林神社。

当前分支已移植至 **Minecraft 1.21.1 / NeoForge / Java 21**，模组版本为 **2.0.0**。

## 安装

客户端和服务器都安装以下两个模组：

- `murasame-1.21.1-neoforge-2.0.0.jar`
- [SlashBlade Resharped 2.0.7-1.21.1](https://modrinth.com/mod/slashblade-resharped/version/u02EDgAr)

已验证环境为 NeoForge **21.1.228**、Java **21**。依赖声明允许 NeoForge `[21.1.228,21.2)`、SlashBlade `[2.0.7,2.1)`；其他版本组合尚未逐一验证。无需额外安装 PlayerAnimator、JEI 等可选模组。

不要将旧 Forge 1.20.1 版本与此版本同时放入 `mods`。本次移植不包含旧 Forge 世界或旧拔刀剑 Capability 存档的自动转换；需要迁移旧世界时，请先在备份副本中验证。

## 游戏内容

- 神社生成于樱花林。可使用 `/locate structure murasame:shrine` 查找，或 `/place structure murasame:shrine` 验证结构放置。
- 默认拔刀条件：50 级经验、击杀 500 只亡灵生物、同时拥有 5 种正面效果。拔刀后授予进度，嵌刀石变为空嵌刀石，并按配置扣除经验等级。
- 名刀达到 1,000 击杀和 50,000 耀魂后，持刀睡眠并起床触发觉醒。保留原有锻造、击杀等数据，获得空间斩和丛雨祝福。
- 主手持觉醒丛雨丸时出现刀灵，默认 **R** 键切换显示。显示偏好按玩家保存，重登和死亡后保留。
- 丛雨祝福需要 50 级：提供抗性、恢复与力量；低血量时强化击退，并触发独立于其他玩家的暴击冷却。
- 创造栏中可直接获得两种刀及两种嵌刀石。为保留旧版行为，创造栏觉醒刀仍使用原 JSON 属性，睡眠觉醒仍使用原代码中的强化属性，两者未合并重平衡。

游戏规则改用服务器配置：`<世界目录>/serverconfig/murasame-server.toml`。六个配置键和默认值保持不变：`killNeeded`、`levelNeeded`、`effectsNeeded`、`gainCleanLevel`、`evKillNeeded`、`evPSNeeded`。旧 `murasame-common.toml` 不会自动迁入；需要自定义默认值时可放入 `defaultconfigs/murasame-server.toml`。

## 构建与验证

需要 Java 21，首次构建需要联网下载 Gradle、NeoForge 和 SlashBlade。Windows 使用 `gradlew.bat`，Linux/macOS 使用 `./gradlew`。

```powershell
.\gradlew.bat build
.\gradlew.bat runData
.\gradlew.bat runGameTestServer -PenableGameTests=true
```

成品位于 `build/libs/`，`-sources.jar` 是源码包，不是安装包。GameTest 源码位于 `src/gametest`，只有显式设置 `-PenableGameTests=true` 才加入编译，正常发布构建不包含测试类或测试模板。

运行开发客户端与服务器：

```powershell
.\gradlew.bat runClient
.\gradlew.bat runServer
```

客户端、服务器、GameTest 的工作目录分别为 `run/client`、`run/server`、`run/gametest`；数据生成使用 `run-data`，避免同时运行时争用日志或存档。

客户端自动检查：先运行 GameTest，将其生成的 `run/gametest/world` 复制为独立测试世界 `run/client/saves/murasame-smoke`，然后运行：

```powershell
.\gradlew.bat runClient -PenableGameTests=true -PclientSmokeTest=true
```

首次启动如出现无障碍引导，点击继续。检查会进入该测试世界，验证两把刀的 OBJ 模型、纹理、创造栏、渲染器，以及真实客户端到服务器的刀灵切换包，输出 `CLIENT_SMOKE_PASSED` 后自动关闭。只对这个可丢弃的测试世界启用该选项。

已通过的验证及剩余人工检查项见 [移植验证记录](docs/NEOFORGE_PORT.md)。

## 源码说明

历史路径 `src/main/java/cn/adwadg/murasame/events/src/` 是原仓库嵌套的旧项目副本。它保持原样，并已从源码编译及源码包输入中排除；维护与修改应使用顶层 `src/main`。

上游参考：[SlashBlade 1.21.1 源码](https://github.com/0999312/SlashBlade_Resharped/tree/1.21.1)、[NeoForge 数据组件与附件](https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/)、[NeoForge 联网接口](https://docs.neoforged.net/docs/1.21.1/networking/payload/)。
