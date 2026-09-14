package dev.tockri.kowakunai.args.builder;

import dev.tockri.kowakunai.args.Args;
import dev.tockri.kowakunai.args.ArgsError;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgsBuilderTest {

  @Nested
  @DisplayName("build")
  class BuildTest {
    @Test
    @DisplayName("スキーマに従って各種の引数を生成できる")
    void buildsArgs() {
      var result =
        ArgsBuilder.build(
          "verbose,name*,count#", new String[]{"-verbose", "-name", "Alice", "-count", "42"});

      assertThat(result).isInstanceOf(Success.class);
      if (result instanceof Success<Args, ArgsError>(Args args)) {
        assertThat(args.getBool("verbose")).isTrue();
        assertThat(args.getString("name")).contains("Alice");
        assertThat(args.getInt("count")).contains(42);
      }
    }

    @Test
    @DisplayName("引数がない場合は空のArgsを生成する")
    void buildsEmptyArgs() {
      var result = ArgsBuilder.build("verbose,name*,count#", new String[]{});

      assertThat(result).isInstanceOf(Success.class);
      if (result instanceof Success<Args, ArgsError>(Args args)) {
        assertThat(args.getBool("verbose")).isFalse();
        assertThat(args.getString("name")).isEmpty();
        assertThat(args.getInt("count")).isEmpty();
      }
    }

    @Test
    @DisplayName("整数引数が不正な場合はエラーを返す")
    void returnsFailureForInvalidIntegerArgument() {
      var result = ArgsBuilder.build("count#", new String[]{"-count", "abc"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid integer: abc");
      }
    }

    @Test
    @DisplayName("オプション記号のない引数がある場合はエラーを返す")
    void returnsFailureForUnexpectedArgument() {
      var result = ArgsBuilder.build("verbose", new String[]{"unexpected"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid argument: unexpected");
      }
    }

    @Test
    @DisplayName("スキーマにないキーがある場合はエラーを返す")
    void returnsFailureForUnexpectedKey() {
      var result = ArgsBuilder.build("verbose", new String[]{"-quiet"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Unexpected key: quiet");
      }
    }
  }

  @Nested
  @DisplayName("validateInt")
  class ValidateIntTest {
    @Test
    @DisplayName("整数文字列をIntegerに変換する")
    void convertsIntegerString() {
      var result = ArgsBuilder.validateInt("123");

      assertThat(result).isEqualTo(new Success<Integer, ArgsError>(123));
    }

    @Test
    @DisplayName("整数でない文字列の場合はエラーを返す")
    void returnsFailureForInvalidInteger() {
      var result = ArgsBuilder.validateInt("abc");

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Integer, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid integer: abc");
      }
    }
  }

  @Nested
  @DisplayName("ArgsImpl")
  class ArgsImplTest {
    private static final ArgsBuilder.ArgsImpl args = new ArgsBuilder.ArgsImpl();

    @BeforeAll
    static void setup() {
      args.setTrue("verbose");
      args.setInt("count", 3);
      args.setString("name", "Alice");
    }

    @Nested
    @DisplayName("getBool")
    class GetBoolTest {
      @Test
      @DisplayName("設定した値を取得できる")
      void returnsConfiguredValue() {
        // Act & Assert
        assertThat(args.getBool("verbose")).isTrue();
      }

      @Test
      @DisplayName("設定していない値はfalseを返す")
      void returnsFalseForUnconfiguredValue() {
        assertThat(args.getBool("quiet")).isFalse();
      }
    }

    @Nested
    @DisplayName("getInt")
    class GetIntTest {
      @Test
      @DisplayName("設定した値を取得できる")
      void returnsConfiguredValue() {
        // Act & Assert
        assertThat(args.getInt("count")).contains(3);
      }

      @Test
      @DisplayName("設定していない値は空のOptionalを返す")
      void returnsEmptyForUnconfiguredValue() {
        assertThat(args.getInt("limit")).isEmpty();
      }
    }

    @Nested
    @DisplayName("getString")
    class GetStringTest {
      @Test
      @DisplayName("設定した値を取得できる")
      void returnsConfiguredValue() {
        // Act & Assert
        assertThat(args.getString("name")).contains("Alice");
      }

      @Test
      @DisplayName("設定していない値は空のOptionalを返す")
      void returnsEmptyForUnconfiguredValue() {
        assertThat(args.getString("nickname")).isEmpty();
      }
    }
  }
}
