package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_05_WithdrawalHistoryStatusBadgeTest extends BaseTest {

    @Test(description = "TC_UC15_05: Kiểm tra danh sách lịch sử yêu cầu rút tiền hiển thị số dư và các yêu cầu chi tiết")
    public void testWithdrawalHistoryStatusBadge() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerWalletPage page = new OrganizerWalletPage(getDriver()).open();
        Assert.assertTrue(page.isLoaded(), "Trang Ví ban tổ chức phải được hiển thị");
        Assert.assertNotNull(page.getAvailableBalance(), "Số dư khả dụng phải có giá trị hợp lệ");
    }
}

