package io.github.dracosomething.util;

import java.nio.channels.Channels;
import java.util.*;
import java.util.regex.Pattern;

public class HTMLObject {
    private static final Pattern HTML_HEADER_REGEX = Pattern.compile("<(.*) .*?>.*?</\\1>");
    private final String type;
    private final String contents;
    private final Map<String, String> data;
    private final List<HTMLObject> childObjects;

    HTMLObject(String type, String contents, Map<String, String> data, List<HTMLObject> childObjects) {
        this.type = type;
        this.contents = contents;
        this.data = data;
        this.childObjects = childObjects;
    }

    public static HTMLObject parse(String string) {
        // example html string
        // <img src="/icons/compressed.gif" alt="[TGZ]"> <a href="httpd-2.4.65.tar.gz">httpd-2.4.65.tar.gz</a>         2025-07-23 11:31  9.4M  HTTP Server project
        string = string.trim();
        char[] arr = string.toCharArray();
        String type = "";
        Map<String, String> data = new HashMap<>();
        StringBuilder contents = new StringBuilder();
        boolean hasParsedHeader = false;
        for (int i = 0; i < arr.length; i++) {
            char character = arr[i];
            if (!hasParsedHeader && (character == '<' && arr[i+1] != '/')) {
                StringBuilder builder = new StringBuilder();
                while (!Character.isWhitespace(arr[++i])) {
                    character = arr[i];
                    builder.append(character);
                }
                type = builder.toString();

                while (arr[i+1] != '>') {
                    builder = new StringBuilder();
                    while (arr[++i] != '=') {
                        builder.append(arr[i]);
                    }
                    String key = builder.toString();

                    String value;
                    builder = new StringBuilder();
                    if (arr[i + 1] == '"') {
                        i++;
                        while (arr[++i] != '"') {
                            character = arr[i];
                            builder.append(character);
                        }
                        value = builder.toString();
                    } else {
                        while (!Character.isWhitespace(arr[++i])) {
                            character = arr[i];
                            builder.append(character);
                        }
                        value = builder.toString();
                    }

                    if (character != arr[i]) {
                        character = arr[i];
                    }

                    key = key.trim();
                    value = value.trim();

                    data.put(key, value);
                }
                character = arr[i+=2];
                hasParsedHeader = true;
            }

            if (character == '<' && arr[i+1] == '/') {
                int iCache = i;
                StringBuilder builder = new StringBuilder();
                while (arr[i+1] != '>') {
                    character = arr[++i];
                    if (character == '/') continue;
                    builder.append(character);
                }
                i = iCache;
                character = arr[i];
                if (builder.toString().equals(type)) {
                    break;
                }
            }

            if ((character == '>' || arr[i+1] == '>') && type.equals("img")) {
                break;
            }

            if (hasParsedHeader) {
                contents.append(character);
            }
        }

        String content = contents.toString();
        String[] tmpArray = content.split(HTML_HEADER_REGEX.pattern());
        String shouldParse = contents.toString();
        for (String tmp : tmpArray) {
            shouldParse = shouldParse.replace(tmp, "\n");
        }
        String[] objectArr = shouldParse.split("\n");
        List<HTMLObject> childObjects = new ArrayList<>();
        for (String object : objectArr) {
            if (object.isBlank() || object.isEmpty()) {
                continue;
            }
            HTMLObject toAdd = parse(object);
            childObjects.add(toAdd);
        }

        return new HTMLObject(type, content, data, childObjects);
    }

    public String getType() {
        return type;
    }

    public String getContents() {
        return contents;
    }

    public String getProperty(String name) {
        return this.data.get(name);
    }

    public boolean hasProperty(String name) {
        return this.data.containsKey(name);
    }

    public List<HTMLObject> getChildObjects() {
        return childObjects;
    }

    public HTMLObject getChildObject(int index) {
        return this.childObjects.get(index);
    }
}
