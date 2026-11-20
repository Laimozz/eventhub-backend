package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_06_AdminCategoriesListNotEmptyTest extends BaseTest {

    @Test(description = "TC_UC18_06: Quản trị viên mở trang quản lý danh mục và danh sách danh mục hạt giống (Seed categories) nạp đầy đủ")
    public void testAdminCategoriesListNotEmpty() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getAdminEmail(),
                ConfigurationManager.getAdminPassword()
        );

        AdminCategoriesPage categoriesPage = new AdminCategoriesPage(getDriver()).open();
        Assert.assertTrue(categoriesPage.getCategoriesCount() > 0, "Hệ thống phải có ít nhất 1 danh mục sự kiện mặc định");
    }
}

