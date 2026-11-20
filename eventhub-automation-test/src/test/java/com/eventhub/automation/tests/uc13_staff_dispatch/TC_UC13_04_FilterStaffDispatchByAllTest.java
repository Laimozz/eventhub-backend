package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_04_FilterStaffDispatchByAllTest extends BaseTest {

    @Test(description = "TC_UC13_04: Lọc danh sách phân công nhân viên theo bộ lọc 'Tất cả'")
    public void testFilterStaffDispatchByAll() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        StaffDispatchPage page = new StaffDispatchPage(getDriver()).open();
        page.filterByStatus("Tất cả");

        Assert.assertTrue(page.isLoaded(), "Trang Điều phối Staff phải nạp dữ liệu ổn định");
    }
}

