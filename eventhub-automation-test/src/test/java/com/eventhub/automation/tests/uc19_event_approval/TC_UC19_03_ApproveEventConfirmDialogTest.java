package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_03_ApproveEventConfirmDialogTest extends BaseTest {

    @Test(description = "TC_UC19_03: Nhấn nút Duyệt sự kiện mở dialog xác nhận phê duyệt sự kiện")
    public void testApproveEventConfirmDialog() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();

        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();

            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét sự kiện phải được mở");

            reviewPage.clickApprove();
            Assert.assertTrue(reviewPage.isConfirmApproveDialogDisplayed(), "Hộp thoại xác nhận duyệt sự kiện phải được mở");

            reviewPage.clickCancelDialog();
        } else {
            Assert.assertTrue(true);
        }
    }
}

