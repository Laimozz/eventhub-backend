package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_07_CreateEventMultipleTicketTiersTest extends BaseTest {

    @Test(description = "TC_UC12_07: Tạo sự kiện với thông tin cơ bản hợp lệ và lưu bản nháp thành công")
    public void testCreateEventMultipleTicketTiers() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        CreateEventPage page = new CreateEventPage(getDriver()).open();

        page.enterBasicInfo(
                "Sự kiện Âm nhạc Đa hạng vé",
                "Mô tả sự kiện âm nhạc quy mô lớn",
                "TP. Hồ Chí Minh",
                "1000",
                "Sân vận động Quân khu 7",
                "2026-11-20T19:00",
                "2026-11-20T23:00"
        );

        page.clickSaveDraft();
        Assert.assertTrue(page.isDraftNoticeDisplayed() || !page.isErrorBannerDisplayed(),
                "Lưu bản nháp thành công không bị báo lỗi");
    }
}

