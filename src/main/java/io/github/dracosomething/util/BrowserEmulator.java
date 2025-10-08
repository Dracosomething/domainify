package io.github.dracosomething.util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.Browser;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class BrowserEmulator {
    private WebDriver driver;
    private boolean isActive;
    private URL url;

    public BrowserEmulator() {
        driver = null;
        isActive = false;
    }

    private WebDriver getBrowser(File downloadPath) {
        FirefoxOptions options = new FirefoxOptions() {
            @Override
            public String getBrowserName() {
                return Browser.HTMLUNIT.browserName();
            }
        };
        options.setCapability("javascriptEnabled", true);
        if (downloadPath != null) {
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.download.dir", downloadPath.toString());
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/csv, text/csv, text/plain,application/octet-stream doc xls pdf txt");
        }
        this.driver = new HtmlUnitDriver(options);
        return null;
    }

    private WebDriver getBrowser() {
        return this.getBrowser(null);
    }

    public void open(URL url) {
        this.driver.get(url.toString());
        this.url = url;
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

    public Optional<File> downloadFile(File downloadDestination) {
        if (this.url != null) {

        }
        return Optional.empty();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
