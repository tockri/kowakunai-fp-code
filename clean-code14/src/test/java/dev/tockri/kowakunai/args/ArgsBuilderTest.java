package dev.tockri.kowakunai.args;

import static org.assertj.core.api.Assertions.assertThat;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArgsBuilderTest {

  static Schema createTestSchema(String boolKey, String stringKey, String intKey) {
    return key ->
        key.equals(boolKey)
            ? new SchemaEntry(key, ArgType.BOOL)
            : key.equals(stringKey)
                ? new SchemaEntry(key, ArgType.STRING)
                : key.equals(intKey) ? new SchemaEntry(key, ArgType.INT) : null;
  }

  @Nested
  @DisplayName("build")
  class BuildTest {
    @Test
    @DisplayName("スキーマに従って各種の引数を生成できる")
    void buildsArgs() {
      var schema = createTestSchema("verbose", "name", "count");
      var result =
          ArgsBuilder.build(schema, new String[] {"-verbose", "-name", "Alice", "-count", "42"});

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
      var schema = createTestSchema("verbose", "name", "count");
      var result = ArgsBuilder.build(schema, new String[] {});

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
      var schema = createTestSchema(null, null, "count");
      var result = ArgsBuilder.build(schema, new String[] {"-count", "abc"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid integer: abc");
      }
    }

    @Test
    @DisplayName("値を必要とする引数に値がない場合はエラーを返す")
    void returnsFailureWhenArgumentValueIsMissing() {
      var schema = createTestSchema(null, null, "count");
      var result = ArgsBuilder.build(schema, new String[] {"-count"});

      assertThat(result).isInstanceOf(Failure.class);
    }

    @Test
    @DisplayName("オプション記号のない引数がある場合はエラーを返す")
    void returnsFailureForUnexpectedArgument() {
      var schema = createTestSchema("verbose", null, null);
      var result = ArgsBuilder.build(schema, new String[] {"unexpected"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid argument: unexpected");
      }
    }

    @Test
    @DisplayName("スキーマにないキーがある場合はエラーを返す")
    void returnsFailureForUnexpectedKey() {
      var schema = createTestSchema("verbose", null, null);
      var result = ArgsBuilder.build(schema, new String[] {"-quiet"});

      assertThat(result).isInstanceOf(Failure.class);
      if (result instanceof Failure<Args, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Unexpected key: quiet");
      }
    }

    @Nested
    @DisplayName("makePairs")
    class MakePairsTest {
      @Test
      @DisplayName("スキーマに従ってキーと値の組を生成できる")
      void makesPairs() {
        var schema = createTestSchema("verbose", "name", "count");

        var result =
            ArgsBuilder.makePreEntries(
                schema, new String[] {"-verbose", "-name", "Alice", "-count", "42"});

        assertThat(result)
            .isEqualTo(
                new Success<>(
                    List.of(
                        new ArgsBuilder.PreEntry<>("verbose", ArgType.BOOL, null),
                        new ArgsBuilder.PreEntry<>("name", ArgType.STRING, "Alice"),
                        new ArgsBuilder.PreEntry<>("count", ArgType.INT, "42"))));
      }

      @Test
      @DisplayName("値を必要とする引数の値がない場合はエラーを返す")
      void returnsFailureForMissingValue() {
        var schema = createTestSchema(null, null, "count");

        var result = ArgsBuilder.makePreEntries(schema, new String[] {"-count"});

        assertThat(result)
            .isEqualTo(new Failure<>(new ArgsError("Missing argument value for key: count")));
      }
    }

    @Nested
    @DisplayName("ArgPair")
    class PreEntryTest {
      @Test
      @DisplayName("値を対応する型に変換してキーと値を返す")
      void parsesValue() {
        var pair = new ArgsBuilder.PreEntry<>("count", ArgType.INT, "42");

        var result = pair.parseValue();

        assertThat(result).isEqualTo(new Success<>(new ArgsEntry<>("count", ArgType.INT, 42)));
      }

      @Test
      @DisplayName("値を変換できない場合はエラーを返す")
      void returnsFailureForInvalidValue() {
        var pair = new ArgsBuilder.PreEntry<>("count", ArgType.INT, "abc");

        var result = pair.parseValue();

        assertThat(result).isEqualTo(new Failure<>(new ArgsError("Invalid integer: abc")));
      }
    }

    @Nested
    @DisplayName("parseArg")
    class ParseArgTest {
      @Test
      @DisplayName("オプション記号を除いたキーのスキーマ定義を返す")
      void returnsArgKey() {
        var schema = createTestSchema("verbose", null, null);

        var result = ArgsBuilder.parseKey(schema, "-verbose");

        assertThat(result).isEqualTo(new Success<>(new SchemaEntry("verbose", ArgType.BOOL)));
      }

      @Test
      @DisplayName("オプション記号がない場合はエラーを返す")
      void returnsFailureForMissingOptionPrefix() {
        var result = ArgsBuilder.parseKey(createTestSchema("verbose", null, null), "verbose");

        assertThat(result).isEqualTo(new Failure<>(new ArgsError("Invalid argument: verbose")));
      }

      @Test
      @DisplayName("スキーマにないキーの場合はエラーを返す")
      void returnsFailureForUnknownKey() {
        var result = ArgsBuilder.parseKey(createTestSchema("verbose", null, null), "-quiet");

        assertThat(result).isEqualTo(new Failure<>(new ArgsError("Unexpected key: quiet")));
      }
    }
  }
}
