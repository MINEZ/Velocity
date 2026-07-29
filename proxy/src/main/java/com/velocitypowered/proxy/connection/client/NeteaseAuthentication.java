/*
 * Copyright (C) 2018-2023 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.connection.client;

import static com.velocitypowered.proxy.VelocityServer.GENERAL_GSON;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Configuration and protocol handling for Minecraft China Edition (NetEase) authentication.
 *
 * <p>网易的验证服务器与 Mojang 的会话服务器并不兼容：前者要求以 POST 方式提交一段 JSON 请求体，
 * 而后者使用的是带查询参数的 GET 请求。这个类把两者之间的差异集中在一处，
 * 使 {@link InitialLoginSessionHandler} 只需要判断走哪一条通道即可。</p>
 *
 * <p>相关配置通过以下两个系统属性提供，例如：</p>
 *
 * <pre>
 * java -Dnetease.sessionserver=https://example.com/hasJoined -Dnetease.gameid=123456 -jar velocity.jar
 * </pre>
 *
 * <p>其中 {@code netease.sessionserver} 留空（默认值）时，代理端会继续使用 Mojang 的正版验证。</p>
 */
final class NeteaseAuthentication {

  // 网易验证服务器的完整地址，留空表示不启用网易验证。
  private static final String SESSION_SERVER_URL = System.getProperty("netease.sessionserver", "");
  // 网易开发者平台分配的游戏 ID。
  private static final String GAME_ID = System.getProperty("netease.gameid", "");
  private static final boolean ENABLED = !SESSION_SERVER_URL.isEmpty();

  private NeteaseAuthentication() {
    throw new AssertionError("这是一个工具类，不应被实例化。");
  }

  /**
   * Returns whether the NetEase session server should be used instead of Mojang's.
   *
   * @return {@code true} if {@code netease.sessionserver} has been configured
   */
  static boolean isEnabled() {
    return ENABLED;
  }

  /**
   * Builds a {@code hasJoined} request against the NetEase session server.
   *
   * @param userAgent the User-Agent to send along with the request
   * @param username the username announced by the client in its login packet
   * @param serverId the session id derived from the shared secret and the server public key
   * @return a request ready to be sent with an {@link java.net.http.HttpClient}
   */
  static HttpRequest createHasJoinedRequest(final String userAgent, final String username,
      final String serverId) {
    final String body = GENERAL_GSON.toJson(new HasJoinedRequest(username, serverId, GAME_ID));
    return HttpRequest.newBuilder()
        .uri(URI.create(SESSION_SERVER_URL))
        .setHeader("User-Agent", userAgent)
        .setHeader("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
        .build();
  }

  /**
   * Parses a {@code hasJoined} response returned by the NetEase session server.
   *
   * <p>网易只会回传玩家的唯一 ID，不像 Mojang 那样附带皮肤、披风等材质属性，
   * 因此这里构造出的档案不含任何属性。属性列表为空时，登录成功包中写入的属性数量为 0，
   * 与不下发属性的效果一致。</p>
   *
   * @param body the response body
   * @param username the username announced by the client in its login packet
   * @return the parsed game profile, or {@code null} if the response could not be parsed
   */
  static @Nullable GameProfile parseHasJoinedResponse(final String body, final String username) {
    final HasJoinedResponse response;
    try {
      response = GENERAL_GSON.fromJson(body, HasJoinedResponse.class);
    } catch (final RuntimeException e) {
      return null;
    }

    if (response == null || response.entity == null || response.entity.id == null) {
      return null;
    }

    final UUID uniqueId = parseUniqueId(response.entity.id);
    return uniqueId == null ? null : new GameProfile(uniqueId, username, List.of());
  }

  // 兼容带连字符与不带连字符两种写法的 UUID。
  private static @Nullable UUID parseUniqueId(final String id) {
    try {
      return id.length() == 32 ? UuidUtils.fromUndashed(id) : UUID.fromString(id);
    } catch (final IllegalArgumentException e) {
      return null;
    }
  }

  // 提交给网易验证服务器的请求体，字段名即为 JSON 的键名。
  private static final class HasJoinedRequest {

    private final String username;
    private final String serverId;
    private final String gameId;

    private HasJoinedRequest(final String username, final String serverId, final String gameId) {
      this.username = username;
      this.serverId = serverId;
      this.gameId = gameId;
    }
  }

  // 网易验证服务器的响应体，形如 {"entity": {"id": "..."}}。
  private static final class HasJoinedResponse {

    private @Nullable Entity entity;

    private static final class Entity {

      private @Nullable String id;
    }
  }
}
