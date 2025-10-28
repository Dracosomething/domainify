package io.github.dracosomething.util;

import org.apache.commons.logging.Log;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class Logger {
    private static final StackWalker WALKER;
    private final PrintStream out;
    private final String name;
    private final String callerClass;
    private final StringBuilder logs = new StringBuilder();
    private File logFile = null;

    public static Logger getLogger() {
        return getLogger(System.out, "");
    }

    public static Logger getLogger(String name) {
        return getLogger(System.out, name);
    }

    public static Logger getLogger(PrintStream out) {
        return getLogger(out, "");
    }

    public static Logger getLogger(PrintStream out, String name) {
        List<StackWalker.StackFrame> list = WALKER.walk(stream -> stream.collect(Collectors.toList()));
        StackWalker.StackFrame caller = list.getLast();
        return getLogger(out, name, caller.getDeclaringClass());
    }

    public static Logger getLogger(PrintStream out, String name, Class<?> clazz) {
        return new Logger(out, name, Util.shortenClassPath(clazz.getName(), 2));
    }

    Logger(PrintStream out, String name, String classPath) {
        this.out = out;
        this.name = name;
        this.callerClass = classPath;
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

    public void leaving(Method method, Object retVal) {
        String message = "Leaving %1$s in %2$s.\n%1$s returned %3$s.";
        message = message.formatted(method.getName(), method.getDeclaringClass().getName(), retVal);
        info(message);
    }

    public void warn(String message) {
        log(message, PrintColor.YELLOW, LogType.WARN);
    }

    public void error(String message, Exception exception) {
        String fullMessage = "%1$s\n encountered error %2$s\nstacktrace: %3$s";
        fullMessage = fullMessage.formatted(message, exception.getMessage(), Util.formatStacktrace(exception));
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

    public void logFormatted(String message, PrintColor color, Object... params) {
        message = message.formatted(params);
        this.log(message, color, LogType.INFO);
    }

    public void log(String message, PrintColor color, LogType type) {
        StringBuilder logMessage = new StringBuilder();
        String time = LocalTime.now().toString();
        String threadName = Thread.currentThread().getName();
        logMessage.append("[").append(time).append("] ");
        logMessage.append("[").append(threadName).append("/").append(type).append("] ");
        logMessage.append("[").append(callerClass).append("/").append(name).append("]: ");
        logMessage.append(message);

        logs.append(logMessage).append("\n");

        StringBuilder fullMessage = new StringBuilder(color.toString());
        fullMessage.append(logMessage);
        fullMessage.append(PrintColor.RESET);
        out.println(fullMessage);
    }

    static {
        WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
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
