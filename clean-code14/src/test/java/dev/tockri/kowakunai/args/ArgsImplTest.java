package dev.tockri.kowakunai.args;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArgsImplTest {

  private static ArgsImpl createArgsImpl() {
    return new ArgsImpl(
        new ArrayList<>() {
          {
            add(new ArgsEntry<>("verbose", ArgType.BOOL, true));
            add(new ArgsEntry<>("count", ArgType.INT, 3));
            add(new ArgsEntry<>("name", ArgType.STRING, "Alice"));
          }
        });
  }

  @Nested
  @DisplayName("getBool")
  class GetBoolTest {
    @Test
    @DisplayName("設定した値を取得できる")
    void returnsConfiguredValue() {
      // Arrange
      var args = createArgsImpl();

      // Act & Assert
      assertThat(args.getBool("verbose")).isTrue();
    }

    @Test
    @DisplayName("設定していない値はfalseを返す")
    void returnsFalseForUnconfiguredValue() {
      // Arrange
      var args = createArgsImpl();
      assertThat(args.getBool("quiet")).isFalse();
    }
  }

  @Nested
  @DisplayName("getInt")
  class GetIntTest {
    @Test
    @DisplayName("設定した値を取得できる")
    void returnsConfiguredValue() {
      // Arrange
      var args = createArgsImpl();
      // Act & Assert
      assertThat(args.getInt("count")).contains(3);
    }

    @Test
    @DisplayName("設定していない値は空のOptionalを返す")
    void returnsEmptyForUnconfiguredValue() {
      // Arrange
      var args = createArgsImpl();
      assertThat(args.getInt("limit")).isEmpty();
    }
  }

  @Nested
  @DisplayName("getString")
  class GetStringTest {
    @Test
    @DisplayName("設定した値を取得できる")
    void returnsConfiguredValue() {
      // Arrange
      var args = createArgsImpl();
      // Act & Assert
      assertThat(args.getString("name")).contains("Alice");
    }

    @Test
    @DisplayName("設定していない値は空のOptionalを返す")
    void returnsEmptyForUnconfiguredValue() {
      // Arrange
      var args = createArgsImpl();
      assertThat(args.getString("nickname")).isEmpty();
    }
  }
}
