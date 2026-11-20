package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_08_TicketSaleEndDateBeforeStartDateValidationTest extends BaseTest {

    @Test(description = "TC_UC12_08: Kiểm tra validation thời gian kết thúc sự kiện phải diễn ra sau thời gian bắt đầu")
    public void testTicketSaleEndDateBeforeStartDateValidation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        CreateEventPage page = new CreateEventPage(getDriver()).open();
        page.enterBasicInfo(
                "Đêm Nhạc Acoustic Mùa Thu 2026",
                "Mô tả chi tiết về đêm nhạc acoustic mùa thu tại Hà Nội với sự tham gia của nhiều nghệ sĩ.",
                "Hà Nội",
                "500",
                "Trung tâm Hội nghị Quốc gia, Nam Từ Liêm",
                "2026-11-20T20:00",
                "2026-11-20T18:00" // Thời gian kết thúc trước thời gian bắt đầu
        );

        page.clickNextStep();

        Assert.assertTrue(page.isErrorBannerDisplayed(),
                "Hệ thống phải hiển thị cảnh báo lỗi khi thời gian kết thúc trước thời gian bắt đầu");
    }
}

