package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_02_ViewEventRefundDetailTest extends BaseTest {

    @Test(description = "TC_UC20_02: Quản trị viên xem chi tiết đối soát hoàn tiền và lịch sử hoàn tiền của sự kiện hủy")
    public void testViewEventRefundDetail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        refundsPage.clickFirstEventRefundDetail();

        AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết đối soát hoàn tiền của sự kiện phải được hiển thị");
    }
}

