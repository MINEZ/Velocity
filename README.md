# Velocity

[![Build Status](https://img.shields.io/github/actions/workflow/status/PaperMC/Velocity/gradle.yml)](https://papermc.io/downloads/velocity)
[![Join our Discord](https://img.shields.io/discord/289587909051416579.svg?logo=discord&label=)](https://discord.gg/papermc)

A Minecraft server proxy with unparalleled server support, scalability,
and flexibility.

Velocity is licensed under the GPLv3 license.

## Goals

* A codebase that is easy to dive into and consistently follows best practices
  for Java projects as much as reasonably possible.
* High performance: handle thousands of players on one proxy.
* A new, refreshing API built from the ground up to be flexible and powerful
  whilst avoiding design mistakes and suboptimal designs from other proxies.
* First-class support for Paper, Sponge, Fabric and Forge. (Other implementations
  may work, but we make every endeavor to support these server implementations
  specifically.)
  
## Building

Velocity is built with [Gradle](https://gradle.org). We recommend using the
wrapper script (`./gradlew`) as our CI builds using it.

It is sufficient to run `./gradlew build` to run the full build cycle.

## Running

Once you've built Velocity, you can copy and run the `-all` JAR from
`proxy/build/libs`. Velocity will generate a default configuration file
and you can configure it from there.

Alternatively, you can get the proxy JAR from the [downloads](https://papermc.io/downloads/velocity)
page.

## 网易《我的世界：中国版》登录验证

本分支在上游的基础上增加了对《我的世界：中国版》玩家登录验证的支持，通过以下两个系统属性启用：

| 系统属性 | 说明 |
| --- | --- |
| `netease.sessionserver` | 网易验证服务器的完整地址。留空（默认值）时继续使用 Mojang 的正版验证。 |
| `netease.gameid` | 网易开发者平台分配的游戏 ID。 |

例如：

```shell
java -Dnetease.sessionserver=https://example.com/hasJoined -Dnetease.gameid=123456 -jar velocity.jar
```

启用后，代理端会在加密握手阶段改为向网易验证服务器提交 `hasJoined` 请求，并从响应中取出玩家的唯一 ID。
由于网易不像 Mojang 那样回传皮肤、披风等材质属性，此时下发给客户端的档案不含任何属性。

需要注意的是，该流程仅在 `velocity.toml` 中 `online-mode = true` 时才会触发。

# Localisation

Translations are handled using [Crowdin](https://papermc-io.crowdin.com/velocity).
If you want to translate a language not available on Crowdin,
you might want to ask in the [Discord](https://discord.gg/papermc) about it.
