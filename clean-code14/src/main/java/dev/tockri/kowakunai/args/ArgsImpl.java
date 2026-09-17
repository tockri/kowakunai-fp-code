package dev.tockri.kowakunai.args;

import java.util.Map;
import java.util.Optional;

class ArgsImpl implements Args {
  private final Map<String, Object> valueMap;

  ArgsImpl(Map<String, Object> valueMap) {
    this.valueMap = valueMap;
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
