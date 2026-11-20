package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_05_FilterNoMatchEmptyStateTest extends BaseTest {

    @Test(description = "TC_UC06_05: Lọc với tổ hợp tiêu chí không có kết quả phù hợp hiển thị giao diện trống")
    public void testFilterNoMatchEmptyState() {
        // Lọc với thành phố không tồn tại sự kiện
        ExplorePage explorePage = new ExplorePage(getDriver()).openWithFilter("city=NonExistentCity999");

        Assert.assertTrue(explorePage.isEmptyStateDisplayed() || explorePage.getEventCardsCount() == 0,
                "Khi lọc không có sự kiện nào thỏa mãn phải hiển thị trạng thái trống (Empty state)");
    }
}

