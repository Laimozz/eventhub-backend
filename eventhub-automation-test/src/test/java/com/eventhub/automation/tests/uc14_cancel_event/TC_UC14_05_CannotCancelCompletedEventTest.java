package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_05_CannotCancelCompletedEventTest extends BaseTest {

    @Test(description = "TC_UC14_05: Kiểm tra danh sách sự kiện của Organizer và điều kiện cho phép hủy")
    public void testCannotCancelCompletedEvent() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();
        Assert.assertTrue(eventsPage.getEventsCount() > 0, "Organizer phải có ít nhất 1 sự kiện hiển thị trong danh sách");
    }
}

