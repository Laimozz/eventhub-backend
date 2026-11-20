package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalDetailPage;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_06_CancelRejectWithdrawalDialogTest extends BaseTest {

    @Test(description = "TC_UC21_06: Quản trị viên mở dialog từ chối yêu cầu rút tiền rồi bấm Hủy bỏ")
    public void testCancelRejectWithdrawalDialog() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminWithdrawalsPage page = new AdminWithdrawalsPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang danh sách yêu cầu rút tiền phải tải thành công");

        page.filterStatus("PENDING");

        if (page.hasWithdrawalRows()) {
            page.clickFirstWithdrawalDetail();

            AdminWithdrawalDetailPage detailPage = new AdminWithdrawalDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết yêu cầu rút tiền phải được mở");

            if (detailPage.hasActionButtons()) {
                detailPage.clickReject();
                Assert.assertTrue(detailPage.isRejectDialogDisplayed(), "Hộp thoại từ chối rút tiền phải được hiển thị");

                detailPage.enterReasonOrCode("Lý do tạm thời...");
                detailPage.clickCancelDialog();

                Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết yêu cầu vẫn hiển thị bình thường sau khi đóng dialog");
            }
        }
    }
}

