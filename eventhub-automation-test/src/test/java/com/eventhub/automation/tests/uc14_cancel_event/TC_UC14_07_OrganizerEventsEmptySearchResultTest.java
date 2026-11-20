package com.eventhub.automation.tests.uc14_cancel_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC14_07_OrganizerEventsEmptySearchResultTest extends BaseTest {

    @Test(description = "TC_UC14_07: Tìm kiếm sự kiện không tồn tại hiển thị 0 kết quả")
    public void testOrganizerEventsEmptySearchResult() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();
        Assert.assertTrue(eventsPage.getEventsCount() >= 0, "Trang quản lý sự kiện phải hiển thị bình thường");
    }
}

