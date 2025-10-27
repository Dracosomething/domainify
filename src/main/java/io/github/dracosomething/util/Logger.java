package io.github.dracosomething.util;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;

public class Logger {
    private static final StackWalker WALKER;
    private final PrintStream out;
    private final String name;
    private final String callerClass;
    private final StringBuilder logs = new StringBuilder();
    private File logFile = null;

    public Logger(PrintStream out, String name) {
        this.out = out;
        this.name = name;
        this.callerClass = Util.shortenClassPath(WALKER.getCallerClass().getName(), 2);
    }

    public Logger(String name) {
        this(System.out, name);
    }

    public Logger(PrintStream out) {
        this(out, "");
    }

    public Logger() {
        this(System.out, "");
    }

    public void setLogFile(File log) {
        this.logFile = log;
        Runnable onClose = new Runnable() {
            @Override
            public void run() {
                File logFile = Logger.this.logFile;
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
                    writer.append(Logger.this.logs);
                    writer.close();
                    Logger.this.info("Wrote logs to log file.");
                } catch (IOException e) {
                    Logger.this.error("Encountered error when trying to construct BufferedWriter.", e);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(onClose));
    }

    public void info(String message) {
        log(message, PrintColor.WHITE, LogType.INFO);
    }

    public void entering(Method method) {
        String message = "Entering %1$s in %2$s.";
        message = message.formatted(method.getName(), method.getDeclaringClass().getName());
        info(message);
    }

    public void leaving(Method method) {
        String message = "Leaving %1$s in %2$s.";
        message = message.formatted(method.getName(), method.getDeclaringClass().getName());
        info(message);
    }

    public void warn(String message) {
        log(message, PrintColor.YELLOW, LogType.WARN);
    }

    public void error(String message, Exception exception) {
        String fullMessage = "%1$s: encountered error %2$s\nstacktrace: %3$s";
        fullMessage = fullMessage.formatted(message, exception.getMessage(), Arrays.toString(exception.getStackTrace()));
        log(fullMessage, PrintColor.RED, LogType.ERROR);
    }

    public void success(String message, String taskName) {
        String fullMessage = "Succesfully executed method %1$s with message: %2$s";
        fullMessage = fullMessage.formatted(taskName, message);
        log(fullMessage, PrintColor.GREEN, LogType.SUCCESS);
    }

    public void success(String message, Method method) {
        success(message, method.getName());
    }

    public void success(String message) {
        log(message, PrintColor.GREEN, LogType.SUCCESS);
    }

    public void important(String message) {
        log(message, PrintColor.PURPLE, LogType.IMPORTANT);
    }


    public void log(String message, PrintColor color, LogType type) {
        StringBuilder fullMessage = new StringBuilder(color.code);
        String time = LocalTime.now().toString();
        String threadName = Thread.currentThread().getName();
        fullMessage.append("[").append(time).append("] ");
        fullMessage.append("[").append(threadName).append("/").append(type).append("] ");
        fullMessage.append("[").append(callerClass).append("/").append(name).append("]: ");
        fullMessage.append(message);
        fullMessage.append(PrintColor.RESET);

        out.println(fullMessage);
        logs.append(fullMessage).append(System.lineSeparator());
    }

    static {
        Set<StackWalker.Option> options = Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_HIDDEN_FRAMES);
        WALKER = StackWalker.getInstance(options);
    }

    public static enum PrintColor {
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),
        BLACK_BACKGROUND("\u001B[40m"),
        RED_BACKGROUND("\u001B[41m"),
        GREEN_BACKGROUND("\u001B[42m"),
        YELLOW_BACKGROUND("\u001B[43m"),
        BLUE_BACKGROUND("\u001B[44m"),
        PURPLE_BACKGROUND("\u001B[45m"),
        CYAN_BACKGROUND("\u001B[46m"),
        WHITE_BACKGROUND("\u001B[47m"),
        RESET("\u001B[0m");
        private final String code;

        PrintColor(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public static enum LogType {
        INFO,
        WARN,
        ERROR,
        SUCCESS,
        IMPORTANT;

        @Override
        public String toString() {
            return this.name();
        }
    }
}
