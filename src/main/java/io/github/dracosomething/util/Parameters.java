package io.github.dracosomething.util;

import java.util.*;
import java.lang.*;

public class Parameters {
  private final Class<?>[] parameterTypes;
  private List<Object> inputParameters;
  private int cursor;

  public Parameters(Class<?>[] parameterTypes) {
    this.parameterTypes = parameterTypes;
    this.inputParameters = new ArrayList<Object>();
    this.cursor = 0;
  }

  public void accept(Collection<Object> params) {
    for (Object param : params) {
      this.accept(param);
    }
  }

  public void accept(Object param) {
    this.accept(param, cursor);
    this.cursor++;
  }

  public void accept(Object param, int index) {
    Class<?> paramType = this.parameterTypes[index];
    Class<?> paramTypeInput = param.getClass();
    if (paramType.getName().equals(paramTypeInput.getName())) {
      throw new RuntimeException("The parameter does not match the type required.");
    }

    this.inputParameters.add(index, param);
  }

  public Collection<Object> getParametersInput() {
    if (this.inputParameters.size() != this.parameterTypes.length) {
      throw new RuntimeException("Not all parameters have been put in.");
    }

    return this.inputParameters;
  }
}
