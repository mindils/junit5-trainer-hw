package com.dmdev.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PropertiesUtilTest {

  @ParameterizedTest
  @MethodSource("getPropertyArguments")
  void getGet(String key, String expectValue) {
    String actualValue = PropertiesUtil.get(key);

    assertEquals(expectValue, actualValue);

  }

  static Stream<Arguments> getPropertyArguments() {
    return Stream.of(
        Arguments.of("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
        Arguments.of("db.user", "sa"),
        Arguments.of("db.password", "")
    );
  }

}