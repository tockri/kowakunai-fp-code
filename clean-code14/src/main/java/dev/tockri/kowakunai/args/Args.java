package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Result;
import java.util.Optional;

public interface Args {
  boolean getBool(String key);

  Optional<String> getString(String key);

  Optional<Integer> getInt(String key);

  static Result<Args, ArgsError> build(String schemaExpr, String[] argv) {
    return SchemaBuilder.build(schemaExpr).flatMap(schema -> ArgsBuilder.build(schema, argv));
  }
}
