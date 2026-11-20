package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_05_SearchEmptyKeywordShowsAllEventsTest extends BaseTest {

    @Test(description = "TC_UC05_05: Tìm kiếm khi để ô tìm kiếm trống hiển thị toàn bộ danh sách sự kiện")
    public void testSearchEmptyKeywordShowsAllEvents() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();

        explorePage.search("");

        Assert.assertTrue(explorePage.getEventCardsCount() > 0, "Khi không nhập từ khóa tìm kiếm, hệ thống hiển thị danh sách tất cả sự kiện");
    }
}

