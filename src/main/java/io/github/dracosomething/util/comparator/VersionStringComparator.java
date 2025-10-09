package io.github.dracosomething.util.comparator;

import java.util.regex.Pattern;

public class VersionStringComparator extends VersionComparator<String> {
    private static final Pattern LINK_REGEX = Pattern.compile("https://[a-zA-Z]*.com/([a-zA-Z0-9_.-]*/)+");

    public VersionStringComparator(String fileName, String fileExtension) {
        super(fileName, fileExtension);
    }

    @Override
    public String format(String object) {
        return object.replaceAll(LINK_REGEX.pattern(), "");
    }
}
