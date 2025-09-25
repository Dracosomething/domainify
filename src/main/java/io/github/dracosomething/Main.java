package io.github.dracosomething;

import io.github.dracosomething.gui.MainGuiPanel;
import io.github.dracosomething.util.FileUtils;
import io.github.dracosomething.util.Util;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.*;

public class Main {
    public static void main(String[] args) {
        if (Util.firstLaunch()) {
            FileUtils.createProjRoot();
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