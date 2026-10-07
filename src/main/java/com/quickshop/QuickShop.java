package com.quickshop;

public class QuickShop {

    public static String getWelcomeMessage() {
        return "Welcome to QuickShop";
    }

    public static String getHealthStatus() {
        return "QuickShop is healthy";
    }

    public static void main(String[] args) {
        System.out.println(getWelcomeMessage());
        System.out.println(getHealthStatus());
    }
}
