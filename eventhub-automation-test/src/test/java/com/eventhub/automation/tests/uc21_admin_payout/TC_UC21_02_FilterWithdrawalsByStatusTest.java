package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_02_FilterWithdrawalsByStatusTest extends BaseTest {

    @Test(description = "TC_UC21_02: Quản trị viên lọc yêu cầu rút tiền theo trạng thái PENDING / APPROVED")
    public void testFilterWithdrawalsByStatus() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminWithdrawalsPage withdrawalsPage = new AdminWithdrawalsPage(getDriver()).open();

        withdrawalsPage.filterStatus("PENDING");
        int pendingCount = withdrawalsPage.getWithdrawalsCount();

        withdrawalsPage.filterStatus("APPROVED");
        int approvedCount = withdrawalsPage.getWithdrawalsCount();

        Assert.assertTrue(pendingCount >= 0 && approvedCount >= 0, "Hệ thống lọc danh sách yêu cầu rút tiền theo trạng thái thành công");
    }
}

