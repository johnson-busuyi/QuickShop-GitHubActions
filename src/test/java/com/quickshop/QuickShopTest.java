package com.quickshop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuickShopTest {

    @Test
    void testWelcomeMessage() {
        assertEquals("Welcome to QuickShop", QuickShop.getWelcomeMessage());
    }

    @Test
    void testHealthStatus() {
        assertEquals("QuickShop is healthy", QuickShop.getHealthStatus());
    }
}
