package io.github.dracosomething.util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Util {
    public static final File ROOT = Arrays.stream(File.listRoots()).toList().getFirst();
    public static final File PROJECT = new File(ROOT, "/domainify");

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
        File php = new File(PROJECT, "/php");
        File apache = new File(PROJECT, "/apache");
        File mySql = new File(PROJECT, "/sql");
        return !PROJECT.exists() || !php.exists() || !apache.exists() || !mySql.exists();
    }
}
