package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;

public class ArgsBuilder {

  static Result<Args, ArgsError> build(Schema schema, String[] argv) {
    var args = new ArgsImpl();
    var itr = new ArgvIterator(argv);
    while (itr.hasNext()) {
      var setResult =
          parseArg(schema, itr.next()).flatMap((argInfo) -> setValue(args, argInfo, itr));

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

  static Result<Void, ArgsError> setValue(ArgsImpl args, ArgInfo argInfo, ArgvIterator itr) {
    var key = argInfo.key;
    return switch (argInfo.argType) {
      case BOOL -> args.setTrue(key);
      case INT -> args.setInt(key, itr.next());
      case STRING -> args.setString(key, itr.next());
    };
  }
}
