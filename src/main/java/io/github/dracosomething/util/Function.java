package io.github.dracosomething.util;

import java.util.FunctionalInterface;

@FunctionalInterface
public interface Function<R> {
  R run(Map<Class<?>, Object> params);
}
