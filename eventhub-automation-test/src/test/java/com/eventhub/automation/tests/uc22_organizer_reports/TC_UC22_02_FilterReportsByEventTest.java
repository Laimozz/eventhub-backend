package com.eventhub.automation.tests.uc22_organizer_reports;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.OrganizerReportsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC22_02_FilterReportsByEventTest extends BaseTest {

    @Test(description = "TC_UC22_02: Ban tổ chức lọc thống kê theo một sự kiện cụ thể từ danh sách dropdown")
    public void testFilterReportsByEvent() {
        // Đăng nhập tài khoản Organizer
        new LoginPage(getDriver()).open().login(
                ConfigurationManager.getOrganizerEmail(),
                ConfigurationManager.getOrganizerPassword()
        );

        OrganizerReportsPage reportsPage = new OrganizerReportsPage(getDriver()).open();

        int eventCount = reportsPage.getEventFilterOptionCount();
        if (eventCount > 1) {
            // Chọn sự kiện thứ hai trong danh sách
            reportsPage.selectEvent(1);
            Assert.assertTrue(reportsPage.isRevenueMetricDisplayed(), "Dữ liệu thống kê sự kiện đã chọn phải tải thành công");
        } else {
            // Trường hợp tài khoản chưa có sự kiện riêng lẻ nào ngoài Tất cả
            Assert.assertTrue(eventCount >= 1, "Dropdown phải chứa ít nhất tùy chọn 'Tất cả sự kiện'");
        }
    }
}

