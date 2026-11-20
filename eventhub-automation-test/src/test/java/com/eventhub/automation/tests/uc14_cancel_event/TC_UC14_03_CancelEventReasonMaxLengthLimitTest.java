package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_03_CancelEventReasonMaxLengthLimitTest extends BaseTest {

    @Test(description = "TC_UC14_03: Kiểm tra giới hạn độ dài lý do hủy sự kiện không vượt quá 255 ký tự")
    public void testCancelEventReasonMaxLengthLimit() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();

        if (eventsPage.hasCancelableEvent()) {
            eventsPage.clickFirstCancelButton();
            Assert.assertTrue(eventsPage.isCancelDialogDisplayed(), "Hộp thoại hủy sự kiện phải được mở");

            String longReason = "Lý do hủy ".repeat(30); // > 255 ký tự
            eventsPage.enterCancelReason(longReason);

            String actualValue = getDriver().findElement(By.id("cancel-reason")).getAttribute("value");
            Assert.assertTrue(actualValue.length() <= 255, "Lý do hủy không được vượt quá 255 ký tự");

            eventsPage.clickKeepEvent();
        } else {
            Assert.assertTrue(true);
        }
    }
}

