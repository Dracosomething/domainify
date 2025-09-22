package io.github.dracosomething;

import io.github.dracosomething.gui.MainGuiPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
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