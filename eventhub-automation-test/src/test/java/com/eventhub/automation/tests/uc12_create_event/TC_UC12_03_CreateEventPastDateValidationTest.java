package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_03_CreateEventPastDateValidationTest extends BaseTest {

    @Test(description = "TC_UC12_03: Tạo sự kiện thất bại khi chọn thời gian sự kiện trong quá khứ")
    public void testCreateEventPastDateValidation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        CreateEventPage page = new CreateEventPage(getDriver()).open();

        // Thời gian trong quá khứ
        String startTime = "2020-01-01T08:00";
        String endTime = "2020-01-01T12:00";

        page.enterBasicInfo(
                "Sự kiện kiểm thử thời gian quá khứ",
                "Mô tả chi tiết sự kiện quá khứ",
                "Hà Nội",
                "500",
                "Trung tâm Hội nghị Quốc gia",
                startTime,
                endTime
        );
        page.clickNextStep();

        // Hệ thống phải chặn và báo lỗi
        Assert.assertTrue(!page.isAtCategoryStep() || page.isErrorBannerDisplayed(),
                "Hệ thống phải chặn khi thời gian sự kiện nằm trong quá khứ");
    }
}

