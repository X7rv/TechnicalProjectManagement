package util;

/**
 * Money.java
 * تنسيق المبالغ. وحدة العملة ثابتة "ر.س" حالياً
 * لكن يمكن تغييرها لاحقاً من الإعدادات.
 */
public final class Money {

    private Money() {}

    /** يرجّع المبلغ بصيغة عرض جميلة مع رمز العملة. */
    public static String format(double amount) {
        return String.format("%,.0f %s", amount, currency());
    }

    /** نفس الصيغة لكن مع 2 خانات عشرية. */
    public static String formatExact(double amount) {
        return String.format("%,.2f %s", amount, currency());
    }

    /** رمز العملة الحالي. */
    public static String currency() {
        // مستقبلاً نقدر نضيف Settings.currency
        return "ر.س";
    }
}
