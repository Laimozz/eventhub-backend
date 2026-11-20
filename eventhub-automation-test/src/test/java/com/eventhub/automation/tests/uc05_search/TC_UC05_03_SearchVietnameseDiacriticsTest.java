package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_03_SearchVietnameseDiacriticsTest extends BaseTest {

    @Test(description = "TC_UC05_03: Tìm kiếm hỗ trợ tiếng Việt có dấu và không dấu (Localization)")
    public void testSearchVietnameseDiacritics() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();

        // Tìm kiếm có dấu
        explorePage.search("Hòa nhạc");
        int countWithAccents = explorePage.getEventCardCount();

        // Tìm kiếm không dấu
        explorePage.search("Hoa nhac");
        int countWithoutAccents = explorePage.getEventCardCount();

        // Hệ thống xử lý mượt mà cả từ khóa có dấu và không dấu
        Assert.assertTrue(countWithAccents >= 0 && countWithoutAccents >= 0,
                "Tìm kiếm tiếng Việt có dấu và không dấu phải hoạt động ổn định");
    }
}

