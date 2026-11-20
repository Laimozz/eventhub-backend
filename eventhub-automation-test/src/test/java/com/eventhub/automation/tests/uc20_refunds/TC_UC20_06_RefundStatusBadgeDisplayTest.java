package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_06_RefundStatusBadgeDisplayTest extends BaseTest {

    @Test(description = "TC_UC20_06: Kiểm tra hiển thị các khối tóm tắt đối soát hoàn tiền (Reconciliation Tiles)")
    public void testRefundStatusBadgeDisplay() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        Assert.assertTrue(refundsPage.getCanceledEventsCount() > 0, "Hệ thống phải có sự kiện đã hủy");

        refundsPage.clickFirstEventRefundDetail();

        AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết hoàn tiền phải hiển thị");
        Assert.assertTrue(detailPage.isReconciliationTileDisplayed(), "Các thẻ tổng quan hoàn tiền (Tổng cần hoàn, Đã hoàn) phải được hiển thị");
    }
}

