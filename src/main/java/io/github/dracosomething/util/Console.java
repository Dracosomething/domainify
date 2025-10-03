package io.github.dracosomething.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings("deprecated")
public class Console extends Thread {
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private List<String> que = new ArrayList<>();
    private Process currentActive;
    private ProcessBuilder builder;
    private List<String> command;
    private boolean isActive = false;
    private int exitCode = -1;
    private boolean isFile = false;
    private File bat = null;
    private File log = null;
    private Consumer<Console> scheduled = null;

    public Console() {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        this.setDaemon(true);
        if (Util.IS_WINDOWS) {
            builder.command("cmd.exe", "/c");
            this.setName("cmd-" + INDEX.get());
        } else {
            builder.command("sh", "-c");
            this.setName("sh-" + INDEX.get());
        }
        command = builder.command();
        INDEX.getAndIncrement();
    }

    public Console(File bash) {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        this.isFile = true;
        this.bat = bash;
        this.setDaemon(true);
        builder.command(bash.getPath(), "/c");
        this.setName(bash.getPath() + "-" + INDEX.get());
        command = builder.command();
        INDEX.getAndIncrement();
    }

    public void directory(File dir) {
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
            que.add(command);
            return;
        }
        ArrayList<String> list = new ArrayList<>(this.command);
        list.add(command);
        this.builder.command(list);
        this.builder.inheritIO();
        if (this.log != null) {
            this.builder.redirectOutput(this.log);
        }
        try {
            if (this.isFile) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(this.bat));
                    StringBuilder strBuilder = new StringBuilder();
                    String str;
                    boolean shouldAppend = false;
                    while ((str = reader.readLine()) != null) {
                        if (str.equals("cmd /K")) {
                            str = "cmd /c " + command;
                        } else {
                            shouldAppend = true;
                        }
                        strBuilder.append(str).append(System.lineSeparator());
                    }
                    if (!shouldAppend) {
                        strBuilder.append("::for domainify console").append(System.lineSeparator());
                        strBuilder.append("cmd /c ").append(command).append(System.lineSeparator());
                        strBuilder.append("::CLOSE DOMAINIFY SECTION");
                    }
                    reader.close();
                    FileWriter writer = new FileWriter(this.bat);
                    writer.write(strBuilder.toString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            currentActive = this.builder.start();
            isActive = true;
            this.start();
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
        if (this.isAlive()) {
            this.interrupt();
        }
        if (this.isFile) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(this.bat));
                StringBuilder strBuilder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    if (str.startsWith("::for domainify console")) {
                        while (!(str = reader.readLine()).startsWith("::CLOSE DOMAINIFY SECTION")) {
                            str = "";
                        }
                        continue;
                    }
                    strBuilder.append(str).append(System.lineSeparator());
                }
                reader.close();
                FileWriter writer = new FileWriter(this.bat);
                writer.write(strBuilder.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            exitCode = currentActive.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!currentActive.isAlive() || !this.isActive) {
            System.out.println("Finished with exit code: " + exitCode);
            exitCode = -1;
            this.isActive = false;
            this.interrupt();
            if (this.que.isEmpty()) {
                if (this.scheduled != null) {
                    this.scheduled.accept(this);
                }
            } else {
                String command = que.getLast();
                que.remove(command);
                this.runCommand(command);
            }
        }
    }
}
