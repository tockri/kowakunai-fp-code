package dev.tockri.kowakunai.args;

interface Schema {
  SchemaEntry get(String key);
}

record SchemaEntry(String key, ArgType<?> argType) {}
