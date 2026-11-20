package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_05_SaveDraftNotificationTest extends BaseTest {

    @Test(description = "TC_UC12_05: Nhấn 'Lưu bản nháp' khi tạo sự kiện hiển thị thông báo đã lưu thành công vào trình duyệt")
    public void testSaveDraftNotification() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        CreateEventPage createEventPage = new CreateEventPage(getDriver()).open();
        createEventPage.enterName("Sự kiện bản nháp thử nghiệm");
        createEventPage.clickSaveDraft();

        Assert.assertTrue(createEventPage.isDraftNoticeDisplayed(),
                "Hệ thống phải hiển thị thông báo đã lưu bản nháp trên trình duyệt");
    }
}

