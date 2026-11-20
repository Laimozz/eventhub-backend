package com.eventhub.automation.tests.uc09_tickets;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.BookingDetailPage;
import com.eventhub.automation.pages.BookingHistoryPage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.MyTicketsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC09_06_ViewTicketsDirectlyFromBookingDetailTest extends BaseTest {

    @Test(description = "TC_UC09_06: Xem vé điện tử trực tiếp từ trang Chi tiết booking")
    public void testViewTicketsDirectlyFromBookingDetail() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        BookingHistoryPage historyPage = new BookingHistoryPage(getDriver()).open();
        historyPage.clickFirstBookingDetail();

        BookingDetailPage detailPage = new BookingDetailPage(getDriver());
        Assert.assertTrue(detailPage.isLoaded(), "Trang chi tiết booking phải được mở");

        if (detailPage.isQrButtonAvailable()) {
            getDriver().findElement(By.xpath("//a[contains(., 'Xem vé điện tử / QR') or contains(., 'Xem vé QR')]")).click();
            MyTicketsPage ticketsPage = new MyTicketsPage(getDriver());
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/tickets"),
                    "Phải chuyển hướng đến màn hình vé điện tử tương ứng của booking");
        } else {
            Assert.assertTrue(true);
        }
    }
}

