package io.github.dracosomething.gui.util;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JLabelOutputStream extends OutputStream {
    private final JLabel label;
    private final StringBuilder builder = new StringBuilder();

    public JLabelOutputStream(JLabel label) {
        this.label = label;
    }

    @Override
    public void write(int b) throws IOException {
        builder.append((char) b);
    }

    public void write(char b) {
        this.
    }
}
