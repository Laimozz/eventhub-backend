package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_04_CreateEventDescriptionLengthLimitTest extends BaseTest {

    @Test(description = "TC_UC12_04: Kiểm tra giới hạn độ dài ký tự tối đa (255 ký tự) của trường Tên sự kiện")
    public void testCreateEventDescriptionLengthLimit() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        CreateEventPage createEventPage = new CreateEventPage(getDriver()).open();

        // Chuỗi 300 ký tự
        String longText = "A".repeat(300);
        createEventPage.enterName(longText);

        // Trường input có thuộc tính maxLength=255 nên trình duyệt chỉ giữ 255 ký tự
        String actualValue = getDriver().findElement(org.openqa.selenium.By.id("name")).getAttribute("value");
        Assert.assertTrue(actualValue.length() <= 255, "Tên sự kiện không được vượt quá 255 ký tự");
    }
}

