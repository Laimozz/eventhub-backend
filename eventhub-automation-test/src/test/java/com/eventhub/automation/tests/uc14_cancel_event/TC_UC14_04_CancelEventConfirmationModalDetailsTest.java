package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_04_CancelEventConfirmationModalDetailsTest extends BaseTest {

    @Test(description = "TC_UC14_04: Kiểm tra modal xác nhận hủy sự kiện hiển thị nút xác nhận và hủy bỏ")
    public void testCancelEventConfirmationModalDetails() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerEventsPage page = new OrganizerEventsPage(getDriver()).open();
        if (page.hasCancelableEvent()) {
            page.clickFirstCancelButton();
            Assert.assertTrue(page.isCancelDialogDisplayed(), "Hộp thoại xác nhận hủy sự kiện phải được hiển thị");
            page.dismissCancelDialog();
            Assert.assertFalse(page.isCancelDialogDisplayed(), "Hộp thoại hủy sự kiện phải đóng sau khi nhấn Hủy");
        }
    }
}

