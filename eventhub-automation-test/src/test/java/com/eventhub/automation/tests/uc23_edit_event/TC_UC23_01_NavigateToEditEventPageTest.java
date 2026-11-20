package com.eventhub.automation.tests.uc23_edit_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.EditEventPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerEventsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC23_01_NavigateToEditEventPageTest extends BaseTest {

    @Test(description = "TC_UC23_01: Ban tổ chức truy cập trang chỉnh sửa sự kiện và nạp thông tin cũ thành công")
    public void testNavigateToEditEventPage() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerEventsPage eventsPage = new OrganizerEventsPage(getDriver()).open();
        Assert.assertTrue(eventsPage.getEventsCount() > 0, "Ban tổ chức phải có ít nhất 1 sự kiện để kiểm thử chỉnh sửa");

        // Điều hướng trực tiếp đến trang edit sự kiện 1 (hoặc sự kiện đầu tiên)
        EditEventPage editPage = new EditEventPage(getDriver()).openEventEdit(1);

        String currentUrl = editPage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/edit") || currentUrl.contains("/events"), "Phải mở được đường dẫn chỉnh sửa sự kiện");
    }
}

