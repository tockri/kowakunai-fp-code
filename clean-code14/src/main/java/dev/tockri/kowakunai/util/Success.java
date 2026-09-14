package dev.tockri.kowakunai.util;

public record Success<T, E>(T value) implements Result<T, E> {}
