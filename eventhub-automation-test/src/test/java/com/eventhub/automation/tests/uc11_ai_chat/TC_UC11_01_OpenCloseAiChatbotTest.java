package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_01_OpenCloseAiChatbotTest extends BaseTest {

    @Test(description = "TC11_01: Mở và đóng cửa sổ Chatbot AI (Launcher, nút 'X', phím Escape)")
    public void testOpenCloseAiChatbot() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        Assert.assertTrue(chatbot.isLauncherDisplayed(), "Nút launcher 'Hỏi EventHub AI' phải hiển thị khi đã đăng nhập");

        // 1. Mở Chatbot
        chatbot.openChatbot();
        Assert.assertTrue(chatbot.isPanelDisplayed(), "Cửa sổ Chatbot AI phải hiển thị sau khi click nút launcher");
        Assert.assertTrue(chatbot.getPanelTitle().contains("EventHub"), "Tiêu đề panel phải chứa tên 'EventHub AI'");

        // 2. Đóng bằng nút 'X'
        chatbot.closeChatbot();
        Assert.assertFalse(chatbot.isPanelDisplayed(), "Cửa sổ Chatbot AI phải đóng lại sau khi click nút 'X'");

        // 3. Mở lại và đóng bằng phím Escape
        chatbot.openChatbot();
        Assert.assertTrue(chatbot.isPanelDisplayed(), "Cửa sổ Chatbot AI phải mở lại thành công");
        chatbot.closeByEscape();
        Assert.assertFalse(chatbot.isPanelDisplayed(), "Cửa sổ Chatbot AI phải đóng lại sau khi nhấn phím Escape");
    }
}

