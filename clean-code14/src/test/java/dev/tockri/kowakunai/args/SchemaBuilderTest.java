package dev.tockri.kowakunai.args;

import static org.assertj.core.api.Assertions.assertThat;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SchemaBuilderTest {

  @Nested
  class BuildTest {
    @Test
    @DisplayName("正常に生成できる")
    void successful() {
      // Act
      var actual = SchemaBuilder.build("e,b*,num#");

      // Assert
      assertThat(actual.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("書式が間違っている場合エラーになる")
    void failure() {
      // Act
      var actual = SchemaBuilder.build("e,b*,num#,s b");

      // Assert
      assertThat(actual.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("空の要素がある場合はエラー内容を返す")
    void failureForEmptyElement() {
      var actual = SchemaBuilder.build("");

      assertThat(actual).isInstanceOf(Failure.class);
      if (actual instanceof Failure<Schema, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid schema expression: ");
      }
    }

    @Test
    @DisplayName("キーに予約記号が含まれる場合はエラー内容を返す")
    void failureForReservedCharacterInKey() {
      var actual = SchemaBuilder.build("na#me");

      assertThat(actual).isInstanceOf(Failure.class);
      if (actual instanceof Failure<Schema, ArgsError>(ArgsError error)) {
        assertThat(error.message()).isEqualTo("Invalid schema expression: na#me");
      }
    }
  }

  @Nested
  class GetTest {
    @Test
    @DisplayName("定義文字列通りに返す")
    void successful() {
      // Arrange
      var result = SchemaBuilder.build("e,b*,num#");

      // Act & Assert
      assertThat(result.isSuccess()).isTrue();
      if (result instanceof Success<Schema, ArgsError>(Schema schema)) {
        assertThat(schema.get("e")).isEqualTo(new SchemaEntry("e", ArgType.BOOL));
        assertThat(schema.get("b")).isEqualTo(new SchemaEntry("b", ArgType.STRING));
        assertThat(schema.get("num")).isEqualTo(new SchemaEntry("num", ArgType.INT));
        assertThat(schema.get("a")).isEqualTo(null);
      }
    }
  }
}
