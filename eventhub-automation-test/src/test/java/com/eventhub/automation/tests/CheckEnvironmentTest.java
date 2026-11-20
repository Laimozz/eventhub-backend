package com.eventhub.automation.tests;

import com.eventhub.automation.BaseTest;
import com.eventhub.automation.config.ConfigurationManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckEnvironmentTest extends BaseTest {

    @Test(description = "Verify that the browser starts successfully and opens EventHub home page")
    public void testOpenEventHubApp() {
        getDriver().get(ConfigurationManager.getBaseUrl());
        String currentUrl = getDriver().getCurrentUrl();
        System.out.println("Opened URL: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("localhost"), "URL should point to local environment");
    }
}

