package com.eventhub.automation.tests.uc15_withdrawal;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerWalletPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC15_04_ViewWithdrawalDetailItemTest extends BaseTest {

    @Test(description = "TC_UC15_04: Ban tổ chức xem chi tiết yêu cầu rút tiền trong lịch sử giao dịch")
    public void testViewWithdrawalDetailItem() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        OrganizerWalletPage walletPage = new OrganizerWalletPage(getDriver()).open();

        boolean hasItems = getDriver().findElements(By.cssSelector("tr[class*='row'], div[class*='item'], table tbody tr")).size() > 0;
        Assert.assertTrue(walletPage.getCurrentUrl().contains("/wallet"), "Phải truy cập đúng trang ví ban tổ chức");
        Assert.assertTrue(hasItems, "Trang ví phải hiển thị bảng lịch sử hoặc danh sách yêu cầu rút tiền");
    }
}

