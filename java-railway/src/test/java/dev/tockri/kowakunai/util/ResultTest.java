package dev.tockri.kowakunai.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultTest {
  @Nested
  @DisplayName("then")
  class ThenTest {
    @Test
    @DisplayName("Successの場合は次の処理が実行される")
    void testThenSuccess() {
      // Act
      var result = new Success<>(1).then(value -> new Success<>(value + 1));

      // Assert
      if (result instanceof Success<Integer>(Integer value)) {
        assertThat(value).isEqualTo(2);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("Failureの場合は次の処理が実行されない")
    void testThenFailure() {
      // Act
      var result = new Failure<Integer>(List.of("エラー")).then(value -> new Success<>(value + 1));

      // Assert
      if (result instanceof Failure<Integer>(List<String> errors)) {
        assertThat(errors).containsExactly("エラー");
      } else {
        fail();
      }
    }
  }

  @Nested
  class MapTest {
    @Test
    @DisplayName("Successの場合は値が変換される")
    void testMapSuccess() {
      // Act
      var result = new Success<>(1).map(value -> value + 1);

      // Assert
      if (result instanceof Success<Integer>(Integer value)) {
        assertThat(value).isEqualTo(2);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("Failureの場合は値が変換されない")
    void testMapFailure() {
      // Act
      var result = new Failure<Integer>(List.of("エラー")).map(value -> value + 1);

      // Assert
      if (result instanceof Failure<Integer>(List<String> errors)) {
        assertThat(errors).containsExactly("エラー");
      } else {
        fail();
      }
    }
  }

  @Nested
  class CollectTest {
    @Test
    @DisplayName("全てSuccessの場合は成功のリストが返される")
    void testCollectAllSuccess() {
      // Act
      var result = Result.collect(List.of(1, 2, 3), value -> new Success<>(value * 2));

      // Assert
      if (result instanceof Success<List<Integer>>(List<Integer> values)) {
        assertThat(values).containsExactly(2, 4, 6);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("一部Failureの場合はエラーのリストが返される")
    void testCollectSomeFailure() {
      // Act
      var result =
          Result.collect(
              List.of(1, 2, 3),
              value -> {
                if (value % 2 == 0) {
                  return new Success<>(value * 2);
                } else {
                  return new Failure<>(List.of("奇数は処理できません"));
                }
              });

      // Assert
      if (result instanceof Failure<List<Integer>>(List<String> errors)) {
        assertThat(errors).containsExactly("奇数は処理できません", "奇数は処理できません");
      } else {
        fail();
      }
    }
  }
}
