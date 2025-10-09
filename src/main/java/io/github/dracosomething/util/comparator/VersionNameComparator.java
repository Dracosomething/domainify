package io.github.dracosomething.util.comparator;

import io.github.dracosomething.util.HTMLObject;

public class VersionNameComparator extends VersionComparator<HTMLObject> {
    public VersionNameComparator(String fileName, String fileExtension) {
        super(fileName, fileExtension);
    }

    @Override
    public String format(HTMLObject object) {
        return object.getContents();
    }
}
