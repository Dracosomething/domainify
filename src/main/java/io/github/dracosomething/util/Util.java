package io.github.dracosomething.util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class Util {
    public static final String PATH_SEPARATOR = System.getProperty("file.separator");
    public static final File ROOT = Arrays.stream(File.listRoots()).toList().getFirst();
    public static final File PROJECT = new File(ROOT, PATH_SEPARATOR + "domainify");
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

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
        StringBuilder builder = new StringBuilder(".*\\b");
        Iterator<String> it = Arrays.stream(arr).iterator();
        while (it.hasNext()) {
            String str = it.next();
            builder.append(str).append("\\b.*");
            if (it.hasNext()) {
                builder.append("\\b");
            }
        }
        return builder.toString();
    }
}
