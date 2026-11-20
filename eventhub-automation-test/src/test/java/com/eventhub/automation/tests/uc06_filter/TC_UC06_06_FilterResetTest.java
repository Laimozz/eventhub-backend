package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_06_FilterResetTest extends BaseTest {

    @Test(description = "TC_UC06_06: Kiểm tra tính năng Đặt lại bộ lọc (Reset Filter)")
    public void testFilterReset() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        
        // 1. Áp dụng một bộ lọc
        explorePage.filterByQuickTime("this_week");
        Assert.assertTrue(getDriver().getCurrentUrl().contains("time="));

        // 2. Nhấn xóa bộ lọc
        explorePage.clickResetFilter();

        // 3. Kiểm tra URL đã xóa tham số lọc
        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("time=this_week"), "Sau khi xóa bộ lọc URL không còn chứa tham số lọc cũ");
    }
}

