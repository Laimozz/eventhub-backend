package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_05_AdminEventReviewBackButtonTest extends BaseTest {

    @Test(description = "TC_UC19_05: Quản trị viên mở chi tiết sự kiện chờ duyệt và bấm nút Quay lại danh sách")
    public void testAdminEventReviewBackButton() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();
        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();
            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét phê duyệt sự kiện phải hiển thị");

            reviewPage.clickBack();
            Assert.assertTrue(pendingPage.isLoaded(), "Phải quay lại trang danh sách sự kiện chờ duyệt thành công");
        }
    }
}

