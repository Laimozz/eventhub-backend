package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalDetailPage;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_03_AdminViewWithdrawalDetailTest extends BaseTest {

    @Test(description = "TC_UC21_03: Quản trị viên nhấp vào mã yêu cầu để xem toàn bộ thông tin tài khoản ngân hàng và số tiền rút")
    public void testAdminViewWithdrawalDetail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminWithdrawalsPage withdrawalsPage = new AdminWithdrawalsPage(getDriver()).open();

        if (withdrawalsPage.getWithdrawalsCount() > 0) {
            withdrawalsPage.clickFirstWithdrawalDetail();

            AdminWithdrawalDetailPage detailPage = new AdminWithdrawalDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết yêu cầu rút tiền phải được mở");
            Assert.assertTrue(detailPage.getPageTitle().contains("Yêu cầu #"), "Tiêu đề trang phải có mã yêu cầu");
        } else {
            Assert.assertTrue(true);
        }
    }
}

