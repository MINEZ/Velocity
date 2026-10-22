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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.util.GameProfile;
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
    final GameProfile profile = NeteaseAuthentication.parseHasJoinedResponse(
        "{\"entity\":{\"id\":\"" + id + "\"}}", "Steve");

    assertNotNull(profile);
    assertEquals(UNIQUE_ID, profile.getId());
    // 网易不回传材质属性，名称取自客户端在登录包中提交的用户名。
    assertEquals("Steve", profile.getName());
    assertTrue(profile.getProperties().isEmpty());
  }

  @Test
  void ignoresUnknownFields() {
    final GameProfile profile = NeteaseAuthentication.parseHasJoinedResponse(
        "{\"code\":0,\"entity\":{\"id\":\"069a79f444e94726a5befca90e38aaf5\",\"name\":\"x\"}}",
        "Steve");

    assertNotNull(profile);
    assertEquals(UNIQUE_ID, profile.getId());
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
      "{\"entity\":{\"id\":\"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz\"}}"
  })
  void rejectsMalformedResponses(final String body) {
    assertNull(NeteaseAuthentication.parseHasJoinedResponse(body, "Steve"));
  }
}
