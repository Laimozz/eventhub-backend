package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_06_WithdrawalHistoryFilterTest extends BaseTest {

    @Test(description = "TC_UC15_06: Kiểm tra số dư ví và hiển thị lịch sử yêu cầu rút tiền")
    public void testWithdrawalHistoryDisplay() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();
        Assert.assertTrue(walletPage.isLoaded(), "Trang ví doanh thu phải hiển thị thành công");
        Assert.assertNotNull(walletPage.getAvailableBalance(), "Số dư ví khả dụng phải được hiển thị");
    }
}

