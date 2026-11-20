package com.eventhub.automation.tests.uc23_edit_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.EditEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC23_03_EditEventEmptyFieldsValidationTest extends BaseTest {

    @Test(description = "TC_UC23_03: Chỉnh sửa thất bại khi xóa trắng tên sự kiện")
    public void testEditEventEmptyFieldsValidation() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        EditEventPage editPage = new EditEventPage(getDriver()).openEventEdit(1);

        // Xóa trắng tên sự kiện
        editPage.updateEventName("");
        editPage.clickNextStep();

        // Kiểm tra vẫn ở lại bước 1 / details
        Assert.assertTrue(editPage.getCurrentUrl().contains("details") || editPage.isErrorBannerDisplayed(),
                "Không cho phép lưu khi xóa trắng tên sự kiện");
    }
}

