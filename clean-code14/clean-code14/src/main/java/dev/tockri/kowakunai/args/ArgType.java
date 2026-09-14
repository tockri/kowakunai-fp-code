package dev.tockri.kowakunai.args;

enum ArgType {
  BOOL(false),
  INT(true),
  STRING(true);

  final boolean needsValue;

  ArgType(boolean needsValue) {
    this.needsValue = needsValue;
  }
}
