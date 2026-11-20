package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_01_SearchExactKeywordTest extends BaseTest {

    @Test(description = "TC_UC05_01: Tìm kiếm sự kiện thành công với từ khóa chính xác")
    public void testSearchExactKeyword() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        
        // Tìm kiếm sự kiện Âm nhạc / Concert
        explorePage.search("Concert");

        Assert.assertTrue(explorePage.getEventCardCount() > 0, "Phải tìm thấy ít nhất 1 sự kiện chứa từ khóa Concert");
    }
}

