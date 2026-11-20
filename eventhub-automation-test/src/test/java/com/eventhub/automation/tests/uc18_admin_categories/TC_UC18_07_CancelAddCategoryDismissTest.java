package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_07_CancelAddCategoryDismissTest extends BaseTest {

    @Test(description = "TC_UC18_07: Hủy bỏ thao tác thêm danh mục khi nhấn nút Hủy hoặc đóng Dialog")
    public void testCancelAddCategoryDismiss() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage page = new AdminCategoriesPage(getDriver()).open();
        page.clickAddCategory();
        Assert.assertTrue(page.isModalOpen(), "Dialog thêm danh mục phải hiển thị");

        page.clickCancel();
        Assert.assertFalse(page.isModalOpen(), "Dialog thêm danh mục phải được đóng lại khi nhấn Hủy");
    }
}

