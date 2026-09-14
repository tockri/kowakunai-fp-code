package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;
import java.util.HashMap;
import java.util.Optional;

class ArgsImpl implements Args {
  private final HashMap<String, Boolean> booleans = new HashMap<>();
  private final HashMap<String, Integer> ints = new HashMap<>();
  private final HashMap<String, String> strings = new HashMap<>();

  Result<Void, ArgsError> setTrue(String key) {
    booleans.put(key, true);
    return new Success<>(null);
  }

  Result<Void, ArgsError> setInt(String key, String value) {
    if (value == null) {
      return new Failure<>(new ArgsError("Missing value for key: " + key));
    }
    return validateInt(value)
        .map(
            (vInt) -> {
              ints.put(key, vInt);
              return null;
            });
  }

  static Result<Integer, ArgsError> validateInt(String arg) {
    try {
      return new Success<>(Integer.parseInt(arg));
    } catch (NumberFormatException e) {
      return new Failure<>(new ArgsError("Invalid integer: " + arg));
    }
  }

  Result<Void, ArgsError> setString(String key, String value) {
    if (value == null) {
      return new Failure<>(new ArgsError("Missing value for key: " + key));
    }
    strings.put(key, value);
    return new Success<>(null);
  }

  public boolean getBool(String key) {
    var v = booleans.get(key);
    if (v == null) {
      return false;
    }
    return v;
  }

  public Optional<Integer> getInt(String key) {
    var v = ints.get(key);
    if (v == null) {
      return Optional.empty();
    }
    return Optional.of(v);
  }

  public Optional<String> getString(String key) {
    var v = strings.get(key);
    if (v == null) {
      return Optional.empty();
    }
    return Optional.of(v);
  }
}
