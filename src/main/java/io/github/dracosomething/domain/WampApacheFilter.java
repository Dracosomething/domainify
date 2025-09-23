package io.github.dracosomething.domain;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class WampApacheFilter implements FilenameFilter {
    private static final Pattern VERSION_REGEX = Pattern.compile("apache([0-9]{1,3})[.]([0-9]{1,2})(([^.][^0-9]{1,2})|([.][0-9]{1,2}))(([^.0-9])|([.][0-9]{1,5}))(([^.0-9])|([.][0-9]{1,5}))");

    @Override
    public boolean accept(File dir, String name) {
        return VERSION_REGEX.matcher(name).matches();
    }
}
