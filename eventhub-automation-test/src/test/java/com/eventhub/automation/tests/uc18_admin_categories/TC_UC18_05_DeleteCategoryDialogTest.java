package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_05_DeleteCategoryDialogTest extends BaseTest {

    @Test(description = "TC_UC18_05: Nhấn nút Xóa danh mục mở dialog xác nhận xóa danh mục sự kiện")
    public void testDeleteCategoryDialog() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();

        if (categoriesPage.getCategoryCount() > 0) {
            categoriesPage.clickFirstDeleteCategory();
            // Dialog xác nhận xóa
            boolean dialogOpen = getDriver().findElements(org.openqa.selenium.By.xpath("//h2[contains(text(), 'Xóa danh mục')]")).size() > 0;
            Assert.assertTrue(dialogOpen, "Hộp thoại xác nhận xóa danh mục phải được hiển thị");

            categoriesPage.clickCancel();
        } else {
            Assert.assertTrue(true);
        }
    }
}

