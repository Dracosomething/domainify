package io.github.dracosomething.util.comparator;

import io.github.dracosomething.util.ImprovedIterator;
import io.github.dracosomething.util.Pair;
import io.github.dracosomething.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class VersionComparator<T> implements Comparator<T> {
    private static final int EQUAL = 0;
    private static final int LOWER = -1;
    private static final int HIGHER = 1;
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

    private int compareNextNumbers(Integer[] array1, Integer[] array2) {
        int retVal = 0;
        int lengthVersions1 = array1.length;
        int lengthVersions2 = array2.length;

        Pair<String, Integer> lowest = getLowerLength(lengthVersions1, lengthVersions2);
        int lowestLength = lowest.getValue();
        String fieldName = lowest.getKey();

        int longestLength = lengthVersions1;
        if (Objects.equals(fieldName, "lengthVersions2"))
            longestLength = lengthVersions2;

        Integer[] longerArray = array1;
        if (Objects.equals(fieldName, "lengthVersions2"))
            longerArray = array2;

        List<Integer> differences = new ArrayList<>();
        int i = 0;
        for (; i < lowestLength; i++) {
            int number1 = array1[i];
            int number2 = array2[i];
            int difference = compare(number1, number2);
            differences.add(difference);
        }

        boolean areNotEqualLength = lowestLength != longestLength;

        if (areNotEqualLength) {
            int lastDifference = differences.getLast();
            if (lastDifference == EQUAL) {
                retVal = HIGHER;
            } else {
                retVal = differences.getLast();
            }
        } else {
            for (int difference : differences) {
                if (difference == EQUAL) continue;
                retVal = difference;
                break;
            }
        }

        return retVal;
    }

    public int compare(Integer int1, Integer int2) {
        if (int1 > int2) return HIGHER;
        if (int1 < int2) return LOWER;
        return EQUAL;
    }

    private Pair<String, Integer> getLowerLength(int length1, int length2) {
        if (length1 < length2)
            return new Pair<>("lengthVersions1", length1);
        else
            return new Pair<>("lengthVersions2", length2);
    }
}
