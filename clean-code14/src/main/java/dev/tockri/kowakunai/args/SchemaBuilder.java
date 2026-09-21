package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

class SchemaBuilder {
  private static final Pattern elemPattern = Pattern.compile("^([^ \t*#]+)([*#]?)$");

  /**
   * @param schemaExpr 「,」区切りの要素の連続。要素=key + suffix。 key は #,*,空白以外の文字の連続。suffixは#,*,空文字のいずれか。
   */
  static Result<Schema, ArgsError> build(String schemaExpr) {
    return Arrays.stream(schemaExpr.split(","))
        .map(SchemaBuilder::parseElem)
        .collect(ResultCollector.toMap(SchemaEntry::key, (ak) -> ak))
        .map(SchemaImpl::new);
  }

  static Result<SchemaEntry, ArgsError> parseElem(String schemaElem) {
    var matcher = elemPattern.matcher(schemaElem);
    if (matcher.matches()) {
      return new Success<>(new SchemaEntry(matcher.group(1), ArgType.fromSuffix(matcher.group(2))));
    } else {
      return new Failure<>(new ArgsError("Invalid schema expression: " + schemaElem));
    }
  }

  static class SchemaImpl implements Schema {
    private final Map<String, SchemaEntry> schemaEntries;

    SchemaImpl(Map<String, SchemaEntry> schemaEntries) {
      this.schemaEntries = schemaEntries;
    }

    @Override
    public SchemaEntry get(String key) {
      return schemaEntries.get(key);
    }
  }
}
