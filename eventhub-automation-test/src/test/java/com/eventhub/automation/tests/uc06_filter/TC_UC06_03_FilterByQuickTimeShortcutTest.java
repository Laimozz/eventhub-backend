package com.eventhub.automation.tests.uc06_filter;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC06_03_FilterByQuickTimeShortcutTest extends BaseTest {

    @Test(description = "TC_UC06_03: Lọc sự kiện theo phím tắt thời gian nhanh (Tháng này)")
    public void testFilterByQuickTimeShortcut() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        
        // Lọc tháng này
        explorePage.filterByQuickTime("this_month");

        Assert.assertTrue(getDriver().getCurrentUrl().contains("time=this_month"), "URL phải chứa tham số time=this_month");
    }
}

