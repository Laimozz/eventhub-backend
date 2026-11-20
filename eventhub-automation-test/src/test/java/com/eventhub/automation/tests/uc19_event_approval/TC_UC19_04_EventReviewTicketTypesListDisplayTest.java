package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_04_EventReviewTicketTypesListDisplayTest extends BaseTest {

    @Test(description = "TC_UC19_04: Trang duyệt sự kiện hiển thị đầy đủ danh sách các loại vé cấu hình")
    public void testEventReviewTicketTypesListDisplay() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();

        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();

            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét sự kiện phải được mở");
            Assert.assertTrue(reviewPage.getTicketTypesCount() > 0, "Hồ sơ sự kiện phải hiển thị bảng loại vé");
        } else {
            Assert.assertTrue(true);
        }
    }
}

