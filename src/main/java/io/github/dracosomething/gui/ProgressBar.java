package io.github.dracosomething.gui;

import io.github.dracosomething.Main;
import io.github.dracosomething.gui.util.JLabelOutputStream;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.function.Supplier;

public class ProgressBar extends JFrame {
    private final JProgressBar progressBar;
    private final Runnable task;
    /**
     * this is here so that we can append logger messages to the text.
     */
    private final PrintStream textStream;

    public ProgressBar(Runnable task) {
        this.task = task;
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setValue(0);

        this.setName("Domainify Setup");
        this.setVisible(true);
        this.pack();
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel progressText = new JLabel();
        this.textStream = new PrintStream(new JLabelOutputStream(progressText));

        this.add(progressBar, gbc);
        gbc.gridy = 1;
        this.add(progressText, gbc);
    }

    public void incrementProgress() {
        this.progressBar.setValue(this.progressBar.getValue() + 1);
    }

    public boolean isDoneWithTask() {
        return this.progressBar.getValue() >= this.progressBar.getMaximum();
    }

    public void start() {
        Runnable task = this.task;
        Supplier<Boolean> threadTask = new Supplier() {
            @Override
            public Object get() {
                if (!isDoneWithTask()) {
                    task.run();
                    incrementProgress();
                }
                return isDoneWithTask();
            }
        };
        Main.schedule(threadTask);
    }
}
