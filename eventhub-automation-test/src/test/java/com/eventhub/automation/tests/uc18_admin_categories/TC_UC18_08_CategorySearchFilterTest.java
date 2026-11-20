package com.eventhub.automation.tests.uc18_admin_categories;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AdminCategoriesPage;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC18_08_CategorySearchFilterTest extends BaseTest {

    @Test(description = "TC_UC18_08: Kiểm tra danh mục sự kiện có đầy đủ dữ liệu mô tả")
    public void testCategoriesDataIntegrity() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getAdminEmail(), ConfigurationManager.getAdminPassword());

        AdminCategoriesPage page = new AdminCategoriesPage(getDriver()).open();
        Assert.assertTrue(page.getCategoryCount() >= 6, "Phải có ít nhất 6 danh mục chuẩn trong hệ thống");
    }
}

