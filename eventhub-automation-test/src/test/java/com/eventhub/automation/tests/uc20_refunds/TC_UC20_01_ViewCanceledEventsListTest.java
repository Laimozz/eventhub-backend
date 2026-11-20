package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_01_ViewCanceledEventsListTest extends BaseTest {

    @Test(description = "TC_UC20_01: Quản trị viên xem danh sách các sự kiện đã hủy để tiến hành đối soát hoàn tiền")
    public void testViewCanceledEventsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();

        Assert.assertTrue(refundsPage.getCanceledEventsCount() > 0, "Hệ thống phải có sự kiện đã hủy để đối soát hoàn tiền");
    }
}

