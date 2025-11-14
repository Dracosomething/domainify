package io.github.dracosomething.util;

import java.util.concurrent.*;
import java.util.Optional;
import java.lang.Runnable;

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

  public static void runVoidAsync(Runnable function) throws ExecutionException, InterruptedException {
    ExecutorService pool = Executors.newCachedThreadPool();
    Future<?> future = pool.submit(function);

    future.get();

    pool.close();
  }
}
