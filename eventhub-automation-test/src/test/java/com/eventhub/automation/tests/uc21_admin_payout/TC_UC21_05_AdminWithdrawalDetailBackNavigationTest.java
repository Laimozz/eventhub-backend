package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalDetailPage;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_05_AdminWithdrawalDetailBackNavigationTest extends BaseTest {

    @Test(description = "TC_UC21_05: Quản trị viên mở chi tiết yêu cầu rút tiền và quay trở lại danh sách rút tiền")
    public void testAdminWithdrawalDetailBackNavigation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminWithdrawalsPage withdrawalsPage = new AdminWithdrawalsPage(getDriver()).open();
        if (withdrawalsPage.getWithdrawalsCount() > 0) {
            withdrawalsPage.clickFirstWithdrawalDetail();
            AdminWithdrawalDetailPage detailPage = new AdminWithdrawalDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết yêu cầu rút tiền phải hiển thị");

            detailPage.clickBack();
            Assert.assertTrue(withdrawalsPage.isLoaded(), "Phải quay lại trang danh sách yêu cầu rút tiền thành công");
        }
    }
}

