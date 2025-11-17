package io.github.dracosomething.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


import static io.github.dracosomething.Main.LOGGER;

@SuppressWarnings("deprecated")
public class Console {
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private boolean isActive = false;
    private int exitCode = -1;
    private String currentCommand = null;
    private ProcessBuilder builder;
    private Process currentActive;
    private List<String> command;
    private List<String> que = new ArrayList<>();
    private File directory;
    private Map<Integer, Consumer<Console>> scheduled = new HashMap<>();

    public Console() {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        if (Util.IS_WINDOWS) {
            builder.command("cmd.exe", "/c");
        } else {
            builder.command("sh", "-c");
        }
        command = builder.command();
        INDEX.getAndIncrement();
    }

    public void directory(File dir) {
        this.directory = dir;
        this.builder.directory(dir);
    }

    public void runCommandAndSchedule(String command, Consumer<Console> task) {
      int size = que.size()+1;
      this.scheduled.put(size, task);
      this.runCommand(command);
    }

    public void runCommand(String command) {
        this.currentCommand = command;
        if (isActive) {
            LOGGER.info("Adding command to que.");
            que.add(command);
            return;
        }
        ArrayList<String> list = new ArrayList<>(this.command);
        list.add(command);
        LOGGER.info("Constructed command.\nCommand: " + Arrays.toString(list.toArray()));
        LOGGER.info("Executing in directory: " + directory);
        this.builder.command(list);
        this.builder.inheritIO();
        try {
            currentActive = this.builder.start();
            isActive = true;
            Async.runVoidAsync(this::executeCommands);
        } catch (IOException | ExecutionException | InterruptedException e) {
            LOGGER.error("Encountered error when trying to activate console", e);
        } finally {
            this.builder = new ProcessBuilder(this.command);
        }
    }

    public void schedule(Consumer<Console> consumer) {
        String next = que.getFirst();
        int index = this.que.indexOf(next);
        this.scheduled.put(0, consumer);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.currentActive.destroy();
    }

    private void updateReferences() {
        for (Pair<Integer, Consumer<Console>> pair : Util.mapToPairList(this.scheduled)) {
            int key = pair.getKey();
            this.scheduled.remove(key);
            if (key-1 < 0) continue;
            this.scheduled.put(key-1, pair.getValue());
        }
    }

    private void executeCommands() {
        try {
            exitCode = currentActive.waitFor(150, TimeUnit.SECONDS) ? currentActive.exitValue() : 0;
        } catch (InterruptedException e) {
            LOGGER.error("Encountered error when running command", e);
        }
        if (!currentActive.isAlive() || !this.isActive) {
            LOGGER.info("Finished with exit code: " + exitCode);
            exitCode = -1;
            this.isActive = false;
            String command = que.getFirst();
            que.removeFirst();
            updateReferences();
            this.builder.directory(this.directory);
            if (this.scheduled.containsKey(0)) {
                Consumer<Console> consumer = this.scheduled.get(0);
                if (consumer != null) {
                    consumer.accept(this);
                }
            }
            this.runCommand(command);
        }
    }
}
