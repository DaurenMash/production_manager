package com.prodman.employeeservice.util;

/**
 * Нормализация телефона.
 * Ввод: 1231231212 (10 цифр).
 * Хранение: +71231231212 (E.164).
 * Отображение: +7 123 123 12 12.
 */
public final class PhoneUtils {

    private static final String COUNTRY_CODE = "+7";

    private PhoneUtils() {
    }

    /** 1231231212 → +71231231212 */
    public static String toE164(String input) {
        if (input == null) return null;
        String digits = input.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return COUNTRY_CODE + digits;
        }
        if (digits.length() == 11 && digits.startsWith("7")) {
            return "+" + digits;
        }
        throw new IllegalArgumentException("Телефон должен содержать 10 цифр (или 11, начиная с 7)");
    }

    /** +71231231212 → +7 123 123 12 12 */
    public static String toDisplay(String e164) {
        if (e164 == null || e164.length() < 12) return e164;
        String d = e164.startsWith("+") ? e164.substring(1) : e164;
        return "+" + d.charAt(0) + " " + d.substring(1, 4) + " " + d.substring(4, 7)
                + " " + d.substring(7, 9) + " " + d.substring(9, 11);
    }
}