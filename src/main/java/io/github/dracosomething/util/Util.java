package io.github.dracosomething.util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Util {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    public static final boolean IS_64_BIT;

    public static boolean firstLaunch() {
        File php = new File(FileUtils.PROJECT, FileUtils.PATH_SEPARATOR + "php");
        File apache = new File(FileUtils.PROJECT, FileUtils.PATH_SEPARATOR + "apache");
        File mySql = new File(FileUtils.PROJECT, FileUtils.PATH_SEPARATOR + "sql");
        return !FileUtils.PROJECT.exists() || !php.exists() || !apache.exists() || !mySql.exists();
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
            if (!isRegex) {
                builder.append("\\b");
            }
            builder.append(str);
            if (!isRegex) {
                builder.append("\\b");
            }
            builder.append(".*");

        }
        return builder.toString();
    }

    public static void staticWait(int time, TimeUnit unit) throws InterruptedException {
        long waitingTime = unit.toMillis(time);
        Thread.sleep(waitingTime);
    }


    public static Integer[] parseStringArray(String[] array) {
        Integer[] retVal = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            String str = array[i];
            int integer = Integer.parseInt(str);
            retVal[i] = integer;
        }
        return retVal;
    }

    public static String replaceOther(String regex, String replacement, String in) {
        String toRemove = in.replaceAll(regex, "toSplitAtHere");
        String[] remove = toRemove.split("toSplitAtHere");
        for (String str : remove) {
            in = in.replaceFirst(str, replacement);
        }
        return in;
    }

    static {
        if (IS_WINDOWS) {
            IS_64_BIT = System.getenv("ProgramFiles(x86)") != null;
        } else {
            IS_64_BIT = System.getProperty("os.arch").contains("64");
        }
    }
}
