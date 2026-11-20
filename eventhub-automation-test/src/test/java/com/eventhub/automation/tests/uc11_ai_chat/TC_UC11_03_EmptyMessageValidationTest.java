package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_03_EmptyMessageValidationTest extends BaseTest {

    @Test(description = "TC11_03: Chặn gửi tin nhắn khi ô nhập trống hoặc chỉ chứa khoảng trắng")
    public void testEmptyMessageValidation() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // 1. Ô nhập rỗng ban đầu -> nút gửi phải bị disabled
        Assert.assertFalse(chatbot.isSendButtonEnabled(), "Nút gửi câu hỏi phải bị vô hiệu hóa khi ô nhập trống");

        // 2. Nhập khoảng trắng -> nút gửi vẫn phải bị disabled
        chatbot.enterQuestion("     ");
        Assert.assertFalse(chatbot.isSendButtonEnabled(), "Nút gửi câu hỏi phải bị vô hiệu hóa khi chỉ nhập khoảng trắng");

        // Không phát sinh tin nhắn trong khung chat
        Assert.assertFalse(chatbot.isUserMessageDisplayed(), "Khung chat không được hiển thị tin nhắn khi dữ liệu rỗng");
    }
}

