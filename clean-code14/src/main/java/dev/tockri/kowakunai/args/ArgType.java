package dev.tockri.kowakunai.args;

import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;

sealed interface ArgType<T> permits BoolType, IntType, StrType {

  boolean needsValue();

  /**
   * @param arg needsValue == falseの場合はnullが渡される
   * @return 成功時の値はNot null。
   */
  Result<T, ArgsError> parse(String arg);

  BoolType BOOL = BoolType.INSTANCE;
  IntType INT = IntType.INSTANCE;
  StrType STRING = StrType.INSTANCE;

  static ArgType<?> fromSuffix(String suffix) {
    return switch (suffix) {
      case "#" -> INT;
      case "*" -> STRING;
      default -> BOOL;
    };
  }
}

final class BoolType implements ArgType<Boolean> {
  static final BoolType INSTANCE = new BoolType();

  private BoolType() {}

  @Override
  public Result<Boolean, ArgsError> parse(String arg) {
    return new Success<>(true);
  }

  @Override
  public boolean needsValue() {
    return false;
  }
}

final class IntType implements ArgType<Integer> {
  static final IntType INSTANCE = new IntType();

  private IntType() {}

  @Override
  public Result<Integer, ArgsError> parse(String arg) {
    try {
      return new Success<>(Integer.parseInt(arg));
    } catch (NumberFormatException e) {
      return new Failure<>(new ArgsError("Invalid integer: " + arg));
    }
  }

  @Override
  public boolean needsValue() {
    return true;
  }
}

final class StrType implements ArgType<String> {
  static final StrType INSTANCE = new StrType();

  private StrType() {}

  @Override
  public Result<String, ArgsError> parse(String arg) {
    return new Success<>(arg);
  }

  @Override
  public boolean needsValue() {
    return true;
  }
}
