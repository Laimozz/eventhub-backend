package com.eventhub.automation.tests.uc13_staff_dispatch;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.StaffDispatchPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC13_01_ViewStaffDispatchListTest extends BaseTest {

    @Test(description = "TC_UC13_01: Ban tổ chức xem danh sách sự kiện cần điều phối và phân công nhân viên soát vé")
    public void testViewStaffDispatchList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        StaffDispatchPage dispatchPage = new StaffDispatchPage(getDriver()).open();

        Assert.assertTrue(dispatchPage.getCurrentUrl().contains("/staffs"),
                "Ban tổ chức phải truy cập thành công trang phân công nhân viên");
    }
}

