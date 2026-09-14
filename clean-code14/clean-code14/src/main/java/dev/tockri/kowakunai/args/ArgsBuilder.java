package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;
import java.util.HashMap;
import java.util.Optional;

public class ArgsBuilder {

  static Result<Args, ArgsError> build(Schema schema, String[] argv) {
    var args = new ArgsImpl();
    var itr = new ArgvIterator(argv);
    while (itr.hasNext()) {
      var arg = itr.next();

      if (!arg.startsWith("-")) {
        return new Failure<>(new ArgsError("Invalid argument: " + arg));
      }

      var key = arg.substring(1);
      var argType = schema.get(key);
      if (argType == null) {
        return new Failure<>(new ArgsError("Unexpected key: " + key));
      }

      switch (argType) {
        case BOOL -> args.setTrue(key);
        case INT -> {
          var valid = validateInt(itr.next());
          if (valid instanceof Success<Integer, ArgsError>(var value)) {
            args.setInt(key, value);
          } else if (valid instanceof Failure<Integer, ArgsError> failure) {
            return failure.cast();
          }
        }
        case STRING -> args.setString(key, itr.next());
      }
    }
    return new Success<>(args);
  }

  static Result<Integer, ArgsError> validateInt(String arg) {
    try {
      return new Success<>(Integer.parseInt(arg));
    } catch (NumberFormatException e) {
      return new Failure<>(new ArgsError("Invalid integer: " + arg));
    }
  }

  static class ArgsImpl implements Args {
    private final HashMap<String, Boolean> booleans = new HashMap<>();
    private final HashMap<String, Integer> ints = new HashMap<>();
    private final HashMap<String, String> strings = new HashMap<>();

    void setTrue(String key) {
      booleans.put(key, true);
    }

    void setInt(String key, int value) {
      ints.put(key, value);
    }

    void setString(String key, String value) {
      strings.put(key, value);
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
}
