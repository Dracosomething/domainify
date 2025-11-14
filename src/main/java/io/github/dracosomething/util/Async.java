package io.github.dracosomething.util;

import java.util.concurrent.*;
import java.util.Optional;

public class Async {
  public static <R> R functionAsync(Function<R> func, Parameters params, Optional<Object> parent) throws InterruptedException, ExecutionException {
    ExecutorService pool = Executors.newCachedThreadPool();
    Future<R> future = pool.submit(() -> {
      return func.body(params, parent);
    });

    R returned = future.get();

    pool.close();

    return returned;
  }
}
