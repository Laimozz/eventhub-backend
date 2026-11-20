package com.eventhub.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Warning: config.properties not found in classpath. Using default values.");
            }
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static String getBaseUrl() {
        return get("base.url", "http://localhost:5173");
    }

    public static String getBrowser() {
        return get("browser", "chrome").toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    public static int getImplicitWait() {
        return Integer.parseInt(get("implicit.wait", "10"));
    }

    public static int getExplicitWait() {
        return Integer.parseInt(get("explicit.wait", "15"));
    }

    public static int getStepDelay() {
        return Integer.parseInt(get("step.delay", "500"));
    }

    public static String getAdminEmail() {
        return get("admin.email", "seed.admin@eventhub.local");
    }

    public static String getAdminPassword() {
        return get("admin.password", "EventHub@123");
    }

    public static String getCustomerEmail() {
        return get("customer.email", "vol.customer1@eventhub.test");
    }

    public static String getCustomerPassword() {
        return get("customer.password", "EventHub@123");
    }

    public static String getOrganizerEmail() {
        return get("organizer.email", "vol.organizer1@eventhub.test");
    }

    public static String getOrganizerPassword() {
        return get("organizer.password", "EventHub@123");
    }

    public static String getStaffEmail() {
        return get("staff.email", "vol.staff1@eventhub.test");
    }

    public static String getStaffPassword() {
        return get("staff.password", "EventHub@123");
    }

    public static String getLockedCustomerEmail() {
        return get("locked.customer.email", "vol.customer36@eventhub.test");
    }

    public static String getSeedCustomerEmail() {
        return get("seed.customer.email", "seed.customer1@eventhub.local");
    }

    public static String getSeedOrganizerEmail() {
        return get("seed.organizer.email", "seed.organizer1@eventhub.local");
    }

    public static String getSeedStaffEmail() {
        return get("seed.staff.email", "seed.staff1@eventhub.local");
    }

    public static String getDbUrl() {
        return get("db.url", "jdbc:postgresql://localhost:5432/eventhub_db");
    }

    public static String getDbUsername() {
        return get("db.username", "postgres");
    }

    public static String getDbPassword() {
        return get("db.password", "30032004");
    }

    public static boolean isDbRollbackEnabled() {
        return Boolean.parseBoolean(get("db.rollback.enabled", "true"));
    }
}

