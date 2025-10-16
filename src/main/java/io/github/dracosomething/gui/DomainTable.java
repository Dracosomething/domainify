package io.github.dracosomething.gui;

import io.github.dracosomething.domain.CustomDomain;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.util.HashMap;
import java.util.Map;

public class DomainTable extends JTable {
    private Map<Integer, CustomDomain> values = new HashMap<>();

    public DomainTable(JFrame frame) {
        this.setTableHeader(null);
        DefaultTableModel model = new DefaultTableModel(0, 6);
        model.addRow(new String[]{"name", "server admin", "path", "server aliases", "custom log", "error log"});

        DomainTable table = this;
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (table.getSelectedRow() <= 0) {
                    table.clearSelection();
                } else {
                    int selected = table.getSelectedRow();
                    CustomDomain domain = values.get(selected);
                    frame.setContentPane(new EditDomainPanel(frame, domain));
                    frame.pack();
                }
            }
        });

        int i = 0;
        for (CustomDomain domain : CustomDomain.DOMAINS) {
            model.addRow(domain.toArray());
            values.put(++i, domain);
        }

        this.setModel(model);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.getColumnModel().getColumn(0).setPreferredWidth(200);
        this.getColumnModel().getColumn(1).setPreferredWidth(150);
        this.getColumnModel().getColumn(2).setPreferredWidth(200);
        this.getColumnModel().getColumn(3).setPreferredWidth(150);
        this.getColumnModel().getColumn(4).setPreferredWidth(150);
        this.getColumnModel().getColumn(5).setPreferredWidth(100);

        this.setRowHeight(50);
        this.setFont(frame.getFont().deriveFont(20F));
    }
}
