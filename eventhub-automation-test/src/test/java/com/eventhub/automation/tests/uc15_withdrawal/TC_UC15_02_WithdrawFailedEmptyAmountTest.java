package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_02_WithdrawFailedEmptyAmountTest extends BaseTest {

    @Test(description = "TC_UC15_02: Mở modal rút tiền và để trống số tiền rút không thể chuyển sang bước xác nhận")
    public void testWithdrawFailedEmptyAmount() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();
        walletPage.clickOpenWithdrawModal();
        Assert.assertTrue(walletPage.isModalOpen(), "Hộp thoại rút tiền phải được mở");

        // Không nhập số tiền, chỉ nhập số tài khoản
        walletPage.enterWithdrawDetails("", "Vietcombank", "0071001234567");
        walletPage.clickProceedConfirm();

        // Modal vẫn phải đang mở ở bước nhập liệu
        Assert.assertTrue(walletPage.isModalOpen(), "Không thể hoàn tất rút tiền khi chưa nhập số tiền hợp lệ");

        walletPage.clickCancelModal();
    }
}

