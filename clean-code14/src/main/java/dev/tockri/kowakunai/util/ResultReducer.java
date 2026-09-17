package dev.tockri.kowakunai.util;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public interface ResultReducer {
  static <T, E, R> BiFunction<Result<R, E>, Result<T, E>, Result<R, E>> accumulator(
      BiFunction<R, T, R> accumulator) {
    return (accR, elemR) ->
        accR.flatMap((acc) -> elemR.map((elem) -> accumulator.apply(acc, elem)));
  }

  static <R, E> BinaryOperator<Result<R, E>> combiner(BinaryOperator<R> combiner) {
    return (r1R, r2R) -> r1R.flatMap((r1) -> r2R.map((r2) -> combiner.apply(r1, r2)));
  }
}
