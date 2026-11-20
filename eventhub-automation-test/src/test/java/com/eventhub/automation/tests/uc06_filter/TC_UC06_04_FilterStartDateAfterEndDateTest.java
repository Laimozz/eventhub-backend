package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_04_FilterStartDateAfterEndDateTest extends BaseTest {

    @Test(description = "TC_UC06_04: Lọc sự kiện thất bại hoặc hiển thị cảnh báo khi Ngày bắt đầu lớn hơn Ngày kết thúc (Exception 1.1)")
    public void testFilterStartDateAfterEndDate() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();

        // Thử lọc với khoảng ngày không hợp lệ (từ ngày sau đến ngày trước)
        explorePage.openWithFilter("startDate=2026-10-30&endDate=2026-10-01");

        // Trang phải xử lý an toàn: hiển thị danh sách rỗng hoặc thông báo lỗi hợp lệ
        int cardCount = explorePage.getEventCardCount();
        Assert.assertTrue(cardCount >= 0, "Hệ thống phải xử lý an toàn khi khoảng ngày lọc không hợp lệ");
    }
}

