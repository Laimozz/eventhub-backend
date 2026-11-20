package com.eventhub.automation.tests.uc22_organizer_reports;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerReportsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC22_04_OrganizerReportsExportCsvButtonTest extends BaseTest {

    @Test(description = "TC_UC22_04: Kiểm tra nút Xuất file CSV và nút Làm mới dữ liệu hoạt động bình thường")
    public void testOrganizerReportsExportCsvButton() {
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerReportsPage reportsPage = new OrganizerReportsPage(getDriver()).open();

        Assert.assertTrue(reportsPage.isExportCsvButtonEnabled(), "Nút xuất CSV phải sẵn sàng click");

        // Bấm làm mới
        reportsPage.clickRefresh();
        Assert.assertTrue(reportsPage.isPageTitleDisplayed(), "Trang vẫn hiển thị bình thường sau khi làm mới");
    }
}

