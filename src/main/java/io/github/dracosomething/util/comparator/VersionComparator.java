package io.github.dracosomething.util.comparator;

import io.github.dracosomething.util.Util;

import java.util.Comparator;
import java.util.regex.Pattern;

public abstract class VersionComparator<T> implements Comparator<T> {
//    private static final Integer DEFAULT_COMPARISON = 0;
    private static final Pattern NUMBER_REGEX = Pattern.compile("(?<=[-_.\\\\/\\s\\na-zA-Z]|^)(\\.?[0-9]+)+(?=[-_.\\\\/\\s\\na-zA-Z]|$)");
    protected final String fileName;
    protected final String fileExtension;

    protected VersionComparator(String fileName, String fileExtension) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    public abstract String format(T object);

    @Override
    public int compare(T o1, T o2) {
        String version1 = Util.replaceOther(NUMBER_REGEX.toString(), "",
                format(o1).replace(fileName, "").replace(fileExtension, ""));
        String version2 = Util.replaceOther(NUMBER_REGEX.toString(), "",
                format(o2).replace(fileName, "").replace(fileExtension, ""));
        String[] versions1 = version1.split("\\.");
        String[] versions2 = version2.split("\\.");
        versions1 = Util.removeNullFromArray(versions1);
        versions2 = Util.removeNullFromArray(versions2);
        return compareNextNumbers(Util.parseStringArray(versions1), Util.parseStringArray(versions2));
    }

    private int compareNextNumbers(Integer[] versions1, Integer[] versions2) {
        if (versions1.length == 1 || versions2.length == 1)
            return versions2[0].compareTo(versions1[0]);

        if (versions2[0].compareTo(versions1[0]) == 0) {
            int length1 = versions1.length;
            int length2 = versions2.length;
            if (length1 == 2 || length2 == 2) {
                return versions2[1].compareTo(versions1[1]);
            }

            for (int i = 1; i < length1; i++) {
                if (versions2[i].compareTo(versions1[i]) == 0) {
                    continue;
                }
                return versions2[i].compareTo(versions1[i]);
            }
        } else {
            return versions2[0].compareTo(versions1[0]);
        }
        return versions2[0].compareTo(versions1[0]);
    }
}
