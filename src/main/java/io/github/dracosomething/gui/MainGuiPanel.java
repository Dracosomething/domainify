package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;
import io.github.dracosomething.domain.DomainListModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class MainGuiPanel extends JPanel {
    public MainGuiPanel() {
        this.setSize(1000, 1500);
        this.setLayout(null);
        this.setVisible(true);
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(50, 50, 50));


        JList<CustomDomain> domainList = new JList<>(new DomainListModel());
        domainList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("ewrwerw");
            }
        });
        this.add(domainList);
    }
}
