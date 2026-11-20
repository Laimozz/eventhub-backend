package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CreateEventPage extends BasePage {

    private final By pageTitle = By.xpath("//h1[contains(text(), 'Tạo sự kiện mới')]");
    private final By nameInput = By.id("name");
    private final By descriptionInput = By.id("description");
    private final By cityInput = By.id("city");
    private final By capacityInput = By.id("capacity");
    private final By addressInput = By.id("address");
    private final By startTimeInput = By.id("startTime");
    private final By endTimeInput = By.id("endTime");
    private final By btnNextStep = By.xpath("//button[contains(., 'Tiếp tục: Bước 2')]");
    private final By errorBanner = By.cssSelector("p[role='alert'], p[class*='errorBanner']");
    private final By categoryStepTitle = By.xpath("//h2[contains(text(), 'Danh mục sự kiện')]");
    private final By btnSaveDraft = By.xpath("//button[contains(., 'Lưu bản nháp')]");
    private final By btnPreview = By.xpath("//button[contains(., 'Xem trước')]");
    private final By draftNotice = By.cssSelector("p[role='status'], p[class*='notice']");
    private final By previewDialog = By.xpath("//h2[contains(text(), 'Xem trước sự kiện')] | //div[contains(@class, 'dialog')]");
    private final By categorySelect = By.id("categoryId");
    private final By btnNextStep3 = By.xpath("//button[contains(., 'Tiếp tục: Bước 3')]");
    private final By btnSubmitForApproval = By.xpath("//button[contains(., 'Gửi Admin xét duyệt') or contains(., 'Gửi duyệt')]");

    public CreateEventPage(WebDriver driver) {
        super(driver);
    }

    public CreateEventPage open() {
        open(ConfigurationManager.getBaseUrl() + "/organizer/events/new/details");
        waitForVisibility(pageTitle);
        return this;
    }

    public void enterBasicInfo(String name, String desc, String city, String capacity, String address, String startTime, String endTime) {
        type(nameInput, name);
        type(descriptionInput, desc);
        type(cityInput, city);
        type(capacityInput, capacity);
        type(addressInput, address);
        type(startTimeInput, startTime);
        type(endTimeInput, endTime);
    }

    public void enterName(String name) {
        type(nameInput, name);
    }

    public void enterTimeRange(String startTime, String endTime) {
        type(startTimeInput, startTime);
        type(endTimeInput, endTime);
    }

    public void clickNextStep() {
        click(btnNextStep);
    }

    public boolean isErrorBannerDisplayed() {
        return isDisplayed(errorBanner);
    }

    public String getErrorBannerText() {
        return getText(errorBanner);
    }

    public boolean isAtCategoryStep() {
        return isDisplayed(categoryStepTitle);
    }

    public void clickSaveDraft() {
        click(btnSaveDraft);
    }

    public boolean isDraftNoticeDisplayed() {
        return isDisplayed(draftNotice);
    }

    public void clickPreview() {
        click(btnPreview);
    }

    public boolean isPreviewModalDisplayed() {
        return isDisplayed(previewDialog);
    }

    public void selectFirstCategory() {
        WebElement sel = waitForVisibility(categorySelect);
        highlightElement(sel);
        org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(sel);
        if (dropdown.getOptions().size() > 1) {
            dropdown.selectByIndex(1);
        }
        delay();
    }

    public void clickNextStep3() {
        click(btnNextStep3);
    }

    public void clickSubmitForApproval() {
        click(btnSubmitForApproval);
    }
}

