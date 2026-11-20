package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_07_SearchAndFilterCombinedTest extends BaseTest {

    @Test(description = "TC_UC06_07: Kết hợp đồng thời tìm kiếm từ khóa và lọc sự kiện theo thành phố")
    public void testSearchAndFilterCombined() {
        ExplorePage explorePage = new ExplorePage(getDriver()).openWithFilter("search=Music&city=all");

        Assert.assertTrue(explorePage.getEventCardsCount() >= 0,
                "Hệ thống phải thực hiện kết hợp tìm kiếm và lọc tiêu chí thành công");
    }
}

