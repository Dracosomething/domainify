package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;
import io.github.dracosomething.domain.CustomDomainData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AddDomainPanel extends JPanel {
    private JFrame frame;
    private String name;
    private ArrayList<String> serverAlias;
    private File target;
    private String serverAdmin;
    private File errorLog;
    private File customLog;

    public AddDomainPanel(JFrame frame) {
        this.frame = frame;
        this.setSize(1000, 1500);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(255, 255, 255));

        AddDomainPanel panel = this;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 10 ,10 ,10);
        gbc.gridx = 0;
        gbc.gridy = 1;

        JLabel nameText = new JLabel("Enter server url");
        JTextField name = new JTextField(50);
        this.add(nameText, gbc);
        gbc.gridy = 2;
        this.add(name, gbc);

        JButton target = new JButton("Choose target directory");
        JTextField filePath = new JTextField(50);
        target.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooser chooser = new FileChooser(FileChooser.CURRENT_DIRECTORY);
                chooser.setOnConfirm(file -> {
                    panel.target = file;
                    filePath.setText(file.getPath());
                });
            }
        });
        gbc.gridy = 4;
        this.add(filePath,gbc);
        gbc.gridx = 1;
        this.add(target, gbc);
        gbc.gridx = 0;

        //===========================
        JLabel text = new JLabel("Input server admin address");
        JTextField admin = new JTextField(50);

        JLabel aliasText = new JLabel("<html>Input Server Alias.<br>Separate alias' with a comma/html>");
        JTextField alias = new JTextField(50);

        JButton logCustom = new JButton("Choose custom log directory");
        JTextField pathLogCustom = new JTextField(50);
        logCustom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooser chooser = new FileChooser(FileChooser.CURRENT_DIRECTORY);
                chooser.setOnConfirm(file -> {
                    panel.customLog = file;
                    pathLogCustom.setText(file.getPath());
                });
            }
        });

        JButton logError = new JButton("Choose error log directory");
        JTextField pathLogError = new JTextField(50);
        logError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooser chooser = new FileChooser(FileChooser.CURRENT_DIRECTORY);
                chooser.setOnConfirm(file -> {
                    panel.errorLog = file;
                    pathLogError.setText(file.getPath());
                });
            }
        });

        JButton collapse = new JButton("collapse");
        collapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.remove(admin);
                panel.remove(aliasText);
                panel.remove(alias);
                panel.remove(text);
                panel.remove(pathLogCustom);
                panel.remove(logCustom);
                panel.remove(pathLogError);
                panel.remove(logError);
                frame.pack();
            }
        });
        //============================
        JButton advanced = new JButton("<html><p>Advanced Options<p></html>");
        advanced.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gbc.gridy = 13;
                panel.add(text, gbc);
                gbc.gridy = 14;
                panel.add(admin, gbc);
                gbc.gridy = 16;
                panel.add(pathLogCustom, gbc);
                gbc.gridx = 1;
                panel.add(logCustom, gbc);
                gbc.gridx = 0;
                gbc.gridy = 18;
                panel.add(pathLogError, gbc);
                gbc.gridx = 1;
                panel.add(logError, gbc);
                gbc.gridy = 20;
                gbc.gridx = 0;
                gbc.anchor = GridBagConstraints.CENTER;
                panel.add(collapse, gbc);
                frame.pack();
            }
        });
        gbc.gridy = 12;
        this.add(advanced, gbc);

        JButton confirm = new JButton("<html>create domain</html>");
        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (name.getSelectedText().isEmpty()) {
                    String text = nameText.getText();
                    text += "<html><font color='red'>This field has to have a value.</font></html>";
                    nameText.setText(text);
                    return;
                }
                if (target.) {
                    String text = nameText.getText();
                    text += "<html><font color='red'>This field has to have a value.</font></html>";
                    nameText.setText(text);
                    return;
                }
                panel.name = name.getSelectedText();
                panel.serverAlias = new ArrayList<>(Arrays.stream(alias.getSelectedText().split(",")).toList());
                panel.serverAdmin = alias.getSelectedText();
                CustomDomainData data = null;
                if (panel.serverAlias != null || panel.errorLog != null || panel.customLog != null) {
                    data = new CustomDomainData(panel.serverAlias, panel.errorLog, panel.customLog);
                }
                CustomDomain domain = new CustomDomain(panel.name, panel.serverAdmin, panel.target);
                if (data == null) {
                    domain = new CustomDomain(panel.name, panel.serverAdmin, panel.target, data);
                }
                frame.setContentPane(new MainGuiPanel(frame));
            }
        });
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(confirm, gbc);
    }
}
