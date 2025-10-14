package io.github.dracosomething;

import io.github.dracosomething.gui.MainGuiPanel;
import io.github.dracosomething.util.FileUtils;
import io.github.dracosomething.util.Util;
import org.apache.commons.compress.archivers.ArchiveException;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        if (Util.firstLaunch()) {
            try {
                FileUtils.downloadRequirements();
            } catch (IOException | ArchiveException e) {
                e.printStackTrace();
            }
        }
        JFrame frame = new JFrame("domainify");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Font font  = frame.getFont();
        frame.setFont(font.deriveFont(50F));

        MainGuiPanel panel = new MainGuiPanel(frame);

        frame.setContentPane(panel);
        frame.pack();
        frame.setSize(frame.getWidth(), 800);
    }
}