package io.github.dracosomething.util.comparator;

import java.util.Comparator;

public abstract class VersionComparator<T> implements Comparator<T> {
    protected final String fileName;
    protected final String fileExtension;

    protected VersionComparator(String fileName, String fileExtension) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    public abstract String format(T object);

    @Override
    public int compare(T o1, T o2) {
        String version1 = format(o1).replace(fileName, "").replace(fileExtension, "");
        String version2 = format(o2).replace(fileName, "").replace(fileExtension, "");
        return version2.compareTo(version1);
    }
}
