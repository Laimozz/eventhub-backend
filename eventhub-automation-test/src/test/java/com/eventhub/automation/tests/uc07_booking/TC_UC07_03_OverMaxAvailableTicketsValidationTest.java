package com.eventhub.automation.tests.uc07_booking;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.SelectTicketsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC07_03_OverMaxAvailableTicketsValidationTest extends BaseTest {

    @Test(description = "TC_UC07_03: Kiểm tra nút tăng vé bị vô hiệu hóa khi đạt số lượng vé tối đa còn lại hoặc giới hạn 100 vé")
    public void testOverMaxAvailableTicketsValidation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getCustomerEmail(),
                ConfigurationManager.getCustomerPassword()
        );

        SelectTicketsPage selectTicketsPage = new SelectTicketsPage(getDriver()).open(41);

        // Bấm tăng số lượng vé nhiều lần (tối đa cho phép trên hệ thống)
        selectTicketsPage.increaseFirstTicket(105);

        // Khi đạt số lượng vé tối đa hoặc còn lại trong kho, nút Tăng phải bị disable
        Assert.assertFalse(selectTicketsPage.isFirstTicketIncreaseButtonEnabled(),
                "Nút tăng vé phải bị vô hiệu hóa khi đạt ngưỡng số lượng tối đa cho phép");
    }
}

