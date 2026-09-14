package dev.tockri.kowakunai.args;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArgvIteratorTest {

  @Nested
  @DisplayName("hasNext")
  class HasNextTest {
    @Test
    @DisplayName("要素が残っている場合はtrueを返す")
    void returnsTrueWhileElementsRemain() {
      var iterator = new ArgvIterator(new String[] {"first"});

      assertThat(iterator.hasNext()).isTrue();
    }

    @Test
    @DisplayName("空の配列の場合はfalseを返す")
    void returnsFalseForEmptyArray() {
      var iterator = new ArgvIterator(new String[] {});

      assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    @DisplayName("すべての要素を取得した後はfalseを返す")
    void returnsFalseAfterAllElementsAreConsumed() {
      var iterator = new ArgvIterator(new String[] {"first"});

      iterator.next();

      assertThat(iterator.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("next")
  class NextTest {
    @Test
    @DisplayName("引数の要素を順番に返す")
    void returnsElementsInOrder() {
      var iterator = new ArgvIterator(new String[] {"first", "second"});

      assertThat(iterator.next()).isEqualTo("first");
      assertThat(iterator.next()).isEqualTo("second");
    }
  }
}
