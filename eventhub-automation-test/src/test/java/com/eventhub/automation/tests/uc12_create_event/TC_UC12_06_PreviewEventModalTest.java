package com.eventhub.automation.tests.uc12_create_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.CreateEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC12_06_PreviewEventModalTest extends BaseTest {

    @Test(description = "TC_UC12_06: Nhấn nút 'Xem trước' mở dialog cho phép ban tổ chức xem trước hồ sơ sự kiện")
    public void testPreviewEventModal() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        CreateEventPage createEventPage = new CreateEventPage(getDriver()).open();
        createEventPage.enterName("Sự kiện Demo Xem Trước");
        createEventPage.clickPreview();

        Assert.assertTrue(createEventPage.isPreviewModalDisplayed(),
                "Hộp thoại xem trước sự kiện phải được mở");
    }
}

