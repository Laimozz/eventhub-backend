package com.eventhub.automation.tests.uc19_event_approval;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminEventReviewPage;
import com.eventhub.automation.pages.AdminPendingEventsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC19_08_DismissRejectDialogTest extends BaseTest {

    @Test(description = "TC_UC19_08: Đóng hộp thoại từ chối sự kiện khi bấm nút Hủy bỏ mà không làm thay đổi trạng thái")
    public void testDismissRejectDialog() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminPendingEventsPage pendingPage = new AdminPendingEventsPage(getDriver()).open();
        Assert.assertTrue(pendingPage.isLoaded(), "Trang sự kiện chờ duyệt phải tải thành công");

        if (pendingPage.getPendingEventsCount() > 0) {
            pendingPage.clickFirstPendingEvent();

            AdminEventReviewPage reviewPage = new AdminEventReviewPage(getDriver());
            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét sự kiện phải được mở");

            reviewPage.clickReject();
            Assert.assertTrue(reviewPage.isRejectDialogDisplayed(), "Hộp thoại từ chối sự kiện phải xuất hiện");

            reviewPage.enterRejectReason("Lý do tạm thời...");
            reviewPage.clickCancelDialog();

            Assert.assertTrue(reviewPage.isLoaded(), "Trang xem xét vẫn hiển thị bình thường sau khi đóng dialog");
        }
    }
}

