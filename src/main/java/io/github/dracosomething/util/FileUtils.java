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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public static URL getFileFromWeb(URL url, String name, String fileExtension,
                                     boolean shouldFilter) throws IOException {
        return getFileFromWeb(url, name, fileExtension, null, shouldFilter);
    }

    public static URL getFileFromWeb(URL url, String name, String fileExtension,
                                     String[] extraData, boolean shouldFilter) throws IOException {
        BufferedReader in = getUrlReader(url);
        List<HTMLObject> list = new ArrayList<>();
        String str;
        String regex = Util.formatArrayToRegex(extraData);
        while ((str = in.readLine()) != null) {
            if (!str.contains(name)) continue;
            if (extraData != null && !str.matches(regex)) continue;
            if (str.contains(fileExtension)) {
                String[] tmpArray = str.split("<(.*) .*?>.*?</\\1>");
                for (String tmp : tmpArray) {
                    str = str.replace(tmp, "");
                }
                HTMLObject object = HTMLObject.parse(str);
                list.add(object);
            }
        }
        in.close();
        if (shouldFilter) {
            list.forEach(System.out::println);
            list = list.stream().sorted(new VersionNameComparator(name, fileExtension)).toList();
            list.forEach(System.out::println);
        }
        HTMLObject object = list.getFirst();
        String tmp = url.toString();
        tmp += "/";
        tmp += object.getProperty("href");
        URI uri = URI.create(tmp);
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
        url = getFileFromWeb(url, "httpd", ".tar.gz", new String[]{"TGZ"}, false);
        channel = Channels.newChannel(url.openStream());
        File apacheZipped = new File(apacheDir, "/apache.tar.gz");
        outputStream = new FileOutputStream(apacheZipped);
        fileChannel = outputStream.getChannel();
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        outputStream.close();
        // download apr packages.(https://dlcdn.apache.org/apr/)
        // follow download instructions (https://httpd.apache.org/docs/2.4/platform/win_compiling.html)
        // run command line stuff (https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java)
        // code HTMLObject class with String type, Map<String, String> properties, List<HTMLObject> subObjects
        // add os check so that i can decide how to run commands.
        
        // unpack
        File apacheTar = unGzip(apacheZipped, apacheDir);
        File http = unTar(apacheTar, apacheDir);
        File targetFile = new File(apacheDir, "/httpd-2.4.65");
        Console console = new Console();
        console.directory(http);
        console.runCommand("nmake /f Makefile.win _apacher");
        console.runCommand("nmake /f Makefile.win installr INSTDIR=" + apacheDir.getPath());

        // php download (https://www.php.net/downloads) VS17 x64 Thread Safe
        // extension = .zip
        // extraData = Win32-vs17-x64
        // name = php
        // filter on highest
        // required new params: String name, String fileExtension, String description, Pattern extraData, boolean shouldFilter
        // need new method downloadFileFromWeb with param URL url
        // downloads a file and returns it
        // need new method downloadFile with params URL url, String name, String fileExtension, String description, boolean shouldFilter
        // calls getFileFromWeb and then downloadFileFromWeb and returns the file downloaded.

        // server (https://dev.mysql.com/downloads/installer) (mysql-installer-community-8.0.43.0.msi)

    }

    public static File unTar(File infile, File outDir) throws IOException, ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new FileInputStream(infile);
        TarArchiveInputStream tarIn = (TarArchiveInputStream) factory.createArchiveInputStream("tar", in);
        TarArchiveEntry entry = null;
        while ((entry = tarIn.getNextEntry()) != null) {
            File outputFile = new File(outDir, entry.getName());
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
