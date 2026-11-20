package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_08_FilterResetDefaultTest extends BaseTest {

    @Test(description = "TC_UC06_08: Đặt lại bộ lọc (Reset) quay về danh sách sự kiện đầy đủ ban đầu")
    public void testFilterResetDefault() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        int initialCount = explorePage.getEventCardCount();

        // Áp dụng bộ lọc
        explorePage.selectCity("Đà Nẵng");

        // Chọn lại Tất cả địa điểm
        explorePage.selectCity("Tất cả địa điểm");
        int resetCount = explorePage.getEventCardCount();

        Assert.assertEquals(resetCount, initialCount, "Sau khi reset địa điểm, số lượng sự kiện phải quay về ban đầu");
    }
}

