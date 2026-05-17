package util;

/**
 * OrderStatus.java
 * يحدد كل القيم الممكنة لحالة الطلب.
 *
 * مهم: الحالة تُخزَّن دائماً بالعربي (مفاتيح ثابتة) بغض النظر عن لغة الواجهة.
 * هذا يضمن أن منطق isPaid وstats يعمل في كل اللغات.
 */
public final class OrderStatus {

    public static final String PAID      = "مدفوع";
    public static final String SHIPPED   = "مشحون";
    public static final String PENDING   = "معلق";
    public static final String CANCELLED = "ملغي";

    /** كل الحالات بترتيب العرض. */
    public static final String[] ALL = { PAID, SHIPPED, PENDING, CANCELLED };

    /** هل الحالة محسوبة كمدفوع/مكتمل (للإحصائيات والمخزون)؟ */
    public static boolean isPaidStatus(String status) {
        return PAID.equals(status) || SHIPPED.equals(status);
    }

    /** يرجّع نص الحالة معروضاً باللغة الحالية. */
    public static String display(String status) {
        if (status == null) return "";
        return switch (status) {
            case PAID      -> Lang.t("status_paid");
            case SHIPPED   -> Lang.t("status_shipped");
            case PENDING   -> Lang.t("status_pending");
            case CANCELLED -> Lang.t("status_cancelled");
            default        -> status;
        };
    }

    /** يحوّل نص معروض (بأي لغة) إلى المفتاح المخزّن (عربي). */
    public static String fromDisplay(String displayed) {
        if (displayed == null) return PENDING;
        if (displayed.equals(Lang.t("status_paid"))     || displayed.equals(PAID))      return PAID;
        if (displayed.equals(Lang.t("status_shipped")) || displayed.equals(SHIPPED))   return SHIPPED;
        if (displayed.equals(Lang.t("status_pending")) || displayed.equals(PENDING))   return PENDING;
        if (displayed.equals(Lang.t("status_cancelled"))|| displayed.equals(CANCELLED))return CANCELLED;
        return displayed;
    }

    private OrderStatus() {}
}
