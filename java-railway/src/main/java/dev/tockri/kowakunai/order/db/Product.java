package dev.tockri.kowakunai.order.db;

import org.springframework.data.annotation.Id;

public record Product(@Id Long id, String name) {}
