package com.example.a7506_project.util;

public final class Validators {

    private Validators() {
    }

    public static String validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return "Nickname cannot be empty.";
        }
        String trimmed = nickname.trim();
        if (trimmed.length() < 3) {
            return "Nickname must be at least 3 characters.";
        }
        if (trimmed.length() > 20) {
            return "Nickname must be at most 20 characters.";
        }
        if (!trimmed.matches("[a-zA-Z0-9_]+")) {
            return "Nickname can only contain letters, numbers, and underscores.";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        return null;
    }

    public static String validateWhatsapp(String whatsapp) {
        if (whatsapp == null || whatsapp.trim().isEmpty()) {
            return "WhatsApp number cannot be empty.";
        }
        String digits = whatsapp.trim().replaceAll("[\\s\\-()+]", "");
        if (!digits.matches("\\d+")) {
            return "WhatsApp must contain only digits.";
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return "WhatsApp must be 8–15 digits.";
        }
        return null;
    }

    public static String validateItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Item name cannot be empty.";
        }
        if (name.trim().length() > 100) {
            return "Item name must be at most 100 characters.";
        }
        return null;
    }

    public static String validatePriceCents(long priceCents) {
        if (priceCents <= 0) {
            return "Price must be greater than zero.";
        }
        return null;
    }
}
