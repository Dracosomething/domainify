package io.github.dracosomething;

import io.github.dracosomething.gui.MainGuiPanel;
import io.github.dracosomething.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipFile;

public class Main {
    public static void main(String[] args) {
        if (Util.firstLaunch()) {
            createProjRoot();
            try {
                downloadRequirements();
            } catch (IOException e) {
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

    private static void createProjRoot() {
        if (Util.PROJECT.exists()) return;
        Util.PROJECT.mkdir();
    }

    private static URL getFileFromWeb(URL url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
            if (!str.endsWith("HTTP Server project")) continue;
            if (!str.contains("[TGZ]")) continue;
            if (str.contains(".bz2")) continue;
            builder.append(str).append(System.lineSeparator());
        }
        in.close();
        String tmp = builder.toString();
        builder = new StringBuilder();
        for (char char_ : tmp.toCharArray()) {
            if (char_ == ' ') continue;
            builder.append(char_);
            if (char_ == '>') builder.append(System.lineSeparator());
        }
        Iterator<String> lines = builder.toString().lines().iterator();
        builder = new StringBuilder();
        while (lines.hasNext()) {
            String line = lines.next();
            if (!line.contains("href")) continue;
            builder.append(line);
        }
        tmp = builder.toString();
        builder = new StringBuilder();
        char[] arr = tmp.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 'h') {
                i += 5;
                while (arr[i+1] != '\"') {
                    char char_ = arr[++i];
                    builder.append(char_);
                }
                break;
            }
        }
        String retVal = url.toString();
        retVal += "/";
        retVal += builder.toString();
        URI uri = URI.create(retVal);
        return uri.toURL();
    }

    private static void downloadRequirements() throws IOException {
        final File apacheDir = new File(Util.PROJECT, "/apache");
        final File phpDir = new File(Util.PROJECT, "/php");
        final File serverDir = new File(Util.PROJECT, "/mysql");
        apacheDir.mkdir();
        phpDir.mkdir();
        serverDir.mkdir();

        ReadableByteChannel channel;
        FileOutputStream outputStream;
        FileChannel fileChannel;
        URI uri;
        URL url;

        // apache download
        uri = URI.create("https://dlcdn.apache.org/httpd");
        url = uri.toURL();
        url = getFileFromWeb(url);
        channel = Channels.newChannel(url.openStream());
        File apacheZipped = new File(apacheDir, "/apache.tar.gz");
        outputStream = new FileOutputStream(apacheZipped);
        fileChannel = outputStream.getChannel();
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        outputStream.close();

        // unpack
        File apache = unGzip(apacheZipped);
        apacheZipped.delete();

        // php download (https://www.php.net/downloads) VS17 x64 Thread Safe


        // server (https://dev.mysql.com/downloads/installer) (mysql-installer-community-8.0.43.0.msi)

    }

    public static File unGzip(File infile) throws IOException {
        GZIPInputStream gin = new GZIPInputStream(new FileInputStream(infile));
        FileOutputStream fos = null;
        try {
            File outFile = new File(infile.getParent(), infile.getName().replaceAll("\\.tar\\.gz$", ""));
            fos = new FileOutputStream(outFile);
//            byte[] buf = new byte[100000];
//            int len;
            gin.transferTo(fos);
//            while ((len = gin.read(buf)) > 0) {
//                fos.write(buf, 0, len);
//            }

            fos.close();
            infile.delete();
            return outFile;
        } finally {
            if (gin != null) {
                gin.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}