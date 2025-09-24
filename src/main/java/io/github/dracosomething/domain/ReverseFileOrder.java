package io.github.dracosomething.domain;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class ReverseFileOrder implements Comparator<File>, Serializable {
    public static final ReverseFileOrder REVERSE_FILE_ORDER = new ReverseFileOrder();

    @Override
    public int compare(File o1, File o2) {
        return o2.compareTo(o1);
    }

    @Override
    public Comparator<File> reversed() {
        return Comparator.naturalOrder();
    }
}
