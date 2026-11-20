package com.eventhub.automation.tests.uc11_ai_chat;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import com.eventhub.automation.pages.AiChatbotComponent;
import com.eventhub.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC_UC11_06_MaximizeMinimizeChatbotTest extends BaseTest {

    @Test(description = "TC11_06: Phóng to và thu nhỏ cửa sổ chat (Maximize / Minimize)")
    public void testMaximizeMinimizeChatbot() {
        LoginPage loginPage = new LoginPage(getDriver()).open();
        loginPage.login(ConfigurationManager.getCustomerEmail(), ConfigurationManager.getCustomerPassword());

        AiChatbotComponent chatbot = new AiChatbotComponent(getDriver());
        chatbot.openChatbot();

        // Ban đầu ở kích thước thường
        Assert.assertFalse(chatbot.isExpanded(), "Cửa sổ chat ban đầu không được ở trạng thái phóng to");

        // 1. Phóng to
        chatbot.toggleExpand();
        Assert.assertTrue(chatbot.isExpanded(), "Cửa sổ chat phải chuyển sang trạng thái phóng to (expanded)");

        // 2. Thu nhỏ lại
        chatbot.toggleExpand();
        Assert.assertFalse(chatbot.isExpanded(), "Cửa sổ chat phải trở lại kích thước gọn gàng bình thường");
    }
}

