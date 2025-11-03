package io.github.dracosomething.windows;

import io.github.dracosomething.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configure extends File {
    private final BufferedWriter writer;

    Configure(File parent, StringBuilder text) throws IOException {
        super(parent, "./config.xml");
        FileUtils.makeFile(this);
        this.writer = new BufferedWriter(new FileWriter(this));
        this.writer.append(text);
    }

    public static class ConfigureBuilder {
        private File parent;
        private StringBuilder contents = new StringBuilder();
        private int tabCounter = 0;
        private List<String> parentList = new ArrayList<>();

        public ConfigureBuilder setParentFile(File parent) {
            this.parent = parent;
            return this;
        }

        public ConfigureBuilder addXMLObject(String name, Object value) {
            insertTabs();
            this.contents.append("<").append(name).append(">").append(value.toString())
                    .append("</").append(name).append(">").append("\n");
            return this;
        }

        private void insertTabs() {
            for (int i = 0; i < tabCounter; i++) {
                contents.append("\t");
            }
        }

        public ConfigureBuilder createInternalObject(String name) {
            insertTabs();
            tabCounter++;
            this.contents.append("<").append(name).append(">").append("\n");
            parentList.add(name);
            return this;
        }

        public ConfigureBuilder leaveInternalObject() {
            tabCounter--;
            insertTabs();
            String name = parentList.getLast();
            this.contents.append("</").append(name).append(">").append("\n");
            parentList.remove(name);
            return this;
        }

        public ConfigureBuilder comment(String comment) {
            insertTabs();
            this.contents.append("<!--").append("\n").append(comment).append("\n").append("-->").append("\n");
            return this;
        }

        public Configure build() throws IOException {
            Configure configure = new Configure(parent, contents);
            configure.writer.close();
            return configure;
        }
    }
}
