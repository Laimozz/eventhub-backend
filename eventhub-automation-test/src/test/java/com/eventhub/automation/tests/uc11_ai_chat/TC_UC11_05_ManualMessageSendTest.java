package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_05_ManualMessageSendTest extends BaseTest {

    @Test(description = "TC11_05: Nhập câu hỏi thủ công và gửi tin nhắn thành công")
    public void testManualMessageSend() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login("vol.customer2@eventhub.test", ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        String question = "Làm thế nào để tôi xem lại vé đã đặt?";
        chatbot.enterQuestion(question);
        Assert.assertTrue(chatbot.isSendButtonEnabled(), "Nút gửi phải được kích hoạt sau khi nhập câu hỏi");

        // Gửi câu hỏi
        chatbot.clickSend();
        chatbot.waitForUserMessage();

        // Tin nhắn xuất hiện trong khung chat
        Assert.assertTrue(chatbot.isUserMessageDisplayed(), "Tin nhắn của người dùng phải hiển thị trong khung chat");
        Assert.assertTrue(chatbot.getLastUserMessageText().contains(question), "Nội dung tin nhắn người dùng phải khớp với câu hỏi vừa nhập");

        // Ô textarea phải được xóa trắng sau khi gửi
        Assert.assertEquals(chatbot.getQuestionInputText(), "", "Ô nhập câu hỏi phải được làm sạch sau khi gửi");

        // Chờ trợ lý AI xử lý và phản hồi hoàn tất
        chatbot.waitForAssistantMessage();
        Assert.assertTrue(chatbot.isAssistantMessageDisplayed(), "Trợ lý AI phải phản hồi câu hỏi thủ công của người dùng");
        String aiReply = chatbot.getLastAssistantMessageText();
        Assert.assertFalse(aiReply.isEmpty(), "Câu trả lời của trợ lý AI phải có nội dung");
    }
}

