package data;

import model.Order;
import util.OrderStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Statistics.java
 * يحسب كل الإحصائيات والتحليلات من قائمة الطلبات.
 *
 * أنواع التحليلات:
 *   - متوسط/أعلى/أقل قيمة طلب
 *   - توزيع الحالات (لكل حالة كم طلب)
 *   - توزيع أنواع المنتجات
 *   - أفضل عميل (الأكثر إنفاقاً)
 *   - أكثر منتج مبيعاً
 *   - مبيعات حسب اليوم (آخر 30 يوم)
 *   - تنبيهات ذكية
 */
public class Statistics {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderStore store;

    public Statistics(OrderStore store) {
        this.store = store;
    }

    // ── الأرقام الأساسية ─────────────────────────────────────────────────────

    /** متوسط قيمة الطلب. */
    public double averageOrderValue() {
        if (store.size() == 0) return 0;
        return store.totalRevenue() / store.size();
    }

    /** أعلى مبلغ في طلب. */
    public double highestOrder() {
        double max = 0;
        for (Order o : store.getAll()) {
            if (o.getTotal() > max) max = o.getTotal();
        }
        return max;
    }

    /** أقل مبلغ في طلب. */
    public double lowestOrder() {
        if (store.size() == 0) return 0;
        double min = Double.MAX_VALUE;
        for (Order o : store.getAll()) {
            if (o.getTotal() < min) min = o.getTotal();
        }
        return min;
    }

    /** مجموع الكميات المباعة. */
    public int totalUnitsSold() {
        int sum = 0;
        for (Order o : store.getAll()) sum += o.getQuantity();
        return sum;
    }

    // ── التوزيعات ─────────────────────────────────────────────────────────────

    /** عدد الطلبات لكل حالة. الترتيب: مدفوع، مشحون، معلق، ملغي. */
    public Map<String, Integer> statusDistribution() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(OrderStatus.PAID,      0);
        m.put(OrderStatus.SHIPPED,   0);
        m.put(OrderStatus.PENDING,   0);
        m.put(OrderStatus.CANCELLED, 0);
        for (Order o : store.getAll()) {
            m.merge(o.getStatus(), 1, Integer::sum);
        }
        return m;
    }

    /** توزيع المبيعات حسب نوع المنتج. */
    public Map<String, Double> typeDistribution() {
        Map<String, Double> m = new LinkedHashMap<>();
        for (Order o : store.getAll()) {
            m.merge(o.getProductType(), o.getTotal(), Double::sum);
        }
        return sortByValueDesc(m);
    }

    // ── الأفضل ────────────────────────────────────────────────────────────────

    /** أفضل عميل (الأكثر إنفاقاً). يرجع null إذا ما في طلبات. */
    public String[] topCustomer() {
        Map<String, Double> spent = new HashMap<>();
        for (Order o : store.getAll()) {
            spent.merge(o.getCustomerName(), o.getTotal(), Double::sum);
        }
        if (spent.isEmpty()) return null;
        String winner = "";
        double max    = -1;
        for (var e : spent.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                winner = e.getKey();
            }
        }
        return new String[]{ winner, util.Money.format(max) };
    }

    /** أكثر منتج مبيعاً (مجموع الكميات المباعة). */
    public String[] topProduct() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Order o : store.getAll()) {
            counts.merge(o.getProductName(), o.getQuantity(), Integer::sum);
        }
        if (counts.isEmpty()) return null;
        String winner = "";
        int max = -1;
        for (var e : counts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                winner = e.getKey();
            }
        }
        return new String[]{ winner, max + " " + util.Lang.t("unit") };
    }

    // ── الزمني ────────────────────────────────────────────────────────────────

    /**
     * مبيعات آخر N يوم.
     * يرجع قائمة [التاريخ → إجمالي اليوم] مرتبة من الأقدم للأحدث.
     */
    public List<DayRevenue> revenuePerDay(int days) {
        // ننشئ خريطة لكل الأيام (حتى الأيام الفاضية تظهر بـ 0)
        Map<String, Double> daily = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            daily.put(today.minusDays(i).format(FMT), 0.0);
        }
        // نجمع الطلبات على أيامها
        for (Order o : store.getAll()) {
            if (daily.containsKey(o.getDate())) {
                daily.merge(o.getDate(), o.getTotal(), Double::sum);
            }
        }
        List<DayRevenue> result = new ArrayList<>();
        for (var e : daily.entrySet()) {
            result.add(new DayRevenue(e.getKey(), e.getValue()));
        }
        return result;
    }

    /** صف بسيط: تاريخ + مبلغ. */
    public static class DayRevenue {
        public final String date;
        public final double amount;
        public DayRevenue(String date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }

    // ── العدّ السريع ─────────────────────────────────────────────────────────

    public int countPending() {
        int c = 0;
        for (Order o : store.getAll()) if (OrderStatus.PENDING.equals(o.getStatus())) c++;
        return c;
    }

    public int countCancelled() {
        int c = 0;
        for (Order o : store.getAll()) if (OrderStatus.CANCELLED.equals(o.getStatus())) c++;
        return c;
    }

    // ── أدوات ─────────────────────────────────────────────────────────────────

    /** يرتب الخريطة تنازلياً حسب القيمة. */
    private static <K> Map<K, Double> sortByValueDesc(Map<K, Double> m) {
        List<Map.Entry<K, Double>> list = new ArrayList<>(m.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<K, Double> out = new LinkedHashMap<>();
        for (var e : list) out.put(e.getKey(), e.getValue());
        return out;
    }
}
