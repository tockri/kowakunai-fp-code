package dev.tockri.kowakunai.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultCollectorTest {
  @Nested
  @DisplayName("toMap")
  class ToMapTest {
    @Test
    @DisplayName("すべてSuccessの場合はMapに集約される")
    void testSuccess() {
      var result =
          Stream.of(
                  new Success<Integer, String>(1),
                  new Success<Integer, String>(2))
              .collect(ResultCollector.toMap(value -> "key" + value, value -> value * 10));

      if (result instanceof Success<Map<String, Integer>, String>(var values)) {
        assertThat(values).containsExactlyInAnyOrderEntriesOf(Map.of("key1", 10, "key2", 20));
      } else {
        fail("Successであるべきです");
      }
    }

    @Test
    @DisplayName("Failureが含まれる場合はFailureになる")
    void testFailure() {
      var result =
          Stream.of(
                  new Success<Integer, String>(1),
                  new Failure<Integer, String>("エラー"),
                  new Success<Integer, String>(2))
              .collect(ResultCollector.toMap(value -> "key" + value, value -> value * 10));

      if (result instanceof Failure<Map<String, Integer>, String>(var error)) {
        assertThat(error).isEqualTo("エラー");
      } else {
        fail("Failureであるべきです");
      }
    }
  }

  @Nested
  @DisplayName("toList")
  class ToListTest {
    @Test
    @DisplayName("すべてSuccessの場合はListに集約される")
    void testSuccess() {
      var result =
          Stream.of(
                  new Success<Integer, String>(1),
                  new Success<Integer, String>(2))
              .collect(ResultCollector.toList());

      if (result instanceof Success<List<Integer>, String>(var values)) {
        assertThat(values).containsExactly(1, 2);
      } else {
        fail("Successであるべきです");
      }
    }

    @Test
    @DisplayName("Failureが含まれる場合はFailureになる")
    void testFailure() {
      var result =
          Stream.of(
                  new Success<Integer, String>(1),
                  new Failure<Integer, String>("エラー"),
                  new Success<Integer, String>(2))
              .collect(ResultCollector.toList());

      if (result instanceof Failure<List<Integer>, String>(var error)) {
        assertThat(error).isEqualTo("エラー");
      } else {
        fail("Failureであるべきです");
      }
    }
  }
}
