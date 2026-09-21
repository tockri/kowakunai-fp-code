package dev.tockri.kowakunai.args;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class ArgsImpl implements Args {
  private final Map<String, ArgsEntry<?>> valueMap;

  ArgsImpl(List<? extends ArgsEntry<?>> values) {
    this.valueMap = values.stream().collect(Collectors.toMap(ArgsEntry::key, (e) -> e));
  }

  public boolean getBool(String key) {
    var v = valueMap.get(key);
    return v != null && v.argType() == ArgType.BOOL;
  }

  public Optional<Integer> getInt(String key) {
    var v = valueMap.get(key);
    if (v != null && v.argType() == ArgType.INT) {
      return Optional.of((Integer) v.value());
    }
    return Optional.empty();
  }

  public Optional<String> getString(String key) {
    var v = valueMap.get(key);
    if (v != null && v.argType() == ArgType.STRING) {
      return Optional.of((String) v.value());
    }
    return Optional.empty();
  }
}

record ArgsEntry<T>(String key, ArgType<T> argType, T value) {}
