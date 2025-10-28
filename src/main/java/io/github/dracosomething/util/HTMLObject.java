package io.github.dracosomething.util;

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
        String shouldParse = contents.toString();
        String[] tmpArray = content.split(HTML_HEADER_REGEX.pattern());
        for (String tmp : tmpArray) {
            shouldParse = shouldParse.replace(tmp, "willDeleteDomainify");
        }
        String[] objectArr = shouldParse.split("willDeleteDomainify");
        List<HTMLObject> childObjects = new ArrayList<>();
        for (String object : objectArr) {
            if (object.isBlank() || object.isEmpty()) {
                continue;
            }
            HTMLObject toAdd = parse(object);
            childObjects.add(toAdd);
        }

        HTMLObject retVal = new HTMLObject(type, content, data, childObjects);
        return retVal;
    }

    public String getType() {
        return type;
    }

    public String getContents() {
        return contents;
    }

    public String getProperty(String name) {
        if (hasProperty(name)) {
            return this.data.get(name);
        }
        return "";
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

    public boolean hasLinkChildren() {
        boolean retVal = false;
        for (HTMLObject object : this.childObjects) {
            if (Objects.equals(object.getType(), "a")) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }

    public HTMLObject getLinkChild() {
        HTMLObject retVal = null;
        for (HTMLObject object : this.childObjects) {
            if (Objects.equals(object.getType(), "a")) {
                retVal = object;
                break;
            }
        }
        return retVal;
    }

    public static HTMLObject getAbsoluteLinkChild(HTMLObject parent) {
        final ChildIterator iterator = new ChildIterator(parent);
        HTMLObject retVal = parent;
        HTMLObject object;
        while (iterator.hasNext()) {
            object = iterator.next();
            if (Objects.equals(object.getType(), "a")) {
                retVal = object;
                break;
            }
        }
        return retVal;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append(System.lineSeparator()).append(" ").append("\t");
        builder.append("type: ").append(this.type).append(", ").append(System.lineSeparator()).append(" ").append("\t");
        builder.append("contents: ").append(this.contents).append(", ").append(System.lineSeparator()).append(" ")
                .append("\t");
        builder.append("data: {").append(System.lineSeparator()).append(" ");
        data.forEach((string, string2) -> {
            builder.append("\t\t").append(string).append(": ").append(string2).append(", ")
                    .append(System.lineSeparator()).append(" ");
        });
        builder.append("\t").append("}").append(", ").append(System.lineSeparator()).append(" ");
        builder.append("childObjects: {").append(System.lineSeparator()).append(" ");
        childObjects.forEach(htmlObject -> {
            builder.append("\t\t").append(htmlObject).append(", ").append(System.lineSeparator()).append(" ");
        });
        builder.append("\t").append("}");
        builder.append(System.lineSeparator()).append(" ").append("}");
        return builder.toString();
    }
}
