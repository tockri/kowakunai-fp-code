package dev.tockri.kowakunai.util;

public record Failure<T, E>(E error) implements Result<T, E> {
  public <S> Failure<S, E> cast() {
    return new Failure<>(error);
  }
}
