package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_02_CreateEventStep1InvalidTimeRangeTest extends BaseTest {

    @Test(description = "TC_UC12_02: Tạo sự kiện với thời gian kết thúc trước thời gian bắt đầu bị báo lỗi")
    public void testCreateEventStep1InvalidTimeRange() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        CreateEventPage createEventPage = new CreateEventPage(getDriver()).open();

        createEventPage.enterBasicInfo(
                "Sự kiện Test Thời Gian",
                "Mô tả sự kiện kiểm thử",
                "Hà Nội",
                "1000",
                "Số 1 Tràng Tiền",
                "2027-12-01T20:00", // Start: 2027-12-01
                "2027-11-01T20:00"  // End: 2027-11-01 (trước start!)
        );

        createEventPage.clickNextStep();

        Assert.assertTrue(createEventPage.isErrorBannerDisplayed(),
                "Hệ thống phải hiển thị thông báo lỗi khi thời gian kết thúc trước thời gian bắt đầu");
    }
}

