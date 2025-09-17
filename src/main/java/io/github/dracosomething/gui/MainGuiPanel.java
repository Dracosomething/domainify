package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;

public class MainGuiPanel extends JPanel {

    public MainGuiPanel(JFrame frame) {
        CustomDomain.readDomainXML();
        this.setSize(1000, 1500);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(50, 50, 50));
        MainGuiPanel panel = this;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
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
        this.add(domainList, gbc);
        gbc.gridx = 1;
        this.add(add, gbc);
    }
}
