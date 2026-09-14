package dev.tockri.kowakunai.args.builder;

import dev.tockri.kowakunai.args.ArgsError;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class Schema {
  private static final Pattern elemPattern = Pattern.compile("^([^ \t*#]+)([*#]?)$");
  private static final String SUFFIX_INT = "#";
  private static final String SUFFIX_STRING = "*";

  private final Map<String, ArgType> argTypes;

  private Schema(Map<String, ArgType> argTypes) {
    this.argTypes = argTypes;
  }

  /**
   * @param schemaExpr 「,」区切りの要素の連続。要素=key + suffix。 key は #,*,空白以外の文字の連続。suffixは#,*,空文字のいずれか。
   */
  static Result<Schema, ArgsError> build(String schemaExpr) {
    var elems = schemaExpr.split(",");
    var argTypes = new HashMap<String, ArgType>();
    for (String elem : elems) {
      var matcher = elemPattern.matcher(elem);
      if (matcher.matches()) {
        var argType = toArgType(matcher.group(2));
        argTypes.put(matcher.group(1), argType);
      } else {
        return new Failure<>(new ArgsError("Invalid schema expression: " + elem));
      }
    }
    return new Success<>(new Schema(argTypes));
  }

  static ArgType toArgType(String suffix) {
    return switch (suffix) {
      case SUFFIX_INT -> ArgType.INT;
      case SUFFIX_STRING -> ArgType.STRING;
      default -> ArgType.BOOL;
    };
  }

  ArgType get(String key) {
    return argTypes.get(key);
  }
}
