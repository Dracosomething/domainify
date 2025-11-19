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
    private Map<Integer, String> que = new HashMap<>();
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

    // check if index is correct.
    public void runCommandAndSchedule(String command, Consumer<Console> task) {
      this.runCommand(command);
      this.scheduled.put(que.size(), task);
    }

    public void runCommand(String command) {
        this.currentCommand = command;
        if (isActive) {
            LOGGER.info("Adding command to que.");
            que.put(que.size(), command);
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
        this.scheduled.put(0, consumer);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.currentActive.destroy();
    }

    /**
     * loop door que
     * als de index niet meer klopt(de vorige index is niet gelijk aan de huidige index-1) doorgaan
     * pak de consumer en de command die bij de index hooren
     * doe index 1 omlaag
     *
    */
    private void updateReferences() {
        int previous = 0;
        for (Pair<Integer, String> pair : Util.mapToPairList(this.que)) {
            int index = pair.getKey();
            if (index-1 != previous) {
                Consumer<Console> consumer = this.scheduled.get(index);
                String command = this.que.get(index);
                index--;
                if (this.que.get(index) != null) {
                    this.que.put(index, command);
                }
                if (this.consumer != null && this.scheduled.get(index) != null) {
                    this.scheduled.put(index, scheduled);
                }
            }
            previous = index;
        }
//        for (Pair<Integer, Consumer<Console>> pair : Util.mapToPairList(this.scheduled)) {
//            int key = pair.getKey();
//            this.scheduled.remove(key);
//            if (key-1 < 0) continue;
//            this.scheduled.put(key-1, pair.getValue());
//        }
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
            String command = que.get(0);
            que.remove(0);
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
