<div align="center">

# MINEZ Velocity

面向 MINEZ 服务器维护的 [Velocity](https://github.com/PaperMC/Velocity) Fork。

<br>

<div>
<a href="https://github.com/PaperMC/Velocity"><img src="docs/badges/upstream.svg" alt="upstream"></a>
<a href="#版本号"><img src="docs/badges/version.svg" alt="version"></a>
<a href="#受支持的版本"><img src="docs/badges/minecraft.svg" alt="Minecraft"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-GPLv3-9e9e9e" alt="license"></a>
</div>

<br>

<a href="https://docs.papermc.io/velocity/">查阅文档</a> ｜
<a href="https://github.com/MINEZ/Velocity/issues">问题反馈</a> ｜
<a href="https://github.com/MINEZ/Velocity/releases">版本发布</a> ｜
<a href="#相对上游的改动">改动一览</a>

</div>

> [!IMPORTANT]
> **本项目是非官方 Fork，与 PaperMC 团队没有关系，既未经其认可，也不由其提供支持。**
>
> 与本 Fork 有关的一切，请提交至[本仓库的 issue](https://github.com/MINEZ/Velocity/issues)，不要带到上游仓库或 PaperMC 的 Discord。
>
> 区分方法是先在未经修改的上游构建上复现：
>
> - **脱离本 Fork 仍会出现** —— 属于上游的问题，照常向上游反馈，不必也不要提及本 Fork。
> - **脱离本 Fork 便不再出现** —— 属于我们的问题，只应提交到这里。

## 关于

本仓库跟随上游 `dev/4.0.0` 分支，仅承载 MINEZ 服务器所需的少量改动。除下文列出的内容外，其余部分与上游完全一致。

### 受支持的版本

与上游一致，支持本页顶部徽章所示范围内的 Minecraft 客户端版本。运行需要 Java 25 及以上版本。

我们以合并的方式跟随上游：

```bash
git fetch upstream
git merge upstream/dev/4.0.0
```

上游每次改动版本号时，`gradle.properties` 中的 `version` 一行都会产生冲突，按[版本号](#版本号)一节的规则解决即可。

## 相对上游的改动

| 名称 | 类型 | 改动 |
| --- | --- | --- |
| `netease.sessionserver` | 系统属性 | 新增 |
| `netease.gameid` | 系统属性 | 新增 |
| 正版验证 | 行为 | 修改 |

**网易《我的世界：中国版》登录验证。** 上游只能向 Mojang 的会话服务器验证玩家身份，《我的世界：中国版》的玩家因此无法通过正版验证。现可经以下两个系统属性改为向网易的验证服务器验证：

| 系统属性 | 说明 |
| --- | --- |
| `netease.sessionserver` | 网易验证服务器的完整地址。留空（缺省）时继续使用 Mojang 的正版验证。 |
| `netease.gameid` | 网易开发者平台分配的游戏 ID。 |

```bash
java -Dnetease.sessionserver=https://example.com/hasJoined -Dnetease.gameid=123456 -jar velocity.jar
```

启用后，代理端在加密握手阶段不再向 Mojang 发出 `hasJoined` 查询，而是以 POST 方式向网易验证服务器提交一段 JSON 请求体，并从响应中取出玩家的唯一 ID。该开关作用于全部玩家，无法与 Mojang 的验证并存；离线模式下不进行正版验证，自然也不受其影响。另有两点需要留意：

- 网易不像 Mojang 那样回传皮肤、披风等材质属性，此时下发给客户端与后端服务器的档案不含任何属性。
- `prevent-client-proxy-connections` 依赖 Mojang 会话服务器对玩家 IP 的比对，网易验证下不起作用。

## 构建

本项目使用 [Gradle](https://gradle.org) 构建，请使用仓库自带的包装脚本：

```bash
./gradlew build
```

运行 Gradle 需要 JDK 17 及以上版本；编译所需的 JDK 25 若本机没有，Gradle 会自动下载。产物位于 `proxy/build/libs/`，取其中带 `-all` 后缀的 JAR，形如 `velocity-proxy-4.3.0.1-all.jar`。首次运行会生成默认的配置文件，在此基础上修改即可。

构建时还会重新生成 `docs/badges/` 下的徽章，以反映当前的版本号与支持的 Minecraft 版本范围。徽章有变化时，请随代码一同提交。

## 版本号

版本号由所基于的上游正式版本加上本 Fork 的修订号组成：在 `4.3.0.1` 中，`4.3.0` 是上游版本，末尾的 `.1` 是本 Fork 的修订号。版本号写在 `gradle.properties` 中，合并上游时：

- 上游只是发布了新的快照版本（形如 `4.3.1-SNAPSHOT`），保留我们的版本号即可；
- 上游发布了新的正式版本（形如 `4.3.1`），改为该版本并将修订号从 `1` 重新计起，即 `4.3.1.1`。

上游项目页面上的下载链接指向 PaperMC 的官方构建，**不包含**本 Fork 的改动。

## 上游

Velocity 由 [PaperMC](https://papermc.io/) 团队开发。上游的用法与配置，仍应参阅[官方文档](https://docs.papermc.io/velocity/)，或前往官方 [Discord](https://discord.gg/papermc) 咨询——但仅限于上游原本提供的内容，不包括本 Fork 的任何改动。

此处的改动为单一服务器的需求而作，独立维护。请勿将相关讨论带入上游的各个渠道。

## 许可证

Velocity 以 GNU 通用公共许可证第 3 版（GPLv3）开源，版权归 Velocity Contributors 所有，其中 API 部分以 MIT 许可证开源。本 Fork 的修改同样适用相应的许可证。完整条款见 [LICENSE](LICENSE) 与 [api/LICENSE](api/LICENSE)。
