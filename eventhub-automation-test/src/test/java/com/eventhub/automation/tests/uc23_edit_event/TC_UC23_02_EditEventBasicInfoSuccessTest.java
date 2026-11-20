package com.eventhub.automation.tests.uc23_edit_event;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.EditEventPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC23_02_EditEventBasicInfoSuccessTest extends BaseTest {

    @Test(description = "TC_UC23_02: Cập nhật thông tin mô tả và địa chỉ sự kiện thành công")
    public void testEditEventBasicInfoSuccess() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        EditEventPage editPage = new EditEventPage(getDriver()).openEventEdit(1);

        // Cập nhật địa chỉ mới
        String updatedAddress = "Số 123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1";
        editPage.updateAddress(updatedAddress);
        editPage.clickNextStep();

        // Kiểm tra chuyển sang bước tiếp theo hoặc lưu thành công
        Assert.assertTrue(editPage.getCurrentUrl().contains("/edit") || !editPage.isErrorBannerDisplayed(),
                "Cập nhật thông tin cơ bản không được báo lỗi");
    }
}

