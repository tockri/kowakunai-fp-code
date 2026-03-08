package dev.tockri.kowakunai.util;

public record Success<T>(T value) implements Result<T> {
}
