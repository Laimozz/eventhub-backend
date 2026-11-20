package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_02_AddCategorySuccessTest extends BaseTest {

    @Test(description = "TC_UC18_02: Quản trị viên thêm danh mục mới với thông tin hợp lệ thành công")
    public void testAddCategorySuccess() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();
        categoriesPage.clickAddCategory();

        String uniqueName = "Danh mục Auto " + System.currentTimeMillis();
        categoriesPage.enterCategory(uniqueName, "Mô tả cho danh mục kiểm thử tự động");
        categoriesPage.clickSave();

        Assert.assertTrue(categoriesPage.isNoticeDisplayed() || !categoriesPage.isModalOpen(),
                "Hệ thống phải lưu thành công và đóng dialog thêm danh mục");
    }
}

