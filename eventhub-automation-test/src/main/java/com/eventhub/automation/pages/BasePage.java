package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigurationManager.getExplicitWait()), Duration.ofMillis(200));
    }

    public void delay() {
        int delayMs = ConfigurationManager.getStepDelay();
        if (delayMs > 0) {
            sleep(delayMs);
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Làm nổi bật (highlight) phần tử trên màn hình bằng viền đỏ để người kiểm thử dễ quan sát
     */
    public void highlightElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.border='3px solid #ff4d4f'; arguments[0].style.outline='2px solid #ffa39e';",
                    element
            );
        } catch (Exception ignored) {
        }
    }

    public void open(String url) {
        driver.get(url);
        delay();
    }

    public WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void click(By locator) {
        WebElement element = waitForClickable(locator);
        highlightElement(element);
        try {
            element.click();
        } catch (org.openqa.selenium.WebDriverException e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'}); arguments[0].click();", element);
        }
        delay();
    }

    public void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        highlightElement(element);
        element.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"), org.openqa.selenium.Keys.BACK_SPACE);
        element.clear();
        if (text != null) {
            element.sendKeys(text);
        }
        delay();
    }

    public String getText(By locator) {
        WebElement element = waitForVisibility(locator);
        highlightElement(element);
        return element.getText().trim();
    }

    public boolean isDisplayed(By locator) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            java.util.List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                return false;
            }
            WebElement element = elements.get(0);
            if (element.isDisplayed()) {
                highlightElement(element);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigurationManager.getImplicitWait()));
        }
    }

    public boolean waitForInvisibility(By locator) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigurationManager.getImplicitWait()));
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        delay();
    }
}
