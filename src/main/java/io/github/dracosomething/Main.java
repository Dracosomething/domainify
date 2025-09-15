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
        frame.pack();
        frame.setSize(800, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        MainGuiPanel panel = new MainGuiPanel(frame);

        frame.setContentPane(panel);
    }
}