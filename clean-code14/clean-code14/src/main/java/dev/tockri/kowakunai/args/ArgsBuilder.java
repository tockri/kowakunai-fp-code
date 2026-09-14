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
      var setResult =
          parseArg(schema, itr.next())
              .flatMap(
                  (argInfo) -> {
                    var argType = argInfo.argType;
                    var suppl = argType.needsValue ? itr.next() : null;
                    return setValue(args, argInfo.key, argType, suppl);
                  });
      if (setResult instanceof Failure<Void, ArgsError> f) {
        return f.cast();
      }
    }
    return new Success<>(args);
  }

  private record ArgInfo(String key, ArgType argType) {}

  static Result<ArgInfo, ArgsError> parseArg(Schema schema, String arg) {
    if (!arg.startsWith("-")) {
      return new Failure<>(new ArgsError("Invalid argument: " + arg));
    }

    var key = arg.substring(1);
    var argType = schema.get(key);
    if (argType == null) {
      return new Failure<>(new ArgsError("Unexpected key: " + key));
    }
    return new Success<>(new ArgInfo(key, argType));
  }

  static Result<Void, ArgsError> setValue(
      ArgsImpl args, String key, ArgType argType, String suppl) {
    switch (argType) {
      case BOOL -> args.setTrue(key);
      case INT -> {
        var valid = validateInt(suppl);
        if (valid instanceof Success<Integer, ArgsError>(var value)) {
          args.setInt(key, value);
        } else if (valid instanceof Failure<Integer, ArgsError> failure) {
          return failure.cast();
        }
      }
      case STRING -> {
        if (suppl == null) {
          return new Failure<>(new ArgsError("Supplied value is null"));
        }
        args.setString(key, suppl);
      }
    }
    return new Success<>(null);
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
