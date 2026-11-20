package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_07_SearchClickEventNavigateToDetailTest extends BaseTest {

    @Test(description = "TC_UC05_07: Nhấn vào kết quả sự kiện chuyển hướng đến trang Chi tiết sự kiện")
    public void testSearchClickEventNavigateToDetail() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        explorePage.search("Concert");

        Assert.assertTrue(explorePage.getEventCardCount() > 0);

        // Click vào thẻ sự kiện đầu tiên
        explorePage.clickFirstEventCard();

        // Kiểm tra điều hướng vào chi tiết sự kiện
        boolean onEventDetail = ExpectedConditions.urlContains("/events/").apply(getDriver());
        Assert.assertTrue(onEventDetail, "Phải chuyển hướng đến URL chi tiết sự kiện /events/{id}");
    }
}

