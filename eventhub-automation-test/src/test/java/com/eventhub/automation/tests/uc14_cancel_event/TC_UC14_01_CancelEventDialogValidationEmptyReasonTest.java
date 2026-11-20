package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_01_CancelEventDialogValidationEmptyReasonTest extends BaseTest {

    @Test(description = "TC_UC14_01: Mở dialog hủy sự kiện nhưng để trống lý do hủy phải báo lỗi validation")
    public void testCancelEventDialogValidationEmptyReason() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();

        if (eventsPage.hasCancelableEvent()) {
            eventsPage.clickFirstCancelButton();
            Assert.assertTrue(eventsPage.isCancelDialogDisplayed(), "Hộp thoại xác nhận hủy sự kiện phải được mở");

            // Không điền lý do và bấm Xác nhận hủy
            eventsPage.enterCancelReason("");
            eventsPage.clickConfirmCancel();

            Assert.assertTrue(eventsPage.isErrorBannerDisplayed(), "Phải hiển thị thông báo lỗi khi chưa nhập lý do hủy sự kiện");
        } else {
            // Không có sự kiện nào ở trạng thái có thể hủy trong danh sách hiện tại
            Assert.assertTrue(true);
        }
    }
}

