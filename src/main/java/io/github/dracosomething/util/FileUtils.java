package io.github.dracosomething.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class FileUtils {
    private static final Pattern HTTP_REGEX = Pattern.compile("httpd-([0-9]{1,3}\\.?|/)+");

    public static void createProjRoot() {
        if (Util.PROJECT.exists()) return;
        Util.PROJECT.mkdir();
    }

    public static BufferedReader getUrlReader(URL url) throws IOException {
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    public static URL getFileFromWeb(URL url, String fileExtension) throws IOException {
        BufferedReader in = getUrlReader(url);
        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
            if (!str.endsWith(fileExtension)) continue;
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

    public static void downloadRequirements() throws IOException, ArchiveException {
        final File apacheDir = new File(Util.PROJECT, "/apache/");
        final File phpDir = new File(Util.PROJECT, "/php/");
        final File serverDir = new File(Util.PROJECT, "/mysql/");
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
        url = getFileFromWeb(url, ".tar.gz");
        channel = Channels.newChannel(url.openStream());
        File apacheZipped = new File(apacheDir, "/apache.tar.gz");
        outputStream = new FileOutputStream(apacheZipped);
        fileChannel = outputStream.getChannel();
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        outputStream.close();

        // unpack
        File apacheTar = unGzip(apacheZipped, apacheDir);
        File http = unTar(apacheTar, apacheDir);

        // php download (https://www.php.net/downloads) VS17 x64 Thread Safe
        // extension = Win32-vs17-x64.zip
        // name = php
        // filter on highest
        // required new params: String name, String fileExtension, boolean shouldFilter
        // need new method downloadFileFromWeb with param URL url
        // downloads a file and returns it
        // need new method downloadFile with params URL url, String name, String fileExtension, boolean shouldFilter
        // calls getFileFromWeb and then downloadFileFromWeb and returns the file downloaded.

        // server (https://dev.mysql.com/downloads/installer) (mysql-installer-community-8.0.43.0.msi)

    }

    public static File unTar(File infile, File outDir) throws IOException, ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new FileInputStream(infile);
        TarArchiveInputStream tarIn = (TarArchiveInputStream) factory.createArchiveInputStream("tar", in);
        TarArchiveEntry entry = null;
        while ((entry = tarIn.getNextEntry()) != null) {
            if (HTTP_REGEX.matcher(entry.getName()).matches()) {
                continue;
            }
            File outputFile = new File(outDir, entry.getName().replaceAll(HTTP_REGEX.toString(), ""));
            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new FileExistsException(outputFile);
                    }
                }
            } else {
                OutputStream out = new FileOutputStream(outputFile);
                IOUtils.copy(tarIn, out);
                out.close();
            }
        }
        tarIn.close();
        infile.delete();
        return outDir;
    }

    public static File unGzip(File infile, File outDir) throws IOException, ArchiveException {
        File outFile = new File(outDir, infile.getName().replaceAll("\\.gz", ""));
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
        FileOutputStream out = new FileOutputStream(outFile);
        IOUtils.copy(in, out);
        in.close();
        out.close();
        infile.delete();
        return outFile;
    }
}
