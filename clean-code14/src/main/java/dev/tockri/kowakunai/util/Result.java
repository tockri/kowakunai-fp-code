package dev.tockri.kowakunai.util;

import java.util.function.Function;

public sealed interface Result<T, E> permits Success, Failure {
  default boolean isSuccess() {
    return this instanceof Success;
  }

  default <S> Result<S, E> then(Function<T, Result<S, E>> func) {
    return switch (this) {
      case Success<T, E>(var value) -> func.apply(value);
      case Failure<T, E> failure -> failure.cast();
    };
  }

  default <S> Result<S, E> map(Function<T, S> func) {
    return switch (this) {
      case Success<T, E>(var value) -> new Success<>(func.apply(value));
      case Failure<T, E> failure -> failure.cast();
    };
  }

  default <S> Result<S, E> flatMap(Function<T, Result<S, E>> func) {
    return switch (this) {
      case Success<T, E>(var value) -> func.apply(value);
      case Failure<T, E> failure -> failure.cast();
    };
  }
}
