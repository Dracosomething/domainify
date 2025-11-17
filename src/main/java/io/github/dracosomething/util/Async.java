package io.github.dracosomething.util;

import java.util.concurrent.*;
import java.lang.Runnable;

public class Async {
  public static void runVoidAsync(Runnable function) throws ExecutionException, InterruptedException {
    CompletableFuture.runAsync(function);
  }
}
