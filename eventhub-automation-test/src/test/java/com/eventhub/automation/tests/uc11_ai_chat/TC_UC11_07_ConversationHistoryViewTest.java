package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_07_ConversationHistoryViewTest extends BaseTest {

    @Test(description = "TC11_07: Xem danh sách lịch sử trò chuyện và quay lại khung chat")
    public void testConversationHistoryView() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // 1. Mở xem Lịch sử hội thoại
        chatbot.openHistory();
        Assert.assertTrue(chatbot.isHistoryDisplayed(), "Giao diện lịch sử hội thoại phải được hiển thị");

        // 2. Quay lại giao diện trò chuyện
        chatbot.clickBackToChat();
        Assert.assertFalse(chatbot.isHistoryDisplayed(), "Giao diện lịch sử phải đóng lại sau khi click quay lại");
    }
}

