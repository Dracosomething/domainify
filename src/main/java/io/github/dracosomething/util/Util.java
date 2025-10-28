package io.github.dracosomething.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.github.dracosomething.Main.LOGGER;

public class Util {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    public static final LinuxVersion LINUX_VERSION;
    public static final boolean IS_64_BIT;

    public static String formatArrayToRegex(String[] arr) {
        StringBuilder builder = new StringBuilder(".*");
        Iterator<String> it = Arrays.stream(arr).iterator();

        LOGGER.info("Iterating through array arr: " + Arrays.toString(arr));
        while (it.hasNext()) {
            String str = it.next();
            LOGGER.info("Current string: " + str);
            boolean isRegex = str.startsWith("$!");
            if (isRegex) {
                LOGGER.info("String is regex. Removing \"$!\" from string.");
                str = str.replaceFirst("[$]!", "");
            }
            if (!isRegex) {
                LOGGER.info("String is not regex. Appending \"\\b\" to StringBuilder.");
                builder.append("\\b");
            }
            builder.append(str);
            if (!isRegex) {
                LOGGER.info("Appending \"\\b\" at the end of non regex string.");
                builder.append("\\b");
            }
            builder.append(".*");
        }
        return builder.toString();
    }

    public static void staticWait(int time, TimeUnit unit) {
        long waitingTime = unit.toMillis(time);
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            LOGGER.error("staticWait method got interupted when trying to sleep thread.", e);
        }
    }

    public static Integer[] parseStringArray(String[] array) {
        Integer[] retVal = new Integer[array.length];

        for (int i = 0; i < array.length; i++) {
            String str = array[i];
            if (str.isBlank() || !isStringNumeric(str))
                continue;

            int integer = Integer.parseInt(str);
            retVal[i] = integer;
        }

        return retVal;
    }

    public static boolean isStringNumeric(String string) {
        if (string == null || string.isBlank())
            return false;
        try {
            Integer.parseInt(string);
            Double.parseDouble(string);
            Float.parseFloat(string);
            Short.parseShort(string);
            Long.parseLong(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String replaceOther(String regex, String replacement, String in) {
        String toRemove = in.replaceAll(regex, "toSplitAtHere");
        String[] remove = toRemove.split("toSplitAtHere");
        LOGGER.info("Should remove " + Arrays.toString(remove) + " from string " + in + '.');

        for (String str : remove) {
            in = in.replaceFirst(str, replacement);
        }

        return in;
    }

    public static boolean containsIgnoreCase(String in, String contains) {
        return in.contains(contains) ||
                in.toLowerCase().contains(contains.toLowerCase()) ||
                in.toUpperCase().contains(contains.toUpperCase());
    }

    public static boolean stringContainsCharacter(String in, char character) {
        char[] arr = new char[]{character};
        String check = new String(arr);
        return in.contains(check);
    }

    public static boolean stringStartsWithCharacter(String in, char character) {
        char[] arr = new char[]{character};
        String check = new String(arr);
        return in.startsWith(check);
    }

    public static boolean stringEndsWithCharacter(String in, char character) {
        char[] arr = new char[]{character};
        String check = new String(arr);
        return in.endsWith(check);
    }

    public static <K, V> List<Pair<K, V>> mapToPairList(Map<K, V> map) {
        List<Pair<K, V>> retVal = new ArrayList<>();

        map.forEach((key, value) -> {
            retVal.add(new Pair<>(key, value));
        });

        return retVal;
    }

    public static boolean isValidURL(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            return code < 400;
        } catch (IOException e) {
            LOGGER.error("Encountered IOException.", e);
            return false;
        }
    }

    public static boolean isValidURL(String url) {
        try {
            URL url1 = URI.create(url).toURL();
            return isValidURL(url1);
        } catch (IOException e) {
            LOGGER.error("Encountered IOException.", e);
            return false;
        }
    }

    public static String shortenClassPath(String path, int maxStringSize) {
        StringBuilder retVal = new StringBuilder();
        String[] names = path.split("\\.");
        ImprovedIterator<String> iterator = new ImprovedIterator<>(names);

        while (iterator.hasNext()) {
            String name = iterator.next();
            if (iterator.peek() != null) {
                StringBuilder builder = new StringBuilder();
                char[] characters = name.toCharArray();

                if (characters.length <= maxStringSize) {
                    builder.append(characters);
                    continue;
                }

                for (int i = 0; i < maxStringSize; i++) {
                    char character = characters[i];
                    builder.append(character);
                }
                name = builder.toString();
            }
            retVal.append(name).append(".");
        }

        return retVal.toString();
    }

    public static Method getClassMethodNoParams(Class<?> clazz, String name) throws NoSuchMethodException {
        return getClassMethod(clazz, name, null);
    }

    public static Method getClassMethod(Class<?> clazz, String name, Class<?>... params) throws NoSuchMethodException {
        return clazz.getMethod(name, params);
    }

    static {
        if (IS_WINDOWS) {
            IS_64_BIT = System.getenv("ProgramFiles(x86)") != null;
        } else {
            IS_64_BIT = System.getProperty("os.arch").contains("64");
        }
        if (SystemUtils.IS_OS_LINUX) {
            String osName = System.getProperty("os.name");
            if (containsIgnoreCase(osName, "debian"))
                LINUX_VERSION = LinuxVersion.DEBIAN;
            else if (containsIgnoreCase(osName, "fedora"))
                LINUX_VERSION = LinuxVersion.FEDORA;
            else if (containsIgnoreCase(osName, "redhat"))
                LINUX_VERSION = LinuxVersion.RED_HAT;
            else if (containsIgnoreCase(osName, "ubuntu"))
                LINUX_VERSION = LinuxVersion.UBUNTU;
            else if (containsIgnoreCase(osName, "linux"))
                LINUX_VERSION = LinuxVersion.LINUX;
            else
                LINUX_VERSION = LinuxVersion.NON;
        } else {
            LINUX_VERSION = LinuxVersion.NON;
        }
    }

    public enum LinuxVersion {
        DEBIAN,
        FEDORA,
        RED_HAT,
        UBUNTU,
        LINUX,
        NON
    }
}
