package io.github.dracosomething.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.github.dracosomething.Main.LOGGER;

@SuppressWarnings("deprecated")
public class Console {
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private boolean isActive = false;
    private int exitCode = -1;
    private ProcessBuilder builder;
    private Process currentActive;
    private List<String> command;
    private List<String> que = new ArrayList<>();
    private File directory;
    private File log = null;
    private Consumer<Console> scheduled = null;

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
            executeCommands();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.builder = new ProcessBuilder(this.command);
        }
    }

    public void schedule(Consumer<Console> consumer) {
        this.scheduled = consumer;
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
            if (this.que.isEmpty()) {
                if (this.scheduled != null) {
                    this.scheduled.accept(this);
                }
            } else {
                String command = que.getFirst();
                que.removeFirst();
                this.builder.directory(this.directory);
                this.runCommand(command);
            }
        }
    }
}
