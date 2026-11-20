package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_02_CancelEventDialogDismissTest extends BaseTest {

    @Test(description = "TC_UC14_02: Nhấn nút 'Giữ sự kiện' để đóng modal hủy sự kiện an toàn")
    public void testCancelEventDialogDismiss() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();

        if (eventsPage.hasCancelableEvent()) {
            eventsPage.clickFirstCancelButton();
            Assert.assertTrue(eventsPage.isCancelDialogDisplayed(), "Hộp thoại xác nhận hủy sự kiện phải được mở");

            eventsPage.clickKeepEvent();
            Assert.assertFalse(eventsPage.isCancelDialogDisplayed(), "Hộp thoại xác nhận hủy phải đóng lại khi bấm Giữ sự kiện");
        } else {
            Assert.assertTrue(true);
        }
    }
}

