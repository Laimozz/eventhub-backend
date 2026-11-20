package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AiChatbotComponent extends BasePage {

    // Launcher Floating Button
    private final By launcherButton = By.cssSelector("button[class*='launcher'], button[aria-controls='eventhub-ai']");
    private final By launcherText = By.xpath("//button[contains(@class, 'launcher')]//span[contains(text(), 'Hỏi EventHub AI')]");

    // Chatbot Dialog Panel
    private final By aiPanel = By.id("eventhub-ai");
    private final By panelTitle = By.id("eventhub-ai-title");
    private final By scopeLabel = By.cssSelector("div[class*='scope']");

    // Header Action Buttons
    private final By btnNewChat = By.cssSelector("button[aria-label='Hội thoại mới']");
    private final By btnHistory = By.cssSelector("button[aria-label='Lịch sử hội thoại']");
    private final By btnExpand = By.cssSelector("button[aria-label='Mở rộng'], button[aria-label='Thu nhỏ']");
    private final By btnClose = By.cssSelector("button[aria-label='Đóng trợ lý']");

    // Welcome Screen & Quick Prompts
    private final By welcomeBox = By.cssSelector("div[class*='welcome']");
    private final By welcomeTitle = By.cssSelector("div[class*='welcome'] h3");
    private final By promptButtons = By.cssSelector("div[class*='prompts'] button");

    // Messages Area
    private final By messagesContainer = By.cssSelector("div[class*='messages']");
    private final By userMessages = By.cssSelector("article[class*='userMessage']");
    private final By assistantMessages = By.cssSelector("article[class*='assistantMessage']");
    private final By thinkingIndicator = By.cssSelector("div[class*='thinking']");

    // Composer Input & Send Button
    private final By questionTextarea = By.id("eventhub-ai-question");
    private final By btnSend = By.cssSelector("button[aria-label='Gửi câu hỏi']");

    // History View
    private final By historyView = By.cssSelector("div[class*='history']");
    private final By btnBackToChat = By.cssSelector("button[aria-label='Quay lại trò chuyện']");
    private final By historyItems = By.cssSelector("div[class*='historyItem']");

    public AiChatbotComponent(WebDriver driver) {
        super(driver);
    }

    public boolean isLauncherDisplayed() {
        return isDisplayed(launcherButton);
    }

    public void openChatbot() {
        if (!isPanelDisplayed()) {
            click(launcherButton);
            waitForVisibility(aiPanel);
        }
    }

    public void closeChatbot() {
        if (isPanelDisplayed()) {
            click(btnClose);
            waitForInvisibility(aiPanel);
        }
    }

    public void closeByEscape() {
        if (isPanelDisplayed()) {
            WebElement panel = driver.findElement(aiPanel);
            panel.sendKeys(Keys.ESCAPE);
            waitForInvisibility(aiPanel);
        }
    }

    public boolean isPanelDisplayed() {
        return isDisplayed(aiPanel);
    }

    public String getPanelTitle() {
        return getText(panelTitle);
    }

    public String getScopeLabel() {
        return getText(scopeLabel);
    }

    public boolean isWelcomeDisplayed() {
        return isDisplayed(welcomeBox);
    }

    public String getWelcomeTitle() {
        return getText(welcomeTitle);
    }

    public int getPromptButtonsCount() {
        return driver.findElements(promptButtons).size();
    }

    public void clickPromptByIndex(int index) {
        List<WebElement> prompts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(promptButtons));
        if (index >= 0 && index < prompts.size()) {
            WebElement prompt = prompts.get(index);
            highlightElement(prompt);
            try {
                prompt.click();
            } catch (Exception e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", prompt);
            }
            delay();
        }
    }

    public void enterQuestion(String question) {
        type(questionTextarea, question);
    }

    public void clickSend() {
        click(btnSend);
    }

    public boolean isSendButtonEnabled() {
        try {
            WebElement btn = driver.findElement(btnSend);
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String getQuestionInputText() {
        try {
            WebElement textarea = driver.findElement(questionTextarea);
            return textarea.getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isUserMessageDisplayed() {
        return isDisplayed(userMessages);
    }

    public WebElement waitForUserMessage() {
        return waitForVisibility(userMessages);
    }

    public WebElement waitForAssistantMessage() {
        WebDriverWait aiWait = new WebDriverWait(driver, Duration.ofSeconds(30), Duration.ofMillis(300));
        return aiWait.until(ExpectedConditions.visibilityOfElementLocated(assistantMessages));
    }

    public boolean isAssistantMessageDisplayed() {
        return isDisplayed(assistantMessages);
    }

    public String getLastAssistantMessageText() {
        List<WebElement> list = driver.findElements(assistantMessages);
        if (list.isEmpty()) return "";
        WebElement last = list.get(list.size() - 1);
        highlightElement(last);
        return last.getText().trim();
    }

    public String getLastUserMessageText() {
        List<WebElement> list = driver.findElements(userMessages);
        if (list.isEmpty()) return "";
        return list.get(list.size() - 1).getText().trim();
    }

    public boolean isThinkingDisplayed() {
        return isDisplayed(thinkingIndicator);
    }

    public void clickNewChat() {
        click(btnNewChat);
        waitForVisibility(welcomeBox);
    }

    public void openHistory() {
        click(btnHistory);
        waitForVisibility(historyView);
    }

    public boolean isHistoryDisplayed() {
        return isDisplayed(historyView);
    }

    public void clickBackToChat() {
        click(btnBackToChat);
        waitForInvisibility(historyView);
    }

    public void toggleExpand() {
        click(btnExpand);
    }

    public boolean isExpanded() {
        try {
            WebElement panel = driver.findElement(aiPanel);
            String className = panel.getAttribute("class");
            return className != null && className.contains("expanded");
        } catch (Exception e) {
            return false;
        }
    }
}

