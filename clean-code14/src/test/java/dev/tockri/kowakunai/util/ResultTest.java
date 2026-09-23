package dev.tockri.kowakunai.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultTest {

  @Nested
  class MapTest {
    @Test
    @DisplayName("Successの場合は値が変換される")
    void testMapSuccess() {
      // Act
      var result = new Success<Integer, String>(1).map(value -> value + 1);

      // Assert
      if (result instanceof Success<Integer, String>(Integer value)) {
        assertThat(value).isEqualTo(2);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("Failureの場合は値が変換されない")
    void testMapFailure() {
      // Act
      var result = new Failure<Integer, String>("エラー").map(value -> value + 1);

      // Assert
      if (result instanceof Failure<Integer, String>(var error)) {
        assertThat(error).isEqualTo("エラー");
      } else {
        fail();
      }
    }

    @Nested
    class FlatMapTest {
      @Test
      @DisplayName("Successの場合は次のResultが返される")
      void testFlatMapSuccess() {
        // Act
        var result = new Success<Integer, String>(1).flatMap(value -> new Success<>(value + 1));

        // Assert
        if (result instanceof Success<Integer, String>(Integer value)) {
          assertThat(value).isEqualTo(2);
        } else {
          fail();
        }
      }

      @Test
      @DisplayName("Failureの場合は次の処理が実行されずエラーが維持される")
      void testFlatMapFailure() {
        // Act
        var result = new Failure<Integer, String>("エラー").flatMap(value -> new Success<>(value + 1));

        // Assert
        if (result instanceof Failure<Integer, String>(var error)) {
          assertThat(error).isEqualTo("エラー");
        } else {
          fail();
        }
      }
    }
  }
}
