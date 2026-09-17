package dev.tockri.kowakunai.args;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

class ArgsImpl implements Args {
  private final HashMap<String, Object> valueMap = new HashMap<>();

  void set(String key, Object value) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(value, "value must not be null");
    valueMap.put(key, value);
  }

  public boolean getBool(String key) {
    var v = valueMap.get(key);
    return v == Boolean.TRUE;
  }

  public Optional<Integer> getInt(String key) {
    var v = valueMap.get(key);
    if (v instanceof Integer i) {
      return Optional.of(i);
    }
    return Optional.empty();
  }

  public Optional<String> getString(String key) {
    var v = valueMap.get(key);
    if (v instanceof String s) {
      return Optional.of(s);
    }
    return Optional.empty();
  }
}
