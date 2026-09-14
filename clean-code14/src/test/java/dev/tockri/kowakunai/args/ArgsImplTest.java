package dev.tockri.kowakunai.args;

import static org.assertj.core.api.Assertions.assertThat;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArgsImplTest {

  private static ArgsImpl createArgsImpl() {
    var args = new ArgsImpl();
    args.setTrue("verbose");
    args.setInt("count", "3");
    args.setString("name", "Alice");
    return args;
  }

  @Nested
  @DisplayName("setTrue")
  class SetTrueTest {
    @Test
    @DisplayName("真の値を設定できる")
    void setsTrueValue() {
      var args = new ArgsImpl();

      var result = args.setTrue("verbose");

      assertThat(result).isEqualTo(new Success<Void, ArgsError>(null));
      assertThat(args.getBool("verbose")).isTrue();
    }
  }

  @Nested
  @DisplayName("setInt")
  class SetIntTest {
    @Test
    @DisplayName("整数値を設定できる")
    void setsIntegerValue() {
      var args = new ArgsImpl();

      var result = args.setInt("count", "3");

      assertThat(result).isEqualTo(new Success<Void, ArgsError>(null));
      assertThat(args.getInt("count")).contains(3);
    }

    @Test
    @DisplayName("整数でない値の場合はエラーを返す")
    void returnsFailureForInvalidIntegerValue() {
      var args = new ArgsImpl();

      var result = args.setInt("count", "three");

      assertThat(result).isInstanceOf(Failure.class);
      assertThat(args.getInt("count")).isEmpty();
    }

    @Test
    @DisplayName("値がない場合はエラーを返す")
    void returnsFailureForMissingValue() {
      var args = new ArgsImpl();

      var result = args.setInt("count", null);

      assertThat(result).isInstanceOf(Failure.class);
      assertThat(args.getInt("count")).isEmpty();
    }
  }

  @Nested
  @DisplayName("setString")
  class SetStringTest {
    @Test
    @DisplayName("文字列値を設定できる")
    void setsStringValue() {
      var args = new ArgsImpl();

      var result = args.setString("name", "Alice");

      assertThat(result).isEqualTo(new Success<Void, ArgsError>(null));
      assertThat(args.getString("name")).contains("Alice");
    }

    @Test
    @DisplayName("値がない場合はエラーを返す")
    void returnsFailureForMissingValue() {
      var args = new ArgsImpl();

      var result = args.setString("name", null);

      assertThat(result).isInstanceOf(Failure.class);
      assertThat(args.getString("name")).isEmpty();
    }
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
