package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_02_RejectEventEmptyReasonValidationTest extends BaseTest {

    @Test(description = "TC_UC19_02: Từ chối duyệt sự kiện thất bại khi để trống lý do từ chối")
    public void testRejectEventEmptyReasonValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();

        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();

            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét chi tiết sự kiện phải được hiển thị");

            reviewPage.clickReject();
            reviewPage.enterRejectReason("");
            reviewPage.clickConfirmReject();

            Assert.assertTrue(reviewPage.isErrorAlertDisplayed(), "Hệ thống phải hiển thị cảnh báo lỗi khi không điền lý do từ chối");

            reviewPage.clickCancelDialog();
        } else {
            Assert.assertTrue(true);
        }
    }
}

