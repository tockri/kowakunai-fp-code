package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.ResultCollector;
import dev.tockri.kowakunai.util.Success;
import java.util.*;

public class ArgsBuilder {

  static Result<Args, ArgsError> build(Schema schema, String[] argv) {
    return makePreEntries(schema, argv)
        .flatMap(
            (entries) ->
                entries.stream().map(PreEntry::parseValue).collect(ResultCollector.toList()))
        .map(ArgsImpl::new);
  }

  record PreEntry<T>(String key, ArgType<T> argType, String value) {
    Result<ArgsEntry<T>, ArgsError> parseValue() {
      return argType.parse(value).map((v) -> new ArgsEntry<>(key, argType, v));
    }
  }

  static Result<List<PreEntry<?>>, ArgsError> makePreEntries(Schema schema, String[] argv) {
    var list = new ArrayList<PreEntry<?>>();
    for (var i = 0; i < argv.length; i++) {
      switch (parseKey(schema, argv[i])) {
        case Success<SchemaEntry, ArgsError>(SchemaEntry(var key, var argType)) -> {
          String argValue;
          if (argType.needsValue()) {
            if (i < argv.length - 1) {
              argValue = argv[++i];
            } else {
              return new Failure<>(new ArgsError("Missing argument value for key: " + key));
            }
          } else {
            argValue = null;
          }
          list.add(new PreEntry<>(key, argType, argValue));
        }
        case Failure<SchemaEntry, ArgsError> f -> {
          return f.cast();
        }
      }
    }
    return new Success<>(list);
  }

  static Result<SchemaEntry, ArgsError> parseKey(Schema schema, String arg) {
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
}
