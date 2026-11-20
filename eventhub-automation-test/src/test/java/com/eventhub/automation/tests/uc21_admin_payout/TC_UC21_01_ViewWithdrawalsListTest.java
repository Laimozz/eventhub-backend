package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_01_ViewWithdrawalsListTest extends BaseTest {

    @Test(description = "TC_UC21_01: Quản trị viên xem danh sách các yêu cầu rút tiền từ Ban tổ chức")
    public void testViewWithdrawalsList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminWithdrawalsPage withdrawalsPage = new AdminWithdrawalsPage(getDriver()).open();

        Assert.assertTrue(withdrawalsPage.getWithdrawalsCount() > 0, "Bảng yêu cầu rút tiền phải có dữ liệu hiển thị");
    }
}

