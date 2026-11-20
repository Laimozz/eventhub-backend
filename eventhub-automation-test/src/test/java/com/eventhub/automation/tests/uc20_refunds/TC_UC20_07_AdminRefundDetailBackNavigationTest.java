package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_07_AdminRefundDetailBackNavigationTest extends BaseTest {

    @Test(description = "TC_UC20_07: Quản trị viên bấm nút Quay lại từ trang chi tiết đối soát hoàn tiền")
    public void testAdminRefundDetailBackNavigation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        Assert.assertTrue(refundsPage.getCanceledEventsCount() > 0, "Hệ thống phải có danh sách sự kiện đã hủy");

        refundsPage.clickFirstEventRefundDetail();

        AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết hoàn tiền phải hiển thị");

        detailPage.clickBack();

        Assert.assertTrue(refundsPage.getCanceledEventsCount() > 0,
                "Quay lại danh sách sự kiện đã hủy thành công");
    }
}

