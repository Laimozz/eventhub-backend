package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_02_RoleWelcomePromptsTest extends BaseTest {

    @Test(description = "TC11_02: Hiển thị lời chào và câu hỏi gợi ý (Quick Prompts) theo vai trò")
    public void testRoleWelcomePrompts() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // Kiểm tra nhãn vai trò Customer
        String scope = chatbot.getScopeLabel();
        Assert.assertTrue(scope.contains("Khách hàng"), "Huy hiệu vai trò phải hiển thị 'Khách hàng'");

        // Kiểm tra màn hình chào mừng & tiêu đề
        Assert.assertTrue(chatbot.isWelcomeDisplayed(), "Màn hình chào mừng phải hiển thị khi chưa có tin nhắn");
        Assert.assertTrue(chatbot.getWelcomeTitle().contains("Trải nghiệm tiếp theo"),
                "Tiêu đề chào mừng của Khách hàng phải hiển thị 'Trải nghiệm tiếp theo của bạn là gì?'");

        // Kiểm tra 4 thẻ gợi ý nhanh (Quick Prompts)
        Assert.assertEquals(chatbot.getPromptButtonsCount(), 4,
                "Hệ thống phải hiển thị đủ 4 thẻ câu hỏi gợi ý nhanh (Quick Prompts)");
    }
}

