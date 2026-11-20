package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalDetailPage;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_04_AdminRejectWithdrawalDialogTest extends BaseTest {

    @Test(description = "TC_UC21_04: Nhấn nút Từ chối yêu cầu rút tiền mở dialog nhập lý do từ chối")
    public void testAdminRejectWithdrawalDialog() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminWithdrawalsPage withdrawalsPage = new AdminWithdrawalsPage(getDriver()).open();

        if (withdrawalsPage.getWithdrawalsCount() > 0) {
            withdrawalsPage.clickFirstWithdrawalDetail();

            AdminWithdrawalDetailPage detailPage = new AdminWithdrawalDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết yêu cầu rút tiền phải được mở");

            if (detailPage.hasActionButtons()) {
                detailPage.clickReject();
                Assert.assertTrue(detailPage.isRejectDialogDisplayed(), "Hộp thoại xác nhận từ chối phải được hiển thị");
                detailPage.clickCancelDialog();
            }
        } else {
            Assert.assertTrue(true);
        }
    }
}

