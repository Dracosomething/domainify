package io.github.dracosomething.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class Console {
    private ProcessBuilder builder;
    private List<String> command;

    public Console() {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        if (Util.IS_WINDOWS) {
            builder.command("cmd.exe", "/c");
        } else {
            builder.command("sh", "-c");
        }
        command = builder.command();
        builder.redirectErrorStream(true);
    }

    public Console(File bash) {
        this.builder = new ProcessBuilder();
        this.command = new ArrayList<>();
        builder.command(bash.getPath(), "/c");
        command = builder.command();
        builder.redirectErrorStream(true);
    }

    public void directory(File dir) {
        this.builder.directory(dir);
    }

    public File directory() {
        return this.builder.directory();
    }

    public void runCommand(String command) {
        ArrayList<String> list = new ArrayList<>(this.command);
        list.add(command);
        this.builder.command(list);
        try {
            ExecutorService service = new ThreadPoolExecutor();
            Process process = this.builder.start();
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            reader.lines().forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.builder = new ProcessBuilder(this.command);
        }
    }
}
