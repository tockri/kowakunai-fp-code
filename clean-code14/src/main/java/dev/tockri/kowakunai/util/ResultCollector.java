package dev.tockri.kowakunai.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

public interface ResultCollector {
  static <T, K, U, E> Collector<Result<T, E>, ?, Result<Map<K, U>, E>> toMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collector.of(
        () -> new MutableMapResult<K, U, E>(),
        (a, t) -> {
          if (a.success) {
            switch (t) {
              case Success<T, E>(var tv) -> a.put(keyMapper.apply(tv), valueMapper.apply(tv));
              case Failure<T, E> f -> a.fail(f.error());
            }
          }
        },
        MutableMapResult::putAll,
        MutableMapResult::toResult,
        Collector.Characteristics.UNORDERED);
  }

  class MutableMapResult<K, V, E> {
    private boolean success = true;
    private final HashMap<K, V> map = new HashMap<>();
    private E error = null;

    void fail(E err) {
      error = err;
      success = false;
    }

    void put(K k, V v) {
      map.put(k, v);
    }

    MutableMapResult<K, V, E> putAll(MutableMapResult<K, V, E> other) {
      success = success && other.success;
      error = other.error;
      map.putAll(other.map);
      return this;
    }

    Result<Map<K, V>, E> toResult() {
      if (success) {
        return new Success<>(map);
      } else {
        return new Failure<>(error);
      }
    }
  }

  static <T, E> Collector<Result<T, E>, ?, Result<List<T>, E>> toList() {
    return Collector.of(
        () -> new MutableListResult<T, E>(),
        (a, t) -> {
          if (a.success) {
            switch (t) {
              case Success<T, E>(var tv) -> a.add(tv);
              case Failure<T, E> f -> a.fail(f.error());
            }
          }
        },
        MutableListResult::addAll,
        MutableListResult::toResult);
  }

  class MutableListResult<T, E> {
    private boolean success = true;
    private final List<T> list = new ArrayList<>();
    private E error = null;

    void fail(E err) {
      error = err;
      success = false;
    }

    void add(T t) {
      list.add(t);
    }

    MutableListResult<T, E> addAll(MutableListResult<T, E> other) {
      success = success && other.success;
      error = other.error;
      list.addAll(other.list);
      return this;
    }

    Result<List<T>, E> toResult() {
      if (success) {
        return new Success<>(list);
      } else {
        return new Failure<>(error);
      }
    }
  }
}
