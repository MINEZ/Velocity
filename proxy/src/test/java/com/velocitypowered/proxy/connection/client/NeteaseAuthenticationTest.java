/*
 * Copyright (C) 2026 Velocity Contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.connection.client.NeteaseAuthentication.Authenticated;
import com.velocitypowered.proxy.connection.client.NeteaseAuthentication.Malformed;
import com.velocitypowered.proxy.connection.client.NeteaseAuthentication.Rejected;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NeteaseAuthenticationTest {

  private static final UUID UNIQUE_ID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

  @ParameterizedTest
  @ValueSource(strings = {
      "069a79f444e94726a5befca90e38aaf5",
      "069a79f4-44e9-4726-a5be-fca90e38aaf5"
  })
  void parsesBothUniqueIdForms(final String id) {
    final GameProfile profile = authenticate("{\"entity\":{\"id\":\"" + id + "\"}}");

    assertEquals(UNIQUE_ID, profile.getId());
    // 网易不回传材质属性，名称取自客户端在登录包中提交的用户名。
    assertEquals("Steve", profile.getName());
    assertTrue(profile.getProperties().isEmpty());
  }

  @Test
  void ignoresUnknownFields() {
    final GameProfile profile = authenticate(
        "{\"code\":0,\"entity\":{\"id\":\"069a79f444e94726a5befca90e38aaf5\",\"name\":\"x\"}}");

    assertEquals(UNIQUE_ID, profile.getId());
  }

  @Test
  void reportsRejection() {
    // 网易验证不通过时的实际响应。
    final Rejected rejected = assertInstanceOf(Rejected.class,
        NeteaseAuthentication.parseHasJoinedResponse(
            "{\"code\":7,\"message\":\"认证失败\",\"details\":\"\",\"entity\":null}", "Steve"));

    assertEquals(7, rejected.code());
    assertEquals("认证失败", rejected.message());
  }

  @Test
  void reportsRejectionWithoutMessage() {
    final Rejected rejected = assertInstanceOf(Rejected.class,
        NeteaseAuthentication.parseHasJoinedResponse("{\"code\":7}", "Steve"));

    assertEquals("", rejected.message());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "null",
      "not json",
      "[]",
      "{}",
      "{\"entity\":null}",
      "{\"entity\":{}}",
      "{\"entity\":{\"id\":\"\"}}",
      "{\"entity\":{\"id\":\"not-a-uuid\"}}",
      "{\"entity\":{\"id\":\"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz\"}}",
      "{\"code\":0,\"entity\":null}"
  })
  void rejectsMalformedResponses(final String body) {
    assertInstanceOf(Malformed.class, NeteaseAuthentication.parseHasJoinedResponse(body, "Steve"));
  }

  private static GameProfile authenticate(final String body) {
    return assertInstanceOf(Authenticated.class,
        NeteaseAuthentication.parseHasJoinedResponse(body, "Steve")).profile();
  }
}
