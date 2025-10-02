package io.github.dracosomething.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import static io.github.dracosomething.util.Util.PATH_SEPARATOR;

public class FileUtils {
    public static final ArchiveStreamFactory factory = new ArchiveStreamFactory();
    public static final File DATA = new File(Util.PROJECT, PATH_SEPARATOR + "data.txt");

    public static void createProjRoot() {
        if (Util.PROJECT.exists()) {
            return;
        }
        Util.PROJECT.mkdir();
    }

    public static BufferedReader getUrlReader(URL url) throws IOException {
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    public static String getFileNameFromWeb(URL url, String name, String fileExtension, String[] extraData,
                                            boolean shouldFilter, boolean replaceExtension) throws IOException {
        String retVal = getFileNameFromWeb(url, name, fileExtension, extraData, shouldFilter);
        if (replaceExtension) {
            retVal = retVal.replace(fileExtension, "");
        }
        return retVal;
    }

    public static String getFileNameFromWeb(URL url, String name, String fileExtension) throws IOException {
        return getFileNameFromWeb(url, name, fileExtension, null, false);
    }

    public static String getFileNameFromWeb(URL url, String name, String fileExtension,
                                            boolean shouldFilter) throws IOException {
        return getFileNameFromWeb(url, name, fileExtension, null, shouldFilter);
    }

    public static String getFileNameFromWeb(URL url, String name, String fileExtension,
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
                object = HTMLObject.getAbsoluteLinkChild(object);
                list.add(object);
            }
        }
        in.close();
        if (shouldFilter) {
            list = list.stream().sorted(new VersionNameComparator(name, fileExtension)).toList();
        }
        HTMLObject object = list.getFirst();
        return object.getProperty("href");
    }

    public static String getFileNameFromJson(URL url, String name, String fileExtension, String[] extraData,
                                            boolean shouldFilter, boolean replaceExtension) throws IOException {
        String retVal = getFileNameFromJson(url, name, fileExtension, extraData, shouldFilter);
        if (replaceExtension) {
            retVal = retVal.replace(fileExtension, "");
        }
        return retVal;
    }

    public static String getFileNameFromJson(URL url, String name, String fileExtension,
                                             String[] extraData, boolean shouldFilter) throws IOException {
        BufferedReader in = getUrlReader(url);
        List<String> list = new ArrayList<>();
        String str;
        String regex = Util.formatArrayToRegex(extraData);
        while ((str = in.readLine()) != null) {
            if (!str.contains(name)) continue;
            if (extraData != null && !str.matches(regex)) continue;
            if (str.contains(fileExtension)) {
                str = str.trim();
                str = str.replace("\"", "");
                str = str.replace("url: ", "");
                list.add(str);
            }
        }
        in.close();
        if (shouldFilter) {
            list = list.stream().sorted(new VersionStringComparator(name, fileExtension)).toList();
        }
        return list.getFirst();
    }

    public static URL getDownloadLinkFromWeb(URL url, String name, String fileExtension) throws IOException {
        return getDownloadLinkFromWeb(url, name, fileExtension, null, false);
    }

    public static URL getDownloadLinkFromWeb(URL url, String name, String fileExtension,
                                             boolean shouldFilter) throws IOException {
        return getDownloadLinkFromWeb(url, name, fileExtension, null, shouldFilter);
    }

    public static URL getDownloadLinkFromWeb(URL url, String name, String fileExtension,
                                             String[] extraData, boolean shouldFilter) throws IOException {
        if (url.toString().endsWith(".json")) {
            String target = getFileNameFromJson(url, name, fileExtension, extraData, shouldFilter);
            return URI.create(target).toURL();
        }
        String targetFile = getFileNameFromWeb(url, name, fileExtension, extraData, shouldFilter);
        String tmp = url.toString();
        if (targetFile.contains("github")) {
            tmp = targetFile;
        } else {
            if (!targetFile.startsWith("/")) {
                tmp += "/";
            }
            tmp += targetFile;
        }
        URI uri = URI.create(tmp);
        return uri.toURL();
    }

    public static File downloadFileFromWeb(String urlString, File downloadLocation, String fileName,
                                           String fileExtension, String[] extraData, boolean shouldFilter)
            throws IOException {
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        url = getDownloadLinkFromWeb(url, fileName, fileExtension, extraData, shouldFilter);
        ReadableByteChannel channel = Channels.newChannel(url.openStream());
        File download = new File(downloadLocation, "/" + fileName + fileExtension);
        FileOutputStream outputStream = new FileOutputStream(download);
        FileChannel fileChannel = outputStream.getChannel();
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        outputStream.close();
        return download;
    }

    public static File downloadFileFromWeb(String urlString, File downloadLocation, String fileName,
                                           String fileExtension, boolean shouldFilter)
            throws IOException {
        return downloadFileFromWeb(urlString, downloadLocation, fileName, fileExtension, null, shouldFilter);
    }

    public static File downloadFileFromWeb(String urlString, File downloadLocation, String fileName,
                                           String fileExtension)
            throws IOException {
        return downloadFileFromWeb(urlString, downloadLocation, fileName, fileExtension, null, false);
    }

    public static void downloadRequirements() throws IOException, ArchiveException {
        final File apacheDir = new File(Util.PROJECT, PATH_SEPARATOR + "apache" + PATH_SEPARATOR);
        final File phpDir = new File(Util.PROJECT,  PATH_SEPARATOR + "php" + PATH_SEPARATOR);
        final File serverDir = new File(Util.PROJECT, PATH_SEPARATOR + "mysql" + PATH_SEPARATOR);
        if (!apacheDir.exists()) {
            apacheDir.mkdir();
        }
        if (!phpDir.exists()) {
            phpDir.mkdir();
        }
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
        if (!DATA.exists()) {
            DATA.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(DATA));
        String apacheVer = "";
        String perlVer = "";
        String APRVer = "";
        String APRUtilVer = "";
        String APRIconvVer = "";
        String PHPVersion = "";
        String mySQLVersion = "";
        String str;
        while ((str = reader.readLine()) != null) {
            StringBuilder builder = new StringBuilder();
            for (char character : str.toCharArray()) {
                if (character == '=') break;
                builder.append(character);
            }
            String value = str.replace(builder.toString()+"=", "");
            switch (builder.toString()) {
                case "apache version" -> apacheVer = value;
                case "perl version" -> perlVer = value;
                case "APR version" -> APRVer = value;
                case "APR-util version" -> APRUtilVer = value;
                case "APR-iconv version" -> APRIconvVer = value;
                case "php version" -> PHPVersion = value;
                case "sql version" -> mySQLVersion = value;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(DATA));

        String fileName = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/httpd").toURL(), "httpd",
                ".tar.gz", new String[]{"TGZ"}, false, true);
        writer.append("apache version=").append(fileName).append(System.lineSeparator());
        if (!Objects.equals(apacheVer, fileName) || apacheDir.listFiles() == null) {
            clearDirectory(apacheDir);
            File apacheZipped = downloadFileFromWeb("https://dlcdn.apache.org/httpd", apacheDir, "httpd",
                    ".tar.gz", new String[]{"TGZ"}, false);
            // apache download
            // download apr packages.(https://dlcdn.apache.org/apr/)
            // follow download instructions (https://httpd.apache.org/docs/2.4/platform/win_compiling.html)
            // run command line stuff (https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java)
            // code HTMLObject class with String type, Map<String, String> properties, List<HTMLObject> subObjects
            // add os check so that I can decide how to run commands.

            // unpack
            File apacheTar = unGzip(apacheZipped, apacheDir);
            File httpd = unTar(apacheTar, apacheDir, fileName);
            Console console = new Console();
            console.directory(httpd);
            if (Util.IS_WINDOWS) {
                File perlInterpreter = new File(Util.PROJECT, "/perl/");
                if (!perlInterpreter.exists()) {
                    perlInterpreter.mkdir();
                }
                String perlVersion = getFileNameFromJson(URI.create("https://strawberryperl.com/releases.json").toURL(),
                        "strawberry-perl", ".zip",
                        new String[]{"64bit-portable"}, true, true);
                writer.append("perl version=").append(perlVersion).append(System.lineSeparator());
                if (!perlVer.equals(perlVersion) || !new File(perlInterpreter, "/portableshell.bat").exists()) {
                    clearDirectory(perlInterpreter);
                    File perlZip = downloadFileFromWeb("https://strawberryperl.com/releases.json",
                            perlInterpreter, "strawberry-perl", ".zip",
                            new String[]{"64bit-portable"}, true);
                    File perl = unZip(perlZip, perlInterpreter);
                }

                File httpdAPR = new File(httpd, "/srclib/apr");
                if (!httpdAPR.exists()) {
                    httpdAPR.mkdir();
                }
                String APRDir = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/apr").toURL(), "apr",
                        ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"}, true,
                        true);
                writer.append("APR version").append(APRDir).append(System.lineSeparator());
                if (!APRVer.equals(APRDir)) {
                    clearDirectory(httpdAPR);
                    File APRGZip = downloadFileFromWeb("https://dlcdn.apache.org/apr", httpdAPR, "apr",
                            ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"}, true);
                    File APRTar = unGzip(APRGZip, httpdAPR);
                    File APR = unTar(APRTar, httpdAPR, APRDir);
                }

                File httpdAPRUtil = new File(httpd, "/srclib/apr-util");
                if (!httpdAPRUtil.exists()) {
                    httpdAPRUtil.mkdir();
                }
                String APRUtilDir = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/apr").toURL(), "apr-util",
                        ".tar.gz", new String[]{"TGZ"}, true,
                        true);
                writer.append("APR-util version=").append(APRUtilDir).append(System.lineSeparator());
                if (!APRUtilVer.equals(APRUtilDir)) {
                    clearDirectory(httpdAPRUtil);
                    File APRUtilGZip = downloadFileFromWeb("https://dlcdn.apache.org/apr", httpdAPRUtil, "apr-util",
                            ".tar.gz", new String[]{"TGZ"}, true);
                    File APRUtilTar = unGzip(APRUtilGZip, httpdAPRUtil);
                    File APRUtil = unTar(APRUtilTar, httpdAPRUtil, APRUtilDir);
                }

                File httpdAPRIconv = new File(httpd, "/srclib/apr-iconv");
                if (!httpdAPRIconv.exists()) {
                    httpdAPRIconv.mkdir();
                }
                String APRIconvDir = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/apr").toURL(), "apr-iconv",
                        ".tar.gz", new String[]{"TGZ"}, true,
                        true);
                writer.append("APR-iconv version=").append(APRIconvDir).append(System.lineSeparator());
                if (!APRIconvVer.equals(APRIconvDir)) {
                    clearDirectory(httpdAPRIconv);
                    File APRIconvGZip = downloadFileFromWeb("https://dlcdn.apache.org/apr", httpdAPRIconv, "apr-iconv",
                            ".tar.gz", new String[]{"TGZ"}, true);
                    File APRIconvTar = unGzip(APRIconvGZip, httpdAPRIconv);
                    File APRIconv = unTar(APRIconvTar, httpdAPRIconv, APRIconvDir);
                }

                console = new Console(new File(perlInterpreter, "/portableshell.bat"));
                console.directory(httpd);
                console.runCommand("perl .\\srclib\\apr\\build\\fixwin32mak.pl");
                console.schedule(console1 -> {
                    console1 = new Console();
                    console1.directory(httpd);
                    console1.runCommand("nmake /f Makefile.win _apacher");
                    console1.runCommand("nmake /f Makefile.win installr INSTDIR=" + httpd);
                });
            } else {
                console.runCommand("./configure --prefix=" + httpd.getPath());
                console.runCommand("make");
                console.runCommand("make install");
            }
            System.out.println("Apache installed.");
        }

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
        String phpName = getFileNameFromWeb(URI.create("https://downloads.php.net/~windows/releases/archives").toURL(),
                "php", ".zip", new String[]{"Win32-vs17-x64"}, true, true);
        writer.append("php version=").append(phpName).append(System.lineSeparator());
        if (!Objects.equals(phpName, PHPVersion) || phpDir.listFiles() == null) {
            clearDirectory(phpDir);
            File PHPZip = downloadFileFromWeb("https://downloads.php.net/~windows/releases/archives", phpDir,
                    "php", ".zip", new String[]{"Win32-vs17-x64"}, true);
            File PHP = unZip(PHPZip, phpDir);
        }

        // server (https://dev.mysql.com/downloads/installer) (mysql-installer-community-8.0.43.0.msi)

        writer.close();
    }

    public static File unTar(File infile, File outDir) throws IOException, ArchiveException {
        return unTar(infile, outDir, null);
    }

    public static File unTar(File infile, File outDir, String shouldRemove) throws IOException, ArchiveException {
        System.out.println("Untar " + infile.getPath() + "...");
        InputStream in = new FileInputStream(infile);
        TarArchiveInputStream tarIn = (TarArchiveInputStream) factory.createArchiveInputStream("tar", in);
        TarArchiveEntry entry = null;
        while ((entry = tarIn.getNextEntry()) != null) {
            System.out.println("Moving file " + entry.getName() + "...");
            File outputFile;
            if (shouldRemove == null) {
                outputFile = new File(outDir, entry.getName());
            } else {
                outputFile = new File(outDir, entry.getName().replaceAll(shouldRemove, ""));
            }
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

    public static File unGzip(File infile, File outDir) throws IOException {
        File outFile = new File(outDir, infile.getName().replaceAll("\\.gz", ""));
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
        FileOutputStream out = new FileOutputStream(outFile);
        IOUtils.copy(in, out);
        in.close();
        out.close();
        infile.delete();
        return outFile;
    }

    public static File unZip(File infile, File outDir) throws IOException, ArchiveException {
        return unZip(infile, outDir, null);
    }

    public static File unZip(File infile, File outDir, String shouldRemove) throws IOException, ArchiveException {
        System.out.println("Unzipping " + infile.getPath() + "...");
        File outFile = new File(outDir, infile.getName().replaceAll("\\.zip", ""));
        InputStream in = new FileInputStream(infile);
        ZipArchiveInputStream zipStream = factory.createArchiveInputStream("zip", in);
        ZipArchiveEntry entry = null;
        while ((entry = zipStream.getNextEntry()) != null) {
            System.out.println("Moving " + entry.getName() + " to " + outFile.getPath() + "...");
            if (shouldRemove != null && entry.getName().contains(shouldRemove)) {
                continue;
            }
            File outputFile;
            if (shouldRemove == null) {
                outputFile = new File(outDir, entry.getName());
            } else {
                outputFile = new File(outDir, entry.getName().replaceAll(shouldRemove, ""));
            }
            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new FileExistsException(outputFile);
                    }
                }
            } else {
                OutputStream out = new FileOutputStream(outputFile);
                IOUtils.copy(zipStream, out);
                out.close();
            }
        }
        zipStream.close();
        infile.delete();
        return outFile;
    }

    public static void clearDirectory(File dir) {
        System.out.println("Clearing " + dir.getPath() + "...");
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File file : arr) {
                System.out.println("Deleting file:" + file.getPath() + "...");
                if (file.isDirectory()) {
                    System.out.println("File is directory, calling self on file...");
                    clearDirectory(file);
                }
                if (file.delete()) {
                    System.out.println("Deleted file. Moving on to next file...");
                }
            }
            if (arr.length <= 1) {

            }
        }
    }
}
