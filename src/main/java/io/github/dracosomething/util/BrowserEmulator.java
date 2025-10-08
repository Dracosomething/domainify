package io.github.dracosomething.util;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverInfo;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverInfo;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverInfo;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverInfo;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverInfo;
import org.openqa.selenium.safari.SafariOptions;

import java.awt.*;
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

    private WebDriver getBrowser() {
        WebDriverInfo info;
        info = new GeckoDriverInfo();
        if (info.isPresent() && info.isAvailable()) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless=new");
            return new FirefoxDriver(options);
        }
        info = new ChromeDriverInfo();
        if (info.isPresent() && info.isAvailable()) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            return new ChromeDriver();
        }
        info = new EdgeDriverInfo();
        if (info.isPresent() && info.isAvailable()) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--headless=new");
            return new EdgeDriver(options);
        }
        info = new InternetExplorerDriverInfo();
        if (info.isPresent() && info.isAvailable()) {
            InternetExplorerOptions options = new InternetExplorerOptions();
            options.setCapability("headless", true);
            return new InternetExplorerDriver(options);
        }
        info = new SafariDriverInfo();
        if (info.isPresent() && info.isAvailable()) {
            SafariOptions options = new SafariOptions();
            options.setCapability("headless", true);
            return new SafariDriver(options);
        }
        return null;
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

    public Optional<File> downloadFile() {
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
