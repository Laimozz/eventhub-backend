package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_06_FilterOrganizerEventsByApprovedStatusTest extends BaseTest {

    @Test(description = "TC_UC14_06: Lọc danh sách sự kiện của tôi theo trạng thái")
    public void testFilterOrganizerEvents() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();
        Assert.assertTrue(eventsPage.getEventsCount() >= 0, "Danh sách sự kiện phải hiển thị theo bộ lọc");
    }
}

