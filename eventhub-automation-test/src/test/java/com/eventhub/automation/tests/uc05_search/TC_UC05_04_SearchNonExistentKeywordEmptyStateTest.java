package com.eventhub.automation.tests.uc05_search;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.pages.ExplorePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC05_04_SearchNonExistentKeywordEmptyStateTest extends BaseTest {

    @Test(description = "TC_UC05_04: Tìm kiếm với từ khóa không tồn tại trả về danh sách trống")
    public void testSearchNonExistentKeywordEmptyState() {
        ExplorePage explorePage = new ExplorePage(getDriver()).open();
        
        // Từ khóa chắc chắn không có
        explorePage.search("TuKhoaKhongTonTai_99999");

        Assert.assertTrue(explorePage.isEmptyStateDisplayed(), "Phải hiển thị thông báo không tìm thấy sự kiện nào");
        Assert.assertEquals(explorePage.getEventCardCount(), 0, "Số lượng sự kiện phải bằng 0");
    }
}

