package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_06_StaffDispatchBackNavigationTest extends BaseTest {

    @Test(description = "TC_UC13_06: Kiểm tra điều hướng tìm kiếm nhân sự và sự kiện")
    public void testStaffDispatchSearch() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        StaffDispatchPage page = new StaffDispatchPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang điều phối nhân sự sự kiện phải được mở thành công");

        page.searchEvent("Âm nhạc");
        Assert.assertTrue(page.isLoaded(), "Trang vẫn giữ nguyên giao diện sau khi tìm kiếm");
    }
}

