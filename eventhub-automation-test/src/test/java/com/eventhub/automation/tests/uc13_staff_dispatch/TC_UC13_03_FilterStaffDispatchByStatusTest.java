package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_03_FilterStaffDispatchByStatusTest extends BaseTest {

    @Test(description = "TC_UC13_03: Ban tổ chức lọc danh sách sự kiện điều phối theo trạng thái (Sắp diễn ra vs Đã kết thúc)")
    public void testFilterStaffDispatchByStatus() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        StaffDispatchPage dispatchPage = new StaffDispatchPage(getDriver()).open();

        dispatchPage.filterStatus("Sắp diễn ra");
        int upcomingCount = dispatchPage.getEventsCount();

        dispatchPage.filterStatus("Tất cả");
        int allCount = dispatchPage.getEventsCount();

        Assert.assertTrue(allCount >= upcomingCount, "Tổng số sự kiện phải lớn hơn hoặc bằng số sự kiện sắp diễn ra");
    }
}

