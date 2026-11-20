package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_08_NewConversationResetTest extends BaseTest {

    @Test(description = "TC11_08: Tạo cuộc hội thoại mới (New Chat) và làm mới ngữ cảnh")
    public void testNewConversationReset() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getOrganizerEmail(), ConfigurationManager.getOrganizerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // Gửi một tin nhắn trước
        chatbot.enterQuestion("Chào trợ lý EventHub");
        chatbot.clickSend();
        chatbot.waitForUserMessage();
        Assert.assertTrue(chatbot.isUserMessageDisplayed(), "Tin nhắn phải xuất hiện trước khi tạo mới");

        // Chờ trợ lý AI phản hồi xong hội thoại ban đầu
        chatbot.waitForAssistantMessage();
        Assert.assertTrue(chatbot.isAssistantMessageDisplayed(), "Trợ lý AI đã trả lời hội thoại ban đầu");

        // Nhấp nút 'Hội thoại mới' (+)
        chatbot.clickNewChat();

        // Màn hình phải được làm mới về Welcome Screen
        Assert.assertTrue(chatbot.isWelcomeDisplayed(),
                "Sau khi tạo hội thoại mới, khung chat phải trở về màn hình chào mừng ban đầu");
        Assert.assertFalse(chatbot.isUserMessageDisplayed(),
                "Các tin nhắn của cuộc hội thoại cũ không được hiển thị trong cuộc trò chuyện mới");
        Assert.assertFalse(chatbot.isAssistantMessageDisplayed(),
                "Tin nhắn của trợ lý cũ không được hiển thị trong cuộc trò chuyện mới");
    }
}

