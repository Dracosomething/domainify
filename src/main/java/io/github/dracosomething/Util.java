package io.github.dracosomething;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Util {
    public static boolean isProperPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
