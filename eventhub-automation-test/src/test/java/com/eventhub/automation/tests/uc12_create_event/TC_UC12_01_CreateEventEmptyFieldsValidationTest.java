package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_01_CreateEventEmptyFieldsValidationTest extends BaseTest {

    @Test(description = "TC_UC12_01: Bỏ trống các trường bắt buộc ở bước 1 và bấm Tiếp tục phải hiển thị cảnh báo lỗi")
    public void testCreateEventEmptyFieldsValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        CreateEventPage createEventPage = new CreateEventPage(getDriver()).open();

        // Không điền gì và bấm Tiếp tục: Bước 2
        createEventPage.clickNextStep();

        Assert.assertTrue(createEventPage.isErrorBannerDisplayed(), "Hệ thống phải hiển thị cảnh báo lỗi khi để trống thông tin bắt buộc");
        Assert.assertTrue(createEventPage.getErrorBannerText().contains("Vui lòng kiểm tra các thông tin"),
                "Nội dung thông báo lỗi phải nhắc nhở kiểm tra thông tin. Thực tế: " + createEventPage.getErrorBannerText());
    }
}

