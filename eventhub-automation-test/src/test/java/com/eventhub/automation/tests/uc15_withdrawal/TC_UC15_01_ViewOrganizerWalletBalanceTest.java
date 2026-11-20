package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_01_ViewOrganizerWalletBalanceTest extends BaseTest {

    @Test(description = "TC_UC15_01: Ban tổ chức xem số dư khả dụng và thông tin ví tài chính")
    public void testViewOrganizerWalletBalance() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();

        Assert.assertTrue(walletPage.getCurrentUrl().contains("/wallet"), "Phải truy cập đúng trang ví tài chính ban tổ chức");
    }
}

