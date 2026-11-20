package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_07_CancelWithdrawalDialogTest extends BaseTest {

    @Test(description = "TC_UC15_07: Hủy bỏ thao tác rút tiền khi nhấn nút Hủy / đóng modal")
    public void testCancelWithdrawalDialog() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();
        walletPage.clickOpenWithdrawModal();
        Assert.assertTrue(walletPage.isModalOpen(), "Modal rút tiền phải được mở");

        walletPage.clickCancelModal();
        Assert.assertTrue(walletPage.isLoaded(), "Trang ví vẫn hiển thị sau khi đóng modal");
    }
}

