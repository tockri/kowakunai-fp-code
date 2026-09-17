package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;

public class ArgsBuilder {

  static Result<Args, ArgsError> build(Schema schema, String[] argv) {
    var args = new ArgsImpl();
    var itr = new ArgvIterator(argv);
    while (itr.hasNext()) {
      var setResult = parseArg(schema, itr.next()).flatMap((argKey) -> setValue(args, argKey, itr));

      if (setResult instanceof Failure<Void, ArgsError> f) {
        return f.cast();
      }
    }
    return new Success<>(args);
  }

  static Result<ArgKey, ArgsError> parseArg(Schema schema, String arg) {
    if (!arg.startsWith("-")) {
      return new Failure<>(new ArgsError("Invalid argument: " + arg));
    }

    var key = arg.substring(1);
    var argKey = schema.get(key);
    if (argKey == null) {
      return new Failure<>(new ArgsError("Unexpected key: " + key));
    }
    return new Success<>(argKey);
  }

  static Result<Void, ArgsError> setValue(ArgsImpl args, ArgKey argKey, ArgvIterator itr) {
    var key = argKey.key();
    var argType = argKey.argType();

    String argValue;
    if (argType.needsValue()) {
      argValue = itr.next();
      if (argValue == null) {
        return new Failure<>(new ArgsError("Missing argument value for key: " + key));
      }
    } else {
      argValue = null;
    }

    return argType
        .parse(argValue)
        .map(
            (v) -> {
              args.set(key, v);
              return null;
            });
  }
}
