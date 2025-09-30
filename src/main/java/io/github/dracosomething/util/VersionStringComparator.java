package io.github.dracosomething.util;

import java.util.Comparator;
import java.util.regex.Pattern;

public class VersionStringComparator implements Comparator<String> {
    private static Pattern LINK_REGEX = Pattern.compile("https://[a-zA-Z]*.com/([a-zA-Z0-9_.-]*/)+");
    private final String fileName;
    private final String fileExtension;

    public VersionStringComparator(String fileName, String fileExtension) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    @Override
    public int compare(String o1, String o2) {
        String version1 = o1.replaceAll(LINK_REGEX.pattern(), "").replace(fileName, "").replace(fileExtension, "");
        String version2 = o2.replaceAll(LINK_REGEX.pattern(), "").replace(fileName, "").replace(fileExtension, "");
        return version1.compareTo(version2);
    }
}
