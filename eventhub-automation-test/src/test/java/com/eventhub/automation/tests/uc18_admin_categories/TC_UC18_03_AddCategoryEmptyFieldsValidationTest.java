package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_03_AddCategoryEmptyFieldsValidationTest extends BaseTest {

    @Test(description = "TC_UC18_03: Thêm danh mục thất bại khi để trống tên hoặc mô tả")
    public void testAddCategoryEmptyFieldsValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();
        categoriesPage.clickAddCategory();

        categoriesPage.enterCategory("", "");
        categoriesPage.clickSave();

        Assert.assertTrue(categoriesPage.isModalOpen(), "Hộp thoại thêm danh mục không được đóng lại khi dữ liệu để trống");

        categoriesPage.clickCancel();
    }
}

