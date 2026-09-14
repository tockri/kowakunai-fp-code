package dev.tockri.kowakunai.args;

import java.util.Optional;

public interface Args {
  boolean getBool(String key);

  Optional<String> getString(String key);

  Optional<Integer> getInt(String key);
}
