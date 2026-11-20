package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_07_AdminWithdrawalsPaginationTest extends BaseTest {

    @Test(description = "TC_UC21_07: Kiểm tra thanh phân trang danh sách yêu cầu rút tiền")
    public void testAdminWithdrawalsPagination() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminWithdrawalsPage page = new AdminWithdrawalsPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang danh sách yêu cầu rút tiền phải tải thành công");

        if (page.isPaginationDisplayed()) {
            String paginationText = page.getPaginationText();
            Assert.assertTrue(paginationText.contains("kết quả"), "Phân trang phải hiển thị tổng số kết quả");

            page.clickNextPage();
            String newPaginationText = page.getPaginationText();
            Assert.assertNotNull(newPaginationText, "Trang tiếp theo phải cập nhật thông tin phân trang");
        }
    }
}

