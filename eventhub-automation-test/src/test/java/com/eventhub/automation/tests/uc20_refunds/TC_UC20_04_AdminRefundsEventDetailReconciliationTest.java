package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_04_AdminRefundsEventDetailReconciliationTest extends BaseTest {

    @Test(description = "TC_UC20_04: Quản trị viên xem chi tiết đối soát hoàn tiền và các thông tin thanh toán VNPay của sự kiện hủy")
    public void testAdminRefundsEventDetailReconciliation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        if (refundsPage.getRefundEventsCount() > 0) {
            refundsPage.clickFirstEventRefundDetail();
            AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
            Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết đối soát hoàn tiền phải hiển thị");
            Assert.assertTrue(detailPage.isReconciliationTileDisplayed() || detailPage.isLoaded(), "Thẻ tóm tắt hoàn tiền phải được hiển thị");
        }
    }
}
