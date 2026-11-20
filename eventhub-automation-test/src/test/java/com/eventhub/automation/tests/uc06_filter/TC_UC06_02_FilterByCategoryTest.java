package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_02_FilterByCategoryTest extends BaseTest {

    @Test(description = "TC_UC06_02: Lọc sự kiện theo danh mục thể loại (CategoryId = 19)")
    public void testFilterByCategory() {
        ExplorePage explorePage = new ExplorePage(getDriver()).openWithFilter("categoryId=19");

        Assert.assertTrue(explorePage.getEventCardsCount() > 0, "Phải có sự kiện thuộc danh mục 19 được hiển thị");
    }
}

