package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_08_WithdrawMinAmountValidationTest extends BaseTest {

    @Test(description = "TC_UC15_08: Cảnh báo khi nhập số tiền rút nhỏ hơn mức tối thiểu quy định (ví dụ 1000đ)")
    public void testWithdrawMinAmountValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();
        walletPage.clickOpenWithdrawModal();

        walletPage.enterWithdrawDetails("1000", "NCB", "1234567890");
        walletPage.clickProceedConfirm();

        Assert.assertTrue(walletPage.isModalOpen(), "Hệ thống phải chặn submit hoặc hiển thị cảnh báo mức rút tối thiểu");
    }
}

