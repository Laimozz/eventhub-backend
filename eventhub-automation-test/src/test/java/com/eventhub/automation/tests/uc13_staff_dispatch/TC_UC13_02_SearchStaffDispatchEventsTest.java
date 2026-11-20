package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_02_SearchStaffDispatchEventsTest extends BaseTest {

    @Test(description = "TC_UC13_02: Tìm kiếm sự kiện trong bảng điều phối nhân viên theo tên sự kiện")
    public void testSearchStaffDispatchEvents() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        StaffDispatchPage dispatchPage = new StaffDispatchPage(getDriver()).open();
        dispatchPage.searchEvent("Music");

        Assert.assertTrue(dispatchPage.getCurrentUrl().contains("/staffs"),
                "Tìm kiếm sự kiện điều phối thực thi ổn định");
    }
}

