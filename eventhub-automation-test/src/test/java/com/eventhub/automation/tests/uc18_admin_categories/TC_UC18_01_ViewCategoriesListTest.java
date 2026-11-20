package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_01_ViewCategoriesListTest extends BaseTest {

    @Test(description = "TC_UC18_01: Quản trị viên xem danh sách các danh mục sự kiện trong hệ thống")
    public void testViewCategoriesList() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();

        Assert.assertTrue(categoriesPage.getCategoryCount() > 0, "Hệ thống phải có danh sách danh mục sự kiện hiển thị");
    }
}

