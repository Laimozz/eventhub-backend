package com.eventhub.automation.tests.uc21_admin_payout;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminWithdrawalsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC21_08_AdminWithdrawalStatusBadgeTest extends BaseTest {

    @Test(description = "TC_UC21_08: Kiểm tra hiển thị huy hiệu trạng thái yêu cầu rút tiền khi lọc")
    public void testAdminWithdrawalStatusBadge() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminWithdrawalsPage page = new AdminWithdrawalsPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang danh sách yêu cầu rút tiền phải tải thành công");

        // Lọc trạng thái APPROVED
        page.filterStatus("APPROVED");
        if (page.hasWithdrawalRows()) {
            String badge = page.getFirstRowStatusBadgeText();
            Assert.assertTrue(badge.contains("Đã duyệt") || badge.contains("APPROVED"),
                    "Huy hiệu trạng thái phải hiển thị 'Đã duyệt'");
        }

        // Lọc trạng thái REJECTED
        page.filterStatus("REJECTED");
        if (page.hasWithdrawalRows()) {
            String badge = page.getFirstRowStatusBadgeText();
            Assert.assertTrue(badge.contains("từ chối") || badge.contains("REJECTED"),
                    "Huy hiệu trạng thái phải hiển thị 'Bị từ chối'");
        }
    }
}

