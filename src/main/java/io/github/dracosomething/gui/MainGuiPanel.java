package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;
import io.github.dracosomething.domain.DomainListModel;

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
    private JFrame frame;

    public MainGuiPanel(JFrame frame) {
        this.frame = frame;
        this.setSize(1000, 1500);
        this.setLayout(null);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(50, 50, 50));
        MainGuiPanel panel = this;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridheight = 100;
        gbc.gridwidth = 100;
        gbc.gridx = 1;
        gbc.gridy = 1;

        JList<CustomDomain> domainList = new JList<>(new DomainListModel());

        this.add(domainList, gbc);

        gbc.gridx = 2;

        JButton add = new JButton("new domain");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new AddDomainPanel(frame));
                frame.pack();
            }
        });
        this.add(add);
    }
}
