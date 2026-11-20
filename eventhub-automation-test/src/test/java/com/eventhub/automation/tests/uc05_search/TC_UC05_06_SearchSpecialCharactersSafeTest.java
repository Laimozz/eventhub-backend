package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_06_SearchSpecialCharactersSafeTest extends BaseTest {

    @Test(description = "TC_UC05_06: Tìm kiếm chứa ký tự đặc biệt được xử lý an toàn (SQL Injection / XSS Prevention)")
    public void testSearchSpecialCharactersSafe() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();

        // Thử tìm kiếm với ký tự payload SQL/XSS
        explorePage.search("'<script>alert(1)</script>' OR 1=1 --");

        // Trang web không bị crash, hiển thị empty state an toàn
        Assert.assertTrue(explorePage.isEmptyStateDisplayed() || explorePage.getEventCardsCount() == 0,
                "Ứng dụng phải xử lý an toàn khi nhập ký tự đặc biệt, không sinh lỗi hệ thống");
    }
}

