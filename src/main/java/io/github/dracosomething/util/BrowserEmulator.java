package io.github.dracosomething.util;

import com.gargoylesoftware.htmlunit.*;
import io.github.dracosomething.util.comparator.VersionWebElementComparator;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.http.HttpRequest;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.dracosomething.Main.LOGGER;

@SuppressWarnings("deprecated")
public class BrowserEmulator {
    private HtmlUnitDriver driver;
    private boolean isActive;
    private URL url;

    public BrowserEmulator() {
        driver = null;
        isActive = false;
    }

    private HtmlUnitDriver getBrowser(File downloadPath) {
        FirefoxOptions options = new FirefoxOptions() {
            @Override
            public String getBrowserName() {
                return Browser.HTMLUNIT.browserName();
            }

            @Override
            public String getBrowserVersion() {
                return "firefox-" + BrowserVersion.FIREFOX.getBrowserVersionNumeric();
            }
        };
        options.setCapability("javascriptEnabled", false);
        if (downloadPath != null) {
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.dir", downloadPath.toString());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/csv, text/csv, text/plain,application/octet-stream doc xls pdf txt zip");
            options.setProfile(profile);
        }
        this.driver = new HtmlUnitDriver(options);
        return this.driver;
    }

    private HtmlUnitDriver getBrowser() {
        return this.getBrowser(null);
    }

    public void connect(URL url) {
        if (this.activate()) {
            this.driver.get(url.toString());
            this.url = url;
        } else {
            LOGGER.warn("Already active, closing now...");
            this.close();
            if (this.url == null) {
                try {
                    Method close = this.getClass().getMethod("connect", URL.class);
                    LOGGER.success("Succesfully closed connection.", close);
                } catch (NoSuchMethodException e) {
                    LOGGER.error("Failed to find connect method", e);
                }
            }
            this.connect(url);
        }
    }

    public Optional<WebElement> getDownloadLocation(String fileName, String fileExtension) {
        return getDownloadLocation(fileName, fileExtension, null, false);
    }

    public Optional<WebElement> getDownloadLocation(String fileName, String fileExtension, String[] extraData) {
        return getDownloadLocation(fileName, fileExtension, extraData, false);
    }

    public Optional<WebElement> getDownloadLocation(String fileName, String fileExtension, String[] extraData,
                                                    boolean shouldFilter) {
        if (this.url != null) {
            String regex = null;
            if (extraData != null) {
                regex = Util.formatArrayToRegex(extraData);
            }
            Optional<List<WebElement>> optionalLinks = this.getElements(By.tagName("a"));
            List<WebElement> elements = new ArrayList<>();
            if (optionalLinks.isPresent()) {
                List<WebElement> links = optionalLinks.get();
                for (WebElement element : links) {
                    String text = element.getText();
                    if (!text.startsWith(fileName)) continue;
                    if (!text.endsWith(fileExtension)) continue;
                    if (extraData != null && !text.matches(regex)) continue;
                    elements.add(element);
                }
            }
            if (!elements.isEmpty()) {
                if (shouldFilter) {
                    elements = elements.stream().sorted(new VersionWebElementComparator(fileName, fileExtension)).toList();
                }
                return Optional.of(elements.getLast());
            }
        }
        return Optional.empty();
    }

    public Optional<File> downloadFile(String fileName, String fileExtension, File downloadLocation,
                                       String[] extraData, boolean shouldFilter) throws IOException {
        if (this.url != null) {
            Optional<WebElement> optional = getDownloadLocation(fileName, fileExtension, extraData, shouldFilter);
            if (optional.isPresent()) {
                WebElement element = optional.get();
                String url = this.url.toString();
                String append = element.getAttribute("href");
                if (append != null) {
                    url = append;
                }
                return downloadFile(URI.create(url).toURL(), downloadLocation);
            }
        }
        return Optional.empty();
    }

    public Optional<List<WebElement>> getElements(By by) {
        if (this.url != null) {
            return Optional.of(this.driver.findElements(by));
        }
        return Optional.empty();
    }

    public boolean activate() {
        if (!this.isActive) {
            if (getBrowser() != null) {
                this.driver = getBrowser();
                this.isActive = true;
            }
        }
        return this.isActive;
    }

    public boolean close() {
        if (this.isActive) {
            this.driver.close();
            this.driver.quit();
            this.isActive = false;
            this.driver = null;
        }
        return this.isActive;
    }

    public Optional<File> downloadFile(URL url, File downloadDestination) throws IOException {
        if (this.url != null) {
            this.driver = getBrowser(downloadDestination);
            this.connect(url);

            String name = url.toString().replaceAll("(https|http)://.*\\..*/.*/", "");
            File file = new File(downloadDestination, name);

            WebWindow window = this.driver.getCurrentWindow().getWebWindow();
            UnexpectedPage page = (UnexpectedPage) window.getEnclosedPage();

            ReadableByteChannel channel = Channels.newChannel(page.getInputStream());
            FileOutputStream outputStream = new FileOutputStream(file);
            FileChannel fileChannel = outputStream.getChannel();
            fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
            outputStream.close();

            return Optional.of(file);
        }
        return Optional.empty();
    }

    public Optional<File> downloadFile(String fileName, String fileExtension, File downloadLocation, String[] extraData) throws IOException {
        return downloadFile(fileName, fileExtension, downloadLocation, extraData, false);
    }

    public Optional<String> getBody() {
        if (this.url != null) {
            String body = this.driver.getPageSource();
            return Optional.of(body);
        }
        return Optional.empty();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
