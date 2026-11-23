# Murasame 2.0.1：SlashBlade 2.2.1 更新

日期：2026-11-19。

## 依赖和构建

- SlashBlade：`2.1.9-1.21.1` → `2.2.1-1.21.1`。原来的 `[2.1.9-1.21.1,2.2)` 拒绝加载 2.2.1；改为精确声明 `[2.2.1-1.21.1]`，不推断未核对版本的兼容性。
- NeoForge 构建目标：`21.1.228` → `21.1.267`；Minecraft 仍为 1.21.1，Java 仍为 21。
- Murasame：`2.0.0` → `2.0.1`，发布文件名 `murasame-1.21.1-neoforge-2.0.1.jar`。
- `build.gradle` 优先使用 `modpack_mods_dir` 指定的运行时 SlashBlade JAR；独立检出且该目录不存在时保留 Modrinth Maven 回退。

## 源码核对

依据 Journey Beyond 实际安装的 SlashBlade 2.2.1 JAR、其反编译代码及 NeoForge 21.1.267 编译制品检查。44 个生产 Java 源文件全部使用实际依赖重新编译，没有需要迁移的 Java API 调用，因此保持现有玩法代码。

| 调用位置 | 上游实现与结论 |
| --- | --- |
| `MurasameBladeHelper`、觉醒逻辑 | `SlashBladeDefinition.getBlade(HolderLookup.Provider)` 和 `BladeStateAccess.of` 仍存在；工厂仍写入刀类型、模型、纹理、斩技及特殊效果。状态修改通过物品数据组件完成，不需要迁回旧 Capability。 |
| `ComboStateRegistry`、`SpatialSlash` | 连段 Builder、时间轴、眩晕和 `AttackManager.doSlash` 调用仍兼容。上游时间轴保留多段动作组合处理；超时时间仍是动画长度 / 速度 + 额外 timeout，当前约 2133 ms，覆盖第 40 tick 的恢复动作。 |
| `MurasameBlessing` | `SpecialEffect.isEffective(effect, user)` 仍按实际玩家等级判断；更新、命中、斩击事件及伤害/暴击访问器继续支持当前实现。 |
| `EmbeddedBladeStoneRenderer` | 模型加载、普通/发光绘制接口仍存在；上游绘制包含事件派发和 finally 状态清理。当前调用不绕过这些流程，与已更新的 SlashBlade 修复模组使用同一绘制入口。 |
| 名刀 JSON、创造栏 | `named_blades` 目录及属性、渲染、附魔、创造栏字段仍与上游 Codec 一致；`carry_type: default` 仍有效。 |

未修改觉醒数值、刀灵、神社、嵌刀石领取规则及资源内容。创造栏觉醒刀与睡眠觉醒刀原有属性差异继续保留。

## 本次验证范围

- 使用 Java 21 `javac` / `jar` 完成干净目录编译和打包，依据实际运行时依赖编译；当前执行环境阻止 Gradle 客户端连接其守护进程，因此这不是一次成功的 Gradle task 执行。
- 44 个生产 Java 源文件编译通过；3 个现有 GameTest Java 源文件单独编译通过，但未执行 GameTest。
- 发布包不包含 GameTest 类、测试模板、历史嵌套源码或生成器缓存，包含许可证。
- 检查 JAR 完整性、元数据变量展开、依赖版本、14 个 JSON/pack 元数据资源和名刀引用的模型/纹理路径。
- 对比旧安装包，45 个 `assets/`、`data/` 文件内容完全保留。
- 本次未启动游戏，不能将旧版本的 17 项 GameTest 和客户端实机结果算作本次结果。仍需在新依赖环境中验证进世界、两把刀显示、祝福、觉醒与空间斩的实际表现。

工作区离线重建入口为 `Journey Beyond/scripts/Build-FixMods.py --murasame`；运行时需设置 `JAVA_HOME` 为 Java 21，并使用已有的 NeoForge 编译制品和依赖缓存。构建详情记录在本项目 `build/offline-build.json`。
