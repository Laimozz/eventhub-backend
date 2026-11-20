package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_03_WithdrawFailedExceedBalanceTest extends BaseTest {

    @Test(description = "TC_UC15_03: Yêu cầu rút tiền thất bại khi số tiền rút vượt quá số dư khả dụng thực tế")
    public void testWithdrawFailedExceedBalance() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();
        walletPage.clickOpenWithdrawModal();

        // Nhập số tiền rất lớn (10 tỷ VNĐ) vượt số dư khả dụng
        walletPage.enterWithdrawDetails("10000000000", "Vietcombank", "0071001234567");
        walletPage.clickProceedConfirm();

        // Modal vẫn giữ nguyên để người dùng chỉnh sửa
        Assert.assertTrue(walletPage.isModalOpen(), "Hệ thống phải chặn không cho rút số tiền vượt quá số dư khả dụng");

        walletPage.clickCancelModal();
    }
}

