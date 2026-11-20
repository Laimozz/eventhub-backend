package com.eventhub.automation.tests.uc20_refunds;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminRefundDetailPage;
import com.eventhub.automation.pages.AdminRefundsPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC20_05_ViewRefundRequestsListTest extends BaseTest {

    @Test(description = "TC_UC20_05: Quản trị viên xem danh sách các giao dịch thanh toán cần hoàn tiền của sự kiện đã hủy")
    public void testViewRefundRequestsList() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminRefundsPage refundsPage = new AdminRefundsPage(getDriver()).open();
        Assert.assertTrue(refundsPage.getCanceledEventsCount() > 0, "Hệ thống phải có danh sách sự kiện đã hủy để đối soát");

        refundsPage.clickFirstEventRefundDetail();

        AdminRefundDetailPage detailPage = new AdminRefundDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết đối soát hoàn tiền sự kiện phải được hiển thị");
        Assert.assertTrue(detailPage.isTransactionsSectionDisplayed(), "Mục 'Giao dịch và lịch sử hoàn tiền' phải được hiển thị");
        Assert.assertTrue(detailPage.hasTransactionsOrEmptyState(), "Bảng giao dịch thanh toán hoặc trạng thái dữ liệu phải được render");
    }
}

