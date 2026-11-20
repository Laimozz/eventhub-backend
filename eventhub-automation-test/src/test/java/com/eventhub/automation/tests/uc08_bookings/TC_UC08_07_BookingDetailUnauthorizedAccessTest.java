package com.eventhub.automation.tests.uc08_bookings;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC08_07_BookingDetailUnauthorizedAccessTest extends BaseTest {

    @Test(description = "TC_UC08_07: Bảo mật: Truy cập mã booking không tồn tại hoặc không có quyền sẽ hiển thị thông báo lỗi")
    public void testBookingDetailUnauthorizedAccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        // Truy cập thẳng vào mã booking không tồn tại 999999
        getDriver().get(ConfigurationManager.getBaseUrl() + "/customer/bookings/999999");

        org.openqa.selenium.WebElement alertElem = new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@role='alert'] | //*[contains(text(), 'Chưa thể tải') or contains(text(), 'không tồn tại')]")));

        Assert.assertTrue(alertElem.isDisplayed(), "Hệ thống phải hiển thị thông báo lỗi khi truy cập booking không hợp lệ");
    }
}

