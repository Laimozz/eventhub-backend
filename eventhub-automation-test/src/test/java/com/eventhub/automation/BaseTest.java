package com.eventhub.automation;

import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.driver.DriverManager;
import com.eventhub.automation.utils.DatabaseCleaner;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseTest {

    private DatabaseCleaner.Snapshot testSnapshot;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        if (ConfigurationManager.isDbRollbackEnabled()) {
            DatabaseCleaner.resetUserPasswords();
            testSnapshot = DatabaseCleaner.takeSnapshot();
        }
        DriverManager.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                captureScreenshot(result.getName());
            }
            
            // Tạm dừng trước khi đóng trình duyệt để người kiểm thử nhìn rõ kết quả cuối cùng
            int delay = ConfigurationManager.getStepDelay();
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            DriverManager.quitDriver();
            // Tự động rollback dữ liệu phát sinh trong test case về trạng thái trước test
            if (ConfigurationManager.isDbRollbackEnabled() && testSnapshot != null) {
                DatabaseCleaner.rollbackToSnapshot(testSnapshot);
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (ConfigurationManager.isDbRollbackEnabled()) {
            DatabaseCleaner.cleanAllTestData();
        }
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    private void captureScreenshot(String testName) {
        try {
            WebDriver driver = getDriver();
            if (driver instanceof TakesScreenshot) {
                File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path screenshotsDir = Paths.get("target", "screenshots");
                Files.createDirectories(screenshotsDir);
                Path destPath = screenshotsDir.resolve(testName + "_" + timestamp + ".png");
                Files.copy(srcFile.toPath(), destPath);
                System.out.println("Screenshot captured on failure: " + destPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Could not capture screenshot: " + e.getMessage());
        }
    }
}
