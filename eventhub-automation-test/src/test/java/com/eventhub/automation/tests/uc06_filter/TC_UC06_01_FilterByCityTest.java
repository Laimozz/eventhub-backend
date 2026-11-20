package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_01_FilterByCityTest extends BaseTest {

    @Test(description = "TC_UC06_01: Lọc sự kiện theo thành phố tổ chức (ví dụ: Hà Nội)")
    public void testFilterByCity() {
        ExplorePage explorePage = new ExplorePage(getDriver()).openWithFilter("city=Hà+Nội");

        Assert.assertTrue(explorePage.getEventCardsCount() > 0, "Phải có sự kiện tại thành phố Hà Nội được hiển thị");
    }
}

