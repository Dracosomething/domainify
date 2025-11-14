package io.github.dracosomething.util;

import java.lang.reflect.*;
import java.util.*;
import java.lang.Object;

@FunctionalInterface
public interface Function<R> {
  R body(Parameters params, Optional<Object> parent) throws Exception;

  public static Function<Object> fromMethod(Method method,
    Optional<Object> parent) {
    return new Function<Object>() {
      @Override
      public Object body(Parameters params, Optional<Object> parent) throws InvocationTargetException, IllegalAccessException {
        Collection<Object> parameters = params.getParametersInput();
        return method.invoke(parent.get(), parameters);
      }
    };
  }
}
