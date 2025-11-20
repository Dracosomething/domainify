package io.github.dracosomething.util;

import io.github.dracosomething.util.comparator.VersionNameComparator;
import io.github.dracosomething.util.comparator.VersionStringComparator;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    private static boolean UPDATE_HTTPD = false;
    private static boolean UPDATE_PHP = false;

    public static BufferedReader getUrlReader(URL url) throws IOException {
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    public static String getFileNameFromWeb(URL url, String name, String fileExtension, String[] extraData,
                                            boolean shouldFilter, boolean replaceExtension) throws IOException {
        String retVal = getFileNameFromWeb(url, name, fileExtension, extraData, shouldFilter);
        if (replaceExtension) {
            LOGGER.info("Removing file extension.");
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
            if (str.contains(".sig") && !fileExtension.contains(".sig")) continue;
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
            object = list.getLast();
        }
        LOGGER.info("File selected: " + object.getProperty("href"));
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

    public static File downloadFileFromWeb(String urlString, File downloadLocation) throws IOException {
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        ReadableByteChannel channel = Channels.newChannel(url.openStream());
        String name = url.toString().replaceAll("(https|http)://.*\\..*\\..*/.*/", "");
        File download = new File(downloadLocation, "/" + name);
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
        try {
            Method configureApacheHttpd = Util.getClassMethod(FileUtils.class, "configureApacheHttpd", File.class,
                    File.class);
            LOGGER.entering(configureApacheHttpd);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Encountered an error when trying to find configureApacheHttpd(File, File) method " +
                    "in FileUtils class.", e);
        }
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
        try {
            Method configureApacheHttpd = Util.getClassMethod(FileUtils.class, "configureApacheHttpd", File.class,
                    File.class);
            LOGGER.leaving(configureApacheHttpd);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Encountered an error when trying to find configureApacheHttpd(File, File) method " +
                    "in FileUtils class.", e);
        }
    }

    public static void makeDir(File file) {
        if (!file.exists()) {
            LOGGER.info(file.getPath() + " does not yet exist.");
            LOGGER.info("Making " + file.getPath() + '.');
            file.mkdirs();
        } else {
            LOGGER.warn("Directory already exists");
        }
    }

    public static void makeFile(File file) throws IOException {
        if (!file.exists()) {
            LOGGER.info(file.getPath() + " does not yet exist.");
            LOGGER.info("Making " + file.getPath() + '.');
            file.createNewFile();
        } else {
            LOGGER.warn("File already exists");
        }
    }

    // add loading screen.
    /**
     * method sets downloads all required files, and it updates them if necessary.
     *
     * @throws IOException
     * @throws ArchiveException
     */
    public static void downloadRequirements() throws IOException, ArchiveException, NoSuchMethodException, ExecutionException, InterruptedException {
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
        String[] data = extractVersionData();
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
        while (fileExists(logDir.getPath() + "/" + filename)) {
            index++;
            filename = stringDate + '-' + index + ".txt";
        }
        File log = new File(logDir, filename);
        makeFile(log);
        LOGGER.setLogFile(log);

        LOGGER.leaving(downloadRequirements);
    }

    public static void setupUnix(BufferedWriter writer, String[] data, File apacheDir)
            throws IOException, ArchiveException, ExecutionException, InterruptedException {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            final File srcLib = new File(apacheDir, "srclib");

            final int apr = 3;
            final int aprUtil = 4;
            final int PCRE = 5;
            final int libxml = 6;
            final int libsqlite = 7;
            String aprVersion = data[apr];
            String aprUtilVersion = data[aprUtil];
            String PCREVersion = data[PCRE];
            // get http://xmlsoft.org/sources/
            // get https://sqlite.org/$CURRENT_YEAR/
            String libxmlVersion = data[libxml];
            String libsqliteVersion = data[libsqlite];

            setupPCRE(writer, PCREVersion);
            setupAPR(writer, srcLib, aprVersion, aprUtilVersion);

            runSetupCommands();

            /*
            File config = new File(phpDir, "config");
            config.mkdir();

            Console console = new Console();
            console.directory(phpDir);
            console.runCommand("./configure --with-config-file-path=../config/");
            console.runCommand("make");
             */
        }
    }

    public static String[] extractVersionData() throws IOException {
        LOGGER.info("Extracting data from data.txt file.");
        String[] result = new String[9];
        BufferedReader reader = new BufferedReader(new FileReader(DATA));
        String str;

        while ((str = reader.readLine()) != null) {
            StringBuilder builder = new StringBuilder();
            for (char character : str.toCharArray()) {
                if (character == '=') break;
                builder.append(character);
            }
            LOGGER.info("Key is " + builder);

            String value = str.replace(builder + "=", "");
            LOGGER.info("Value is " + value);
            switch (builder.toString()) {
                case "apache version" -> result[0] = value;
                case "php version" -> result[1] = value;
                case "sql version" -> result[2] = value;
                case "apr version" -> result[3] = value;
                case "apr-util version" -> result[4] = value;
                case "PCRE version" -> result[5] = value;
                case "libxml version" -> result[6] = value;
                case "libsqlite version" -> result[7] = value;
            }
            LOGGER.info("Extracted key and value and stored them in the result array.");
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
                String windowsVer = "win64";
                if (!Util.IS_64_BIT) {
                    windowsVer = "win32";
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
                UPDATE_HTTPD = true;
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
        if (shouldUpdate(phpVerDir, PHPVersion, phpName)) {
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
                    UPDATE_PHP = true;
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
        LOGGER.info("Latest mariadb version: " + latestVersion);
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
                String mariadbFileName = getFileNameFromWeb(URI.create(finalLocation).toURL(),
                        latestVersion.replace("/", ""), ".tar.gz",
                        new String[]{"linux"}, false);
                // do not set shouldFilter to true otherwise it crashes
                File mariadbGzipped = downloadFileFromWeb(finalLocation, serverDir,
                        latestVersion.replace("/", ""), ".tar.gz", new String[]{"linux"},
                        false);
                File mariadbTar = unGzip(mariadbGzipped, serverDir);
                File mariadb = unTar(mariadbTar, serverDir, mariadbFileName);
            }
            LOGGER.info("MySQL installed...");
        }
    }

    public static void setupAPR(BufferedWriter writer, File directory, String versionAPR, String versionUtil)
            throws IOException, ArchiveException {
        File aprDirectory = new File(directory, "apr");
        File aprUtilDirectory = new File(directory, "apr-util");
        makeDir(aprDirectory);
        makeDir(aprUtilDirectory);
// https://www.linuxfromscratch.org/blfs/view/svn/general/apr.html for more info on building
        String latestAPRVersion = getFileNameFromWeb(URI.create("https://dlcdn.apache.org//apr").toURL(),
                "apr", ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"},
                true, true);
        String latestUtilVersion = getFileNameFromWeb(URI.create("https://dlcdn.apache.org//apr").toURL(),
                "apr-util", ".tar.gz", new String[]{"TGZ"}, true, true);
        writer.append("apr version=").append(latestAPRVersion).append("\n");
        writer.append("apr-util version=").append(latestUtilVersion).append("\n");
        if (shouldUpdate(aprDirectory, versionAPR, latestAPRVersion)) {
            clearDirectory(aprDirectory);
            File aprGZipped = downloadFileFromWeb("https://dlcdn.apache.org//apr", aprDirectory,
                    "apr", ".tar.gz", new String[]{"TGZ", "apr-(?!util).*", "apr-(?!iconv).*"},
                    true);
            File aprTarBall = unGzip(aprGZipped, aprDirectory);
            File apr = unTar(aprTarBall, aprDirectory, latestAPRVersion);
        }
        if (shouldUpdate(aprUtilDirectory, versionUtil, latestUtilVersion)) {
            clearDirectory(aprUtilDirectory);
            File utilGZipped = downloadFileFromWeb("https://dlcdn.apache.org//apr", aprUtilDirectory,
                    "apr-util", ".tar.gz", new String[]{"TGZ"}, true);
            File utilTarball = unGzip(utilGZipped, aprUtilDirectory);
            File util = unTar(utilTarball, aprUtilDirectory, latestUtilVersion);
        }
    }

    public static void setupPCRE(BufferedWriter writer, String version) throws IOException, ArchiveException {
        final File pcreDir = new File(PROJECT, "PCRE");
        makeDir(pcreDir);

        BrowserEmulator emulator = new BrowserEmulator();

        emulator.connect(URI.create("https://github.com/PCRE2Project/pcre2/releases/latest").toURL());
        String latest = version;
        Optional<List<WebElement>> optionalElements = emulator.getElements(By.tagName("include-fragment"));
        if (optionalElements.isPresent()) {
            List<WebElement> elements = optionalElements.get();
            elements = elements.stream().filter((element) -> {
                String src = element.getAttribute("src");
                if (src == null) return false;
                return Util.containsIgnoreCase(src, "https://github.com/PCRE2Project/pcre2/releases/expanded_assets/");
            }).toList();
            String url = elements.getFirst().getAttribute("src");
            if (url != null) {
                emulator.connect(URI.create(url).toURL());
                latest = url.replaceAll("(https|http)://.*\\..*\\..*/.*/", "");
                writer.append("PCRE version=").append(latest).append("\n");

                if (shouldUpdate(pcreDir, version, latest)) {
                    clearDirectory(pcreDir);
                    Optional<File> optional = emulator.downloadFile("pcre2-", ".tar.gz", pcreDir, null);
                    if (optional.isPresent()) {
                        File pcreGZipped = optional.get();
                        String name = pcreGZipped.getName().replaceAll(".tar.gz", "");
                        File pcreTarBall = unGzip(pcreGZipped, pcreDir);
                        File pcre = unTar(pcreTarBall, pcreDir, name);
                    }
                }
            }
        }
    }

    public static void runSetupCommands() {
      if (UPDATE_HTTPD) {
        final File pcre = new File(PROJECT, "PCRE");
        final File docs = new File(pcre, "/doc");
        makeDir(docs);
        final File httpd = new File(PROJECT, "apache");
        final File srclib = new File(httpd, "srclib");
        Console console = new Console();
        console.directory(pcre);
        console.queue("chmod +x ./configure");
        console.queue("./configure --prefix=" + pcre + " --docdir=" + docs + " --disable-static");
        console.queue("make");
        console.queue("make install");

        console.queueAndSchedule("chmod +x ./configure", (shell) -> {
            shell.directory(httpd);
        });

        console.queueAndSchedule("chmod +x */build/*", (shell) -> {
            shell.directory(srclib);
        });

        console.queueAndSchedule("./configure --prefix=" + httpd + " --with-included-apr --with-pcre=" + pcre + "/lib", (shell) -> {
            shell.directory(httpd);
        });
        console.queue("make");
        console.queue("make install");
        console.start();
      }
    }

    public static boolean shouldUpdate(File directory, String currentVersion, String latestVersion) {
        if (directory.listFiles() == null || directory.listFiles().length < 1) return true;
        return !Objects.equals(currentVersion, latestVersion);
    }

    public static boolean shouldUpdate(File directory, File current, String url) throws IOException {
        if (!directory.exists())
            return false;
        if (!current.exists())
            return true;
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
        LOGGER.info(shouldRemove);
        InputStream in = new FileInputStream(infile);
        TarArchiveInputStream tarIn = FACTORY.createArchiveInputStream("tar", in);
        TarArchiveEntry entry;
        while ((entry = tarIn.getNextEntry()) != null) {
            File outputFile;
            if (shouldRemove == null) {
                outputFile = new File(outDir, entry.getName());
            } else {
                outputFile = new File(outDir, entry.getName().replaceAll(shouldRemove, ""));
            }
            LOGGER.info("Moving file " + entry.getName() + " to " + outputFile.getPath() + "...");
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

    public static void moveDirectoryContent(String directoryPath, File target, boolean shouldRemoveDirectory)
            throws IOException {
        File directory = new File(directoryPath);
        moveDirectoryContent(directory, target, shouldRemoveDirectory);
    }

    public static void moveDirectoryContent(File directory, File target, boolean shouldRemoveDirectory)
            throws IOException {
        if (!directory.isDirectory())
            return;
        if (directory.listFiles() != null && directory.listFiles().length < 1)
            return;
        for (File file : directory.listFiles()) {
            File newFile = new File(target, file.getName());
            if (file.isDirectory()) {
                makeDir(newFile);
                if (newFile.exists()) continue;
                org.apache.commons.io.FileUtils.moveDirectoryToDirectory(newFile, target, false);
            } else {
                if (newFile.exists()) continue;
                org.apache.commons.io.FileUtils.moveToDirectory(file, target, false);
            }
        }
        if (shouldRemoveDirectory) {
            directory.delete();
        }
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
