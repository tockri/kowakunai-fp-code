package dev.tockri.kowakunai.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public sealed interface Result<T> permits Success, Failure {
  default <S> Result<S> then(Function<T, Result<S>> func) {
    return switch (this) {
      case Success<T>(var value) -> func.apply(value);
      case Failure<T> failure -> failure.cast();
    };
  }

  default <S> Result<S> map(Function<T, S> func) {
    return switch (this) {
      case Success<T>(var value) -> {
        try {
          yield new Success<>(func.apply(value));
        } catch (Exception e) {
          yield new Failure<>(List.of(e.getMessage()));
        }
      }
      case Failure<T> failure -> failure.cast();
    };
  }

  static <S, T> Result<List<T>> collect(Iterable<S> items, Function<S, Result<T>> func) {
    var results = new ArrayList<T>();
    var errors = new ArrayList<String>();

    for (var item : items) {
      switch (func.apply(item)) {
        case Success<T>(var value) -> results.add(value);
        case Failure<T>(var es) -> errors.addAll(es);
      }
    }

    return errors.isEmpty() ? new Success<>(results) : new Failure<>(errors);
  }
}
