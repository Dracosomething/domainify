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
    private List<Pair<String, Consumer<Console>>> queue = new ArrayList<>();
    private File directory;
//    private Map<Integer, Consumer<Console>> scheduled = new HashMap<>();

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

    // check if index is correct.
    public void runCommand(String command) {
      this.runCommandAndSchedule(command, null);
    }

    public void runCommandAndSchedule(String command, Consumer<Console> task) {
        this.currentCommand = command;
        if (isActive || (currentActive != null && !currentActive.isAlive())) {
            LOGGER.info("Adding command to que.");
            queue.add(new Pair<>(command, task));
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
        Pair<String, Consumer<Console>> pair = this.queue.getFirst();
        pair.setValue(consumer);
        this.queue.addFirst(pair);
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
            this.builder.directory(this.directory);
            Pair<String, Consumer<Console>> pair = this.queue.getFirst();
            Consumer<Console> action = pair.getValue();
            if (action != null)
                action.accept(this);
            String command = pair.getKey();
            this.queue.removeFirst();
            this.runCommand(command);
        }
    }
}
