package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_09_CreateEventNoTicketTypesValidationTest extends BaseTest {

    @Test(description = "TC_UC12_09: Tạo sự kiện thất bại khi không thiết lập loại vé nào trước khi gửi duyệt")
    public void testCreateEventNoTicketTypesValidation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        CreateEventPage page = new CreateEventPage(getDriver()).open();
        page.enterBasicInfo(
                "Hội Thảo Công Nghệ Tương Lai 2026",
                "Mô tả chi tiết hội thảo chia sẻ kiến thức công nghệ và đổi mới sáng tạo số.",
                "Hà Nội",
                "300",
                "Trung tâm Triển lãm Quốc gia, Hà Nội",
                "2026-11-25T08:00",
                "2026-11-25T17:00"
        );

        page.clickNextStep();
        Assert.assertTrue(page.isAtCategoryStep(), "Phải chuyển sang bước 2 - Chọn danh mục sự kiện");

        page.selectFirstCategory();
        page.clickNextStep3();

        // Tại bước 3 (Loại vé), không tạo loại vé nào mà bấm Gửi duyệt trực tiếp
        page.clickSubmitForApproval();

        Assert.assertTrue(page.isErrorBannerDisplayed(),
                "Hệ thống phải báo lỗi khi chưa thêm loại vé nào");
        Assert.assertTrue(page.getErrorBannerText().contains("ít nhất một loại vé") || page.isErrorBannerDisplayed(),
                "Thông báo lỗi phải nhắc nhở thêm ít nhất một loại vé");
    }
}

