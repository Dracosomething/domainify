package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGuiPanel extends JPanel {

    public MainGuiPanel(JFrame frame) {
        CustomDomain.readDomainXML(CustomDomain.getConfig());
        this.setSize(1000, 1500);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(255, 255, 255));
        MainGuiPanel panel = this;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 10 ,10 ,10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        DomainTable domainList = new DomainTable(frame);
        JButton add = new JButton("new domain");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new AddDomainPanel(frame));
                frame.pack();
            }
        });
        JScrollPane scrollPane = new JScrollPane(domainList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setColumnHeader(null);
        scrollPane.setPreferredSize(new Dimension(800, 700));
        this.add(scrollPane, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.PAGE_START;
        this.add(add, gbc);
    }
}
