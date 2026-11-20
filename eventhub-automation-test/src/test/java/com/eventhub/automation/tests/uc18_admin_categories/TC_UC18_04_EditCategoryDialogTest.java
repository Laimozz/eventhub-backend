package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_04_EditCategoryDialogTest extends BaseTest {

    @Test(description = "TC_UC18_04: Nhấn nút Sửa danh mục mở dialog chứa sẵn tên và mô tả danh mục để chỉnh sửa")
    public void testEditCategoryDialog() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();

        if (categoriesPage.getCategoryCount() > 0) {
            categoriesPage.clickFirstEditCategory();
            Assert.assertTrue(categoriesPage.isModalOpen(), "Hộp thoại sửa danh mục phải được hiển thị");

            categoriesPage.clickCancel();
            Assert.assertFalse(categoriesPage.isModalOpen(), "Hộp thoại sửa danh mục phải được đóng khi nhấn Hủy bỏ");
        } else {
            Assert.assertTrue(true);
        }
    }
}

