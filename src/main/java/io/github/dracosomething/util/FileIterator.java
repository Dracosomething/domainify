package io.github.dracosomething.util;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileIterator extends ImprovedIterator<String> {
    private Map<Character, SkipType> toSkip;

    public FileIterator(File file) throws FileNotFoundException {
        super(new BufferedReader(new FileReader(file)).lines().toList());
        toSkip = new HashMap<>();
    }

    public void fullySkip(char character) {
        fullySkip(character, SkipType.CONTAIN);
    }

    public void fullySkip(char character, SkipType type) {
        toSkip.put(character, type);
    }

    public void whileSkip(char character) {
        whileSkip(character, SkipType.CONTAIN);
    }

    public void whileSkip(char character, SkipType type) {
        while (type.skipBehaviour(current, character)) {
            skip();
        }
    }

    public void skip() {
        setNext();
    }

    @Override
    public String next() {
        List<Pair<Character, SkipType>> list = Util.mapToPairList(toSkip);
        for (Pair<Character, SkipType> pair : list) {
            Character character = pair.getKey();
            SkipType type = pair.getValue();
            while (type.skipBehaviour(next, character)) {
                skip();
            }
        }
        return super.next();
    }

    public enum SkipType {
        BEGIN {
            @Override
            public boolean skipBehaviour(String line, char c) {
                return Util.stringStartsWithCharacter(line, c);
            }
        },
        END{
            @Override
            public boolean skipBehaviour(String line, char c) {
                return Util.stringEndsWithCharacter(line, c);
            }
        },
        CONTAIN{
            @Override
            public boolean skipBehaviour(String line, char c) {
                return Util.stringContainsCharacter(line, c);
            }
        };

        public abstract boolean skipBehaviour(String line, char c);
    }
}
