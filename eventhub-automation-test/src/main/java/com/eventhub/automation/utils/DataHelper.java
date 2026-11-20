package com.eventhub.automation.utils;

import java.util.Random;

public class DataHelper {
    private static final Random random = new Random();

    public static String generateRandomEmail() {
        return "auto_" + System.currentTimeMillis() + "@eventhub.test";
    }

    public static String generateRandomPhone() {
        return "09" + String.format("%08d", random.nextInt(100000000));
    }

    public static String generateRandomName() {
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi"};
        String[] middleNames = {"Văn", "Thị", "Hải", "Đăng", "Minh", "Quốc", "Gia"};
        String[] lastNames = {"Nam", "Phúc", "Bảo", "Anh", "Vy", "Hân", "Linh", "Khang"};
        return firstNames[random.nextInt(firstNames.length)] + " " +
               middleNames[random.nextInt(middleNames.length)] + " " +
               lastNames[random.nextInt(lastNames.length)];
    }
}

