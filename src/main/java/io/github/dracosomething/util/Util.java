package io.github.dracosomething.util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Util {
    public static final String PATH_SEPARATOR = System.getProperty("file.separator");
    public static final File ROOT = Arrays.stream(File.listRoots()).toList().getFirst();
    public static final File PROJECT = new File(ROOT, PATH_SEPARATOR + "domainify");
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    public static final boolean IS_64_BIT;

    public static boolean isProperPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean firstLaunch() {
        File php = new File(PROJECT, PATH_SEPARATOR + "php");
        File apache = new File(PROJECT, PATH_SEPARATOR + "apache");
        File mySql = new File(PROJECT, PATH_SEPARATOR + "sql");
        return !PROJECT.exists() || !php.exists() || !apache.exists() || !mySql.exists();
    }

    public static String formatArrayToRegex(String[] arr) {
        StringBuilder builder = new StringBuilder(".*");
        Iterator<String> it = Arrays.stream(arr).iterator();
        while (it.hasNext()) {
            String str = it.next();
            boolean isRegex = str.startsWith("$!");
            if (isRegex) {
                str = str.replaceFirst("[$]!", "");
            }
            //append \\b here if not isRegex
            builder.append(str);
            if (!isRegex) {
                builder.append("\\b");
            }
            builder.append(".*");
            // bellow can be removed
            if (it.hasNext()) {
                builder.append("\\b");
            }
        }
        return builder.toString();
    }

    public static void wait(int time, TimeUnit unit) throws InterruptedException {
        long waitingTime = unit.toMillis(time);
        Thread.sleep(waitingTime);
    }

    static {
        if (IS_WINDOWS) {
            IS_64_BIT = System.getenv("ProgramFiles(x86)") != null;
        } else {
            IS_64_BIT = System.getProperty("os.arch").contains("64");
        }
    }
}
