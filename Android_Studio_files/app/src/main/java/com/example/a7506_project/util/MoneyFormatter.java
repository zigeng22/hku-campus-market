package com.example.a7506_project.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String centsToHkd(long cents) {
        double hkd = cents / 100.0;
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-HK"));
        // use default HK locale formatting; fallback for emulators without full locale data
        if (fmt instanceof java.text.DecimalFormat) {
            ((java.text.DecimalFormat) fmt).applyPattern("HK$#,##0.00");
        }
        try {
            return fmt.format(hkd);
        } catch (Exception e) {
            return String.format(Locale.US, "HK$%.2f", hkd);
        }
    }

    public static long hkdToCents(double hkd) {
        return Math.round(hkd * 100.0);
    }

    public static long hkdToCents(String hkdText) {
        if (hkdText == null || hkdText.trim().isEmpty()) return 0;
        try {
            double value = Double.parseDouble(hkdText.trim());
            return hkdToCents(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
