package io.github.dracosomething.gui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Consumer;

public class FileChooser extends JDialog {
    public static final int SELECTED_FILE = 1;
    public static final int CURRENT_DIRECTORY = 2;

    private final int selectionType;
    private File selected;
    private Consumer<File> onConfirm;

    public FileChooser() {
        this(SELECTED_FILE);
    }

    public FileChooser(int selectionType) {
        this.selectionType = selectionType;
        this.setSize(100, 100);
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "Select Directory";
            }
        });
        FileChooser this_ = this;
        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
                    SwingUtilities.windowForComponent((JFileChooser) e.getSource()).dispose();
                } else if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                    if (selectionType == 1) {
                        this_.selected = chooser.getSelectedFile();
                    } else if (selectionType == 2) {
                        this_.selected = chooser.getCurrentDirectory();
                    }
                    this_.onConfirm.accept(this_.selected);
                    SwingUtilities.windowForComponent((JFileChooser) e.getSource()).dispose();
                }
            }
        });
        this.add(chooser);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.pack();
    }

    public void setOnConfirm(Consumer<File> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public File getSelected() {
        return selected;
    }
}
