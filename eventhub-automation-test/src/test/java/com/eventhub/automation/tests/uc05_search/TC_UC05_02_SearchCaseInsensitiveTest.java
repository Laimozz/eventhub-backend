package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_02_SearchCaseInsensitiveTest extends BaseTest {

    @Test(description = "TC_UC05_02: Tìm kiếm không phân biệt chữ hoa, chữ thường (Case-insensitive)")
    public void testSearchCaseInsensitive() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        
        // Tìm chữ thường
        explorePage.search("acoustic");
        int countLower = explorePage.getEventCardCount();

        // Tìm chữ hoa
        explorePage.search("ACOUSTIC");
        int countUpper = explorePage.getEventCardCount();

        Assert.assertEquals(countLower, countUpper, "Số lượng kết quả tìm kiếm chữ hoa và chữ thường phải như nhau");
    }
}

