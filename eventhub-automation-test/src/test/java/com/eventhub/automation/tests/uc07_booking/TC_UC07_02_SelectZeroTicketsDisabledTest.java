package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_02_SelectZeroTicketsDisabledTest extends BaseTest {

    @Test(description = "TC_UC07_02: Khi chưa chọn loại vé nào, nút Tiếp tục bị vô hiệu hóa")
    public void testSelectZeroTicketsDisabled() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(1);

        // Không bấm chọn vé nào
        Assert.assertFalse(selectTicketsPage.isContinueButtonEnabled(), "Nút Tiếp tục phải bị disable khi tổng số vé chọn = 0");
        Assert.assertTrue(selectTicketsPage.isEmptySelectionWarningDisplayed(), "Phải hiển thị cảnh báo 'Bạn chưa chọn vé'");
    }
}

