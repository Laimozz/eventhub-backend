package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_06_RejectEventSuccessTest extends BaseTest {

    @Test(description = "TC_UC19_06: Quản trị viên từ chối duyệt sự kiện thành công khi nhập đầy đủ lý do từ chối")
    public void testRejectEventSuccess() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();
        Assert.assertTrue(pendingPage.isLoaded(), "Trang danh sách sự kiện chờ duyệt phải tải thành công");

        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();

            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét chi tiết sự kiện phải được hiển thị");

            reviewPage.clickReject();
            Assert.assertTrue(reviewPage.isRejectDialogDisplayed(), "Hộp thoại từ chối sự kiện phải hiển thị");

            reviewPage.enterRejectReason("Thông tin mô tả sự kiện chưa đầy đủ sơ đồ phân khu, vui lòng bổ sung.");
            reviewPage.clickConfirmReject();

            Assert.assertTrue(pendingPage.isLoaded(), "Phải quay trở lại trang danh sách sự kiện chờ duyệt");
            Assert.assertTrue(pendingPage.isNoticeDisplayed(), "Thông báo từ chối sự kiện thành công phải được hiển thị");
        }
    }
}

