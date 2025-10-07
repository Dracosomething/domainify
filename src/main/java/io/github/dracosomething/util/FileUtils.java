package io.github.dracosomething.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

import javax.swing.plaf.synth.Region;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        String regex = null;
        if (extraData != null) {
            regex = Util.formatArrayToRegex(extraData);
        }
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

    public static File downloadFileFromWeb(String urlString, File downloadLocation, String fileName) throws IOException {
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        ReadableByteChannel channel = Channels.newChannel(url.openStream());
        File download = new File(downloadLocation, "/" + fileName);
        FileOutputStream outputStream = new FileOutputStream(download);
        FileChannel fileChannel = outputStream.getChannel();
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
        outputStream.close();
        return download;
    }

    public static String getWindowsApacheDownloadLink(String fileName, int vsVersion, LocalDate lastCheckedDate) {
        String base = "http://www.apachelounge.com/download/";
        String afterVSVer = "/binaries/" + fileName + "-";
        String visualStudioVer = "VS" + vsVersion;
        String year = "25";
        String month = "07";
        String day = "24";
        String date = year + month + day;
        String windowsVer = "Win32";
        if (Util.IS_64_BIT) {
            windowsVer = "Win64";
        }
        String url = base + visualStudioVer + afterVSVer + date + "-" + windowsVer + "-" + visualStudioVer + ".zip";
        return url;
    }

    public static boolean isValidUrl(String urlString) {
        URI uri = URI.create(urlString);
        try {
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            int responseCode = con.getResponseCode();
            return  responseCode > 299;
        } catch (Exception e) {
            return false;
        }
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
        String PHPVersion = "";
        String mySQLVersion = "";
        int vsVersion = 17;
        LocalDate lastCheckedDate = LocalDate.now();
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
                case "php version" -> PHPVersion = value;
                case "sql version" -> mySQLVersion = value;
                case "VS" -> vsVersion = Integer.getInteger(value);
                case "last checked date" -> lastCheckedDate = LocalDate.parse(value);
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(DATA));

        String fileName = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/httpd/").toURL(), "httpd",
                ".tar.gz", new String[]{"TGZ"}, false, true);
        writer.append("apache version=").append(fileName).append(System.lineSeparator());
        if (!Objects.equals(apacheVer, fileName) || apacheDir.listFiles() == null) {
            clearDirectory(apacheDir);
            Console console = new Console();
            console.directory(apacheDir);
            if (Util.IS_WINDOWS) {
                // windows install link www.apachelounge.com/download/VS17/binaries/httpd-2.4.65-250724-Win64-VS17.zip
                // need to get current visual studio version used for apache
                // need apache version
                // id 2.4.62 = 240904
                // id 2.4.63 = 250207
                // id 2.4.65 = 250724
                // format as YYMMDD
                // save latest

                String url = getWindowsApacheDownloadLink(fileName, vsVersion, lastCheckedDate);

                File apacheZipped = downloadFileFromWeb(url, apacheDir, fileName + ".zip");
                File apache = unZip(apacheZipped, apacheDir);
            } else {
                File apacheZipped = downloadFileFromWeb("https://dlcdn.apache.org/httpd", apacheDir,
                        "httpd", ".tar.gz", new String[]{"TGZ"}, false);
                // apache download
                // download apr packages.(https://dlcdn.apache.org/apr/)
                // follow download instructions (https://httpd.apache.org/docs/2.4/platform/win_compiling.html)
                // run command line stuff (https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java)
                // code HTMLObject class with String type, Map<String, String> properties, List<HTMLObject> subObjects
                // add os check so that I can decide how to run commands.

                // unpack
                File apacheTar = unGzip(apacheZipped, apacheDir);
                File httpd = unTar(apacheTar, apacheDir, fileName);

                console.runCommand("./configure --prefix=apache");
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
        byte[] buffer = new byte[1024];
        ZipInputStream in = new ZipInputStream(new FileInputStream(infile));
        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            System.out.println("Moving " + entry.getName() + " to " + outDir.getPath() + "...");
            File toMove = getFileFromZipEntry(outDir, entry, shouldRemove);
            if (toMove == null) continue;
            if (entry.isDirectory()) {
                if (!toMove.isDirectory() && !toMove.mkdirs()) {
                    throw new FileExistsException(toMove);
                }
            } else {
                File parent = toMove.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new FileExistsException(toMove);
                }

                FileOutputStream out = new FileOutputStream(toMove);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.close();
            }
        }
        in.closeEntry();
        in.close();
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

    public static File getFileFromZipEntry(File directory, ZipEntry entry, String shouldRemove) throws IOException {
        File out = new File(directory, entry.getName());
        if (shouldRemove != null) {
            out = new File(directory, entry.getName().replaceFirst(shouldRemove, ""));
        }
        String directoryPath = directory.getCanonicalPath();
        String filePath = out.getCanonicalPath();

        if (!filePath.startsWith(directoryPath + File.separator)) {
            return null;
        }

        return out;
    }
}
