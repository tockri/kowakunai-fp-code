package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.ResultCollector;
import dev.tockri.kowakunai.util.Success;
import java.util.*;

public class ArgsBuilder {

  static Result<Args, ArgsError> build(Schema schema, String[] argv) {
    return makePairs(schema, argv)
        .flatMap(
            (argPairs) ->
                argPairs.stream()
                    .map(ArgsBuilder::parsePair)
                    .collect(ResultCollector.toMap(KeyValue::key, KeyValue::value))
                    .map(ArgsImpl::new));
  }

  record ArgPair(ArgKey argKey, String value) {}

  static Result<List<ArgPair>, ArgsError> makePairs(Schema schema, String[] argv) {
    var list = new ArrayList<ArgPair>();
    for (var i = 0; i < argv.length; i++) {
      switch (parseArg(schema, argv[i])) {
        case Success<ArgKey, ArgsError>(var argKey) -> {
          String argValue;
          if (argKey.argType().needsValue()) {
            if (i < argv.length - 1) {
              argValue = argv[++i];
            } else {
              return new Failure<>(
                  new ArgsError("Missing argument value for key: " + argKey.key()));
            }
          } else {
            argValue = null;
          }
          list.add(new ArgPair(argKey, argValue));
        }
        case Failure<ArgKey, ArgsError> f -> {
          return f.cast();
        }
      }
    }
    return new Success<>(list);
  }

  record KeyValue(String key, Object value) {}

  static Result<KeyValue, ArgsError> parsePair(ArgPair pair) {
    var argKey = pair.argKey();
    var argType = argKey.argType();
    return argType.parse(pair.value()).map((v) -> new KeyValue(argKey.key(), v));
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
}
