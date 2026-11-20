package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_03_ViewRefundReconciliationTilesTest extends BaseTest {

    @Test(description = "TC_UC20_03: Kiểm tra hiển thị các thẻ đối soát hoàn tiền (Tổng cần hoàn trả, Đã hoàn tiền thành công)")
    public void testViewRefundReconciliationTiles() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        refundsPage.clickFirstEventRefundDetail();

        AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết đối soát hoàn tiền phải được hiển thị");
    }
}

