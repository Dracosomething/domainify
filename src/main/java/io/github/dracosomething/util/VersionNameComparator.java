package io.github.dracosomething.util;

import java.util.Comparator;

public class VersionNameComparator implements Comparator<HTMLObject> {
    private final String fileName;
    private final String fileExtension;

    public VersionNameComparator(String fileName, String fileExtension) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    @Override
    public int compare(HTMLObject o1, HTMLObject o2) {
        String version1 = o1.getContents().replace(fileName, "").replace(fileExtension, "");
        String version2 = o2.getContents().replace(fileName, "").replace(fileExtension, "");
        return version2.compareTo(version1);
    }
}
