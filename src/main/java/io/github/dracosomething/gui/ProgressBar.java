package io.github.dracosomething.gui;

import org.eclipse.jetty.util.thread.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class ProgressBar extends JFrame {
    private final JProgressBar progressBar;
    private final Scheduler.Task task;
    /**
     * this is here so that we can append logger messages to the text.
     */
    private final PrintStream textStream;

    public ProgressBar(Scheduler.Task task) {
        this.task = task;
        this.textStream = new PrintStream()
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setValue(0);

        this.setName("Domainify Setup");
        this.setVisible(true);
        this.pack();
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;

        JLabel progressText = new JLabel();

    }
}
