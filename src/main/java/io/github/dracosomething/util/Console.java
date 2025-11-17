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
    private File log = null;
    private Map<String, Consumer<Console>> scheduled = new HashMap<>();

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

    public Console(File exe, String commandMod) {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        builder.command(exe.getPath(), commandMod);
        command = builder.command();
        INDEX.getAndIncrement();
    }

    public void directory(File dir) {
        this.directory = dir;
        this.builder.directory(dir);
    }

    public File directory() {
        return this.builder.directory();
    }

    public void log(File log) {
        this.log = log;
    }

    public void runCommand(String command) {
        if (isActive) {
            LOGGER.info("Adding command to que.");
            que.add(command);
            return;
        }
        this.currentCommand = command;
        ArrayList<String> list = new ArrayList<>(this.command);
        list.add(command);
        LOGGER.info("Constructed command.\nCommand: " + Arrays.toString(list.toArray()));
        LOGGER.info("Executing in directory: " + directory);
        this.builder.command(list);
        this.builder.inheritIO();
        if (this.log != null) {
            LOGGER.info("Redirected console output to log file.");
            this.builder.redirectOutput(this.log);
        }
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
        this.scheduled.put(next, consumer);
    }

    public void schedule(String command, Consumer<Console> consumer) {
      this.scheduled.put(command, consumer);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.currentActive.destroy();
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
            if (this.scheduled.containsKey(this.currentCommand)) {
                Consumer<Console> consumer = this.scheduled.get(this.currentCommand);
                if (consumer != null) {
                    consumer.accept(this);
                }
            }
            String command = que.getFirst();
            que.removeFirst();
            this.builder.directory(this.directory);
            this.runCommand(command);
        }
    }
}
