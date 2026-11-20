package com.eventhub.automation.driver;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public class DriverManager {
    static {
        // Tắt cảnh báo CDP mismatch từ Selenium do Chrome version mới hơn version CDP đóng gói trong thư viện
        java.util.logging.Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver").setLevel(java.util.logging.Level.OFF);
        System.setProperty("webdriver.chrome.silentOutput", "true");
    }

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void initDriver() {
        if (getDriver() == null) {
            String browser = ConfigurationManager.getBrowser();
            boolean headless = ConfigurationManager.isHeadless();
            WebDriver driver;

            if ("edge".equals(browser)) {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--start-maximized");
                if (headless) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                }
                driver = new EdgeDriver(options);
            } else {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--start-maximized");
                options.addArguments("--disable-notifications");
                options.addArguments("--remote-allow-origins=*");
                if (headless) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                }
                driver = new ChromeDriver(options);
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigurationManager.getImplicitWait()));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            setDriver(driver);
        }
    }

    public static void quitDriver() {
        WebDriver driver = getDriver();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}

