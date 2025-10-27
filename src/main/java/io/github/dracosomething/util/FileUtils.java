package io.github.dracosomething.util;

import com.gargoylesoftware.htmlunit.javascript.host.intl.DateTimeFormat;
import io.github.dracosomething.util.comparator.VersionNameComparator;
import io.github.dracosomething.util.comparator.VersionStringComparator;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.text.DateFormatter;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.github.dracosomething.Main.LOGGER;

public class FileUtils {
    public static final ArchiveStreamFactory FACTORY = new ArchiveStreamFactory();
    public static final String PATH_SEPARATOR = System.getProperty("file.separator");
    // below should be in static initializer, ROOT should be accessible directory.
    public static final File ROOT;
    public static final File PROJECT;
    public static final File DATA;
    public static String SRVROOT = "";

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
        HTMLObject object = list.getFirst();
        if (shouldFilter) {
            list = list.stream().sorted(new VersionNameComparator(name, fileExtension)).toList();
            object = list.getFirst();
        }
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
        return downloadFileFromWeb(urlString, downloadLocation, fileName, fileExtension, null,
                false);
    }

    public static File downloadFileFromWeb(String urlString, File downloadLocation, String fileName)
            throws IOException {
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

    public static void configureApacheHttpd(File conf, File root) throws IOException {
        FileIterator iterator = new FileIterator(conf);
        StringBuilder contents = new StringBuilder();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.startsWith("Define SRVROOT")) {
                line = "Define SRVROOT \"" + root.toString() + "\"";
            }
            contents.append(line).append(System.lineSeparator());
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(conf));
        writer.append(contents.toString());
        writer.close();
    }

    public static void makeDir(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void makeFile(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // add loading screen.
    /**
     * method sets downloads all required files, and it updates them if necessary.
     *
     * @throws IOException
     * @throws ArchiveException
     */
    public static void downloadRequirements() throws IOException, ArchiveException, NoSuchMethodException {
        Method downloadRequirements = FileUtils.class.getMethod("downloadRequirements", new Class[]{});
        LOGGER.entering(downloadRequirements);
        makeDir(PROJECT);
        final File apacheDir = new File(PROJECT, PATH_SEPARATOR + "apache" + PATH_SEPARATOR);
        final File phpDir = new File(PROJECT,  PATH_SEPARATOR + "php" + PATH_SEPARATOR);
        final File serverDir = new File(PROJECT, PATH_SEPARATOR + "mysql" + PATH_SEPARATOR);
        makeDir(apacheDir);
        makeDir(phpDir);
        makeDir(serverDir);
        makeFile(DATA);

        SRVROOT = apacheDir.toString();

        final int apache = 0;
        final int PHP = 1;
        final int mySQL = 2;
        String[] data = extractData();
        String apacheVer = data[apache];
        String PHPVersion = data[PHP];
        String mySQLVersion = data[mySQL];

        BufferedWriter writer = new BufferedWriter(new FileWriter(DATA));

        setupApache(writer, apacheDir, apacheVer);
        setupPHP(writer, phpDir, PHPVersion);
        setupMySQL(writer, serverDir, mySQLVersion);
        setupUnix(writer, data, apacheDir);

        writer.close();
        LOGGER.info("Everything is installed and/or up to date.");

        final File logDir = new File(PROJECT, "logs");
        makeDir(logDir);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String stringDate = LocalDate.now().format(format);
        int index = 1;
        String filename = stringDate + '-' + index + ".txt";
        while (fileExists(logDir.getPath() + filename)) {
            index++;
            filename = stringDate + '-' + index + ".txt";
        }
        File log = new File(logDir, filename);
        makeFile(log);
        LOGGER.setLogFile(log);

        LOGGER.leaving(downloadRequirements);
    }

    public static void setupUnix(BufferedWriter writer, String[] data, File apacheDir)
            throws IOException, ArchiveException {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            final File srcLib = new File(apacheDir, "srcLib");
            makeDir(srcLib);

            final String aprURL = "https://dlcdn.apache.org//apr";
            final String aprUtilURL = "https://dlcdn.apache.org//apr";
            final String PCREURL = "https://github.com/PCRE2Project/pcre2/releases";

            final int apr = 3;
            final int aprUtil = 4;
            final int PCRE = 5;
            final int libTool = 6;
            final int autoconf = 7;
            String aprVersion = data[apr];
            String aprUtilVersion = data[aprUtil];
            String PCREVersion = data[PCRE];
            // GNU versions
            String libtoolVersion = data[libTool];
            String autoconfVersion = data[autoconf];

            setupGNU(writer, libtoolVersion, autoconfVersion);

            Console console = new Console();
            console.directory(apacheDir);
            console.runCommand("./configure --prefix=apache");
            console.runCommand("make");
            console.runCommand("make install");
            console.schedule((console1) -> {
                File conf = new File(apacheDir, "conf/httpd.conf");
                try {
                    configureApacheHttpd(conf, apacheDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static String[] extractData() throws IOException {
        String[] result = new String[8];
        BufferedReader reader = new BufferedReader(new FileReader(DATA));
        String str;
        while ((str = reader.readLine()) != null) {
            StringBuilder builder = new StringBuilder();
            for (char character : str.toCharArray()) {
                if (character == '=') break;
                builder.append(character);
            }
            String value = str.replace(builder + "=", "");
            switch (builder.toString()) {
                case "apache version" -> result[0] = value;
                case "php version" -> result[1] = value;
                case "sql version" -> result[2] = value;
                case "apr version" -> result[3] = value;
                case "apr-util version" -> result[4] = value;
                case "PCRE version" -> result[5] = value;
                case "libtool version" -> result[6] = value;
                case "autoconf version" -> result[7] = value;
            }
        }
        return result;
    }

    public static void setupApache(BufferedWriter writer, File apacheDir, String apacheVer)
            throws IOException, ArchiveException {
        String fileName = getFileNameFromWeb(URI.create("https://dlcdn.apache.org/httpd/").toURL(), "httpd",
                ".tar.gz", new String[]{"TGZ"}, false, true);
        writer.append("apache version=").append(fileName).append(System.lineSeparator());
        if (shouldUpdate(apacheDir, apacheVer, fileName)) {
            clearDirectory(apacheDir);
            if (Util.IS_WINDOWS) {
                BrowserEmulator emulator = new BrowserEmulator();
                emulator.connect(URI.create("https://www.apachelounge.com/download").toURL());
                String windowsVer = "win32";
                if (Util.IS_64_BIT) {
                    windowsVer = "win64";
                }
                Optional<File> optional = emulator.downloadFile("httpd", ".zip", apacheDir,
                        new String[]{windowsVer});
                if (optional.isPresent()) {
                    File apacheZipped = optional.get();
                    File apache = unZip(apacheZipped, apacheDir, "Apache24");
                    File conf = new File(apacheDir, "conf/httpd.conf");
                    configureApacheHttpd(conf, apacheDir);
                }
            } else {
                // https://httpd.apache.org/docs/2.4/install.html read requirements stuff.
                File apacheZipped = downloadFileFromWeb("https://dlcdn.apache.org/httpd", apacheDir,
                        "httpd", ".tar.gz", new String[]{"TGZ"}, false);

                File apacheTar = unGzip(apacheZipped, apacheDir);
                File httpd = unTar(apacheTar, apacheDir, fileName);
            }

            LOGGER.info("Apache installed.");
        }
    }

    public static void setupPHP(BufferedWriter writer, File phpDir, String PHPVersion)
            throws IOException, ArchiveException {
        String phpName = getFileNameFromWeb(URI.create("https://downloads.php.net/~windows/releases/archives").toURL(),
                "php", ".zip", new String[]{"$!php-(\\.?[0-9]+)+-Win32-", "-x64"}, true,
                true);
        writer.append("php version=").append(phpName).append(System.lineSeparator());
        File phpVerDir = new File(phpDir, Util.replaceOther("php-(\\.?[0-9]+)+", "", phpName));
        if (!phpVerDir.exists()) {
            phpVerDir.mkdir();
        }
        if (shouldUpdate(phpDir, PHPVersion, phpName)) {
            if (Util.IS_WINDOWS) {
                File phpZip = downloadFileFromWeb("https://downloads.php.net/~windows/releases/archives", phpDir,
                        "php", ".zip", new String[]{"$!php-(\\.?[0-9]+)+-Win32-", "-x64"}, true);
                File php = unZip(phpZip, phpVerDir);
            } else {
                BrowserEmulator browser = new BrowserEmulator();
                browser.connect(URI.create(
                        "https://www.php.net/downloads.php?usage=web&os=linux&osvariant=linux-debian&version=default&source=Y").toURL());
                Optional<File> optional = browser.downloadFile("php", ".tar.gz",
                        phpDir, null, true);
                if (optional.isPresent()) {
                    File phpGzipped = optional.get();
                    File phpTar = unGzip(phpGzipped, phpDir);
                    File php = unTar(phpTar, phpDir);

                    File config = new File(phpDir, "config");
                    config.mkdir();

                    Console console = new Console();
                    console.directory(phpDir);
                    console.runCommand("./configure --with-config-file-path=/config/");
                    console.runCommand("make");
                }
            }
            LOGGER.info("PHP installed...");
        }
    }

    public static void setupMySQL(BufferedWriter writer, File serverDir, String mySQLVersion)
            throws IOException, ArchiveException {
        String latestVersion = getFileNameFromWeb(URI.create("https://archive.mariadb.org").toURL(),
                "mariadb", "", new String[]{"$!mariadb-(\\.?[0-9]+)+\\/"}, true);
        writer.append("sql version=").append(latestVersion);
        if (shouldUpdate(serverDir, mySQLVersion, latestVersion)) {
            clearDirectory(serverDir);
            URL url = URI.create("https://archive.mariadb.org/" + latestVersion).toURL();
            if(Util.IS_WINDOWS) {
                String mariadbDownloadLocation = getFileNameFromWeb(url, "win", "/", null,
                        false);
                String finalLocation = url.toString() + mariadbDownloadLocation;
                // do not set shouldFilter to true otherwise it crashes
                File mariadbZipped = downloadFileFromWeb(finalLocation, serverDir,
                        latestVersion.replace("/", ""), "64.zip", false);
                File mariadb = unZip(mariadbZipped, serverDir);
            } else {
                String mariadbDownloadLocation = getFileNameFromWeb(url, "bintar-linux-systemd", "/",
                        null, false);
                String finalLocation = url.toString() + mariadbDownloadLocation;
                // do not set shouldFilter to true otherwise it crashes
                File mariadbGzipped = downloadFileFromWeb(finalLocation, serverDir,
                        latestVersion.replace("/", ""), ".tar.gz", new String[]{"linux"},
                        false);
                File mariadbTar = unGzip(mariadbGzipped, serverDir);
                File mariadb = unTar(mariadbTar, serverDir);
            }
            LOGGER.info("MySQL installed...");
        }
    }

    public static void setupGNU(BufferedWriter writer, String libtoolVersion, String autoconfVersion)
            throws IOException, ArchiveException {
        final String autoconfURL = "https://mirror.dogado.de/gnu/autoconf";
        final String libtoolURL = "https://www.artfiles.org/gnu.org/libtool";
        final String gnuM4URL = "https://ftp.gnu.org/gnu/m4/m4-latest.tar.gz";

        final File gnuM4Dir = new File(PROJECT, "gnu-m4");
        final File libtoolDir = new File(PROJECT, "libtool");
        final File autoconfDir = new File(PROJECT, "autoconf");
        makeDir(gnuM4Dir);
        makeDir(libtoolDir);
        makeDir(autoconfDir);

        File current = new File(gnuM4Dir, "m4-latest.tar.gz");
        if (shouldUpdate(gnuM4Dir, current, gnuM4URL)) {
            clearDirectory(gnuM4Dir);
            File gnuM4GZipped = downloadFileFromWeb(gnuM4URL, gnuM4Dir, "m4-latest", ".tar.gz",
                    true);
            File gnuM4TarBall = unGzip(gnuM4GZipped, gnuM4Dir, true);
            File gnuM4 = unTar(gnuM4TarBall, gnuM4Dir);
        }

        String latestAutoconf = getFileNameFromWeb(URI.create(autoconfURL).toURL(), "autoconf",
                ".tar.gz", null, true, true);
        writer.append("autoconf version=").append(latestAutoconf);
        if (shouldUpdate(autoconfDir, autoconfVersion, latestAutoconf)) {
            clearDirectory(autoconfDir);
            File autoconfGZipped = downloadFileFromWeb(autoconfURL, autoconfDir, "autoconf",
                    ".tar.gz", true);
            File autoconfTarBall = unGzip(autoconfGZipped, autoconfDir);
            File autoconf = unTar(autoconfTarBall, autoconfDir);
        }

        String latestLibtool = getFileNameFromWeb(URI.create(libtoolURL).toURL(), "libtool",
                ".tar.gz", null, true);
        if (shouldUpdate(libtoolDir, libtoolVersion, latestLibtool)) {
            clearDirectory(libtoolDir);
            File libtoolGZipped = downloadFileFromWeb(libtoolURL, libtoolDir, "libtool", ".tar.gz",
                    true);
            File libtoolTarBall = unGzip(libtoolGZipped, libtoolDir);
            File libtool = unTar(libtoolTarBall, libtoolDir);
        }
    }

//    public static void setupAPR(BufferedWriter writer, File directory, String versionAPR, String versionUtil)
//            throws IOException, ArchiveException {
//        File aprDirectory = new File(directory, "apr");
//        File aprUtilDirectory = new File(directory, "apr-util");
//
//        String latestAPRVersion = getFileNameFromWeb(URI.create("https://dlcdn.apache.org//apr").toURL(),
//                "apr", ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"},
//                true, true);
//        String latestUtilVersion = getFileNameFromWeb(URI.create("https://dlcdn.apache.org//apr").toURL(),
//                "apr-util", ".tar.gz", new String[]{"TGZ"}, true, true);
//        if (shouldUpdate(aprDirectory, versionAPR, latestAPRVersion)) {
//            clearDirectory(aprDirectory);
//            File aprGZipped = downloadFileFromWeb("https://dlcdn.apache.org//apr", aprDirectory,
//                    "apr", ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"},
//                    true);
//            File aprTarBall = unGzip(aprGZipped, aprDirectory);
//            File apr = unTar(aprTarBall, aprDirectory);
//        }
//    }

    public static boolean shouldUpdate(File directory, String currentVersion, String latestVersion) {
        return !Objects.equals(currentVersion, latestVersion) || directory.listFiles() == null;
    }

    public static boolean shouldUpdate(File directory, File current, String url) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(current));
        StringBuilder builder = new StringBuilder();
        for (String line : reader.lines().toList()) {
            builder.append(line).append(System.lineSeparator());
        }
        reader.close();

        BrowserEmulator emulator = new BrowserEmulator();
        emulator.connect(URI.create(url).toURL());
        Optional<String> optional = emulator.getBody();
        if (optional.isEmpty()) return false;
        String body = optional.get();
        emulator.close();

        boolean emptyCheck = shouldUpdate(directory, "", "");
        return emptyCheck || !body.contentEquals(builder);
    }

    public static File unTar(File infile, File outDir) throws IOException, ArchiveException {
        return unTar(infile, outDir, null);
    }

    public static File unTar(File infile, File outDir, String shouldRemove) throws IOException, ArchiveException {
        LOGGER.info("Untaring " + infile.getPath() + "...");
        InputStream in = new FileInputStream(infile);
        TarArchiveInputStream tarIn = FACTORY.createArchiveInputStream("tar", in);
        TarArchiveEntry entry;
        while ((entry = tarIn.getNextEntry()) != null) {
            LOGGER.info("Moving file " + entry.getName() + "...");
            File outputFile;
            if (shouldRemove == null) {
                outputFile = new File(outDir, entry.getName());
            } else {
                outputFile = new File(outDir, entry.getName().replaceAll(shouldRemove, ""));
            }
            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        FileExistsException e = new FileExistsException(outputFile);
                        LOGGER.error("File already exists." , e);
                        throw e;
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

    public static File unGzip(File infile, File outDir, boolean keepOriginal) throws IOException {
        File outFile = new File(outDir, infile.getName().replaceAll("\\.gz", ""));
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
        FileOutputStream out = new FileOutputStream(outFile);
        IOUtils.copy(in, out);
        in.close();
        out.close();
        if (!keepOriginal) {
            infile.delete();
        }
        return outFile;
    }

    public static File unGzip(File infile, File outDir) throws IOException {
        return unGzip(infile, outDir, false);
    }

    public static File unZip(File infile, File outDir) throws IOException, ArchiveException {
        return unZip(infile, outDir, null);
    }

    public static File unZip(File infile, File outDir, String shouldRemove) throws IOException, ArchiveException {
        LOGGER.info("Unzipping " + infile.getPath() + "...");
        File outFile = new File(outDir, infile.getName().replaceAll("\\.zip", ""));
        byte[] buffer = new byte[1024];
        ZipInputStream in = new ZipInputStream(new FileInputStream(infile));
        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            LOGGER.info("Moving " + entry.getName() + " to " + outDir.getPath() + "...");
            File toMove = getFileFromZipEntry(outDir, entry, shouldRemove);
            if (toMove == null) continue;
            if (entry.isDirectory()) {
                if (!toMove.isDirectory() && !toMove.mkdirs()) {
                    FileExistsException e = new FileExistsException(toMove);
                    LOGGER.error("File already exists.", e);
                    throw e;
                }
            } else {
                File parent = toMove.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    FileExistsException e = new FileExistsException(toMove);
                    LOGGER.error("File already exists.", e);
                    throw e;
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
        LOGGER.info("Clearing " + dir.getPath() + "...");
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File file : arr) {
                LOGGER.info("Deleting file:" + file.getPath() + "...");
                if (file.isDirectory()) {
                    LOGGER.info("File is directory, calling self on file...");
                    clearDirectory(file);
                }
                if (file.delete()) {
                    LOGGER.success("Deleted file. Moving on to next file...");
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

    public static boolean isProperPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean fileExists(String path) {
        File tmp = new File(path);
        return tmp.exists();
    }

    static {
        if (Util.IS_WINDOWS) {
             ROOT = Arrays.stream(File.listRoots()).toList().getFirst();
        } else {
            ROOT = new File("/opt/");
        }
        PROJECT = new File(ROOT, "domainify");
        DATA = new File(PROJECT, "data.txt");
    }
}
