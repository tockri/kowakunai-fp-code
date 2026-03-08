package dev.tockri.kowakunai.util;

import java.util.List;

public record Failure<T>(List<String> errors) implements Result<T> {
    
}
