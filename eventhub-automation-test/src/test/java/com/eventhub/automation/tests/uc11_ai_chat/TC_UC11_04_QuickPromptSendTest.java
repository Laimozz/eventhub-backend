package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_04_QuickPromptSendTest extends BaseTest {

    @Test(description = "TC11_04: Gửi câu hỏi nhanh từ danh sách Quick Prompt")
    public void testQuickPromptSend() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // Nhấp vào gợi ý đầu tiên "01 Khám phá sự kiện"
        chatbot.clickPromptByIndex(0);
        chatbot.waitForUserMessage();

        // Tin nhắn người dùng phải xuất hiện trong khung chat
        Assert.assertTrue(chatbot.isUserMessageDisplayed(), "Tin nhắn câu hỏi từ Quick Prompt phải xuất hiện trong khung chat");
        String messageText = chatbot.getLastUserMessageText();
        Assert.assertTrue(messageText.contains("sự kiện") || messageText.contains("bán vé"),
                "Nội dung tin nhắn phải tương ứng với prompt 'Khám phá sự kiện'");

        // Chờ trợ lý AI phản hồi câu hỏi hoàn tất
        chatbot.waitForAssistantMessage();
        Assert.assertTrue(chatbot.isAssistantMessageDisplayed(), "Trợ lý AI phải trả lời câu hỏi của người dùng");
        String aiReply = chatbot.getLastAssistantMessageText();
        Assert.assertFalse(aiReply.isEmpty(), "Nội dung phản hồi của trợ lý AI không được để trống");
    }
}

