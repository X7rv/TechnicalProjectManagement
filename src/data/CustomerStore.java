package data;

import model.Customer;
import model.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CustomerStore.java
 * مستودع العملاء - يُحسب تلقائياً من قائمة الطلبات.
 * كل ما تغيّرت الطلبات، نعيد بناء قائمة العملاء.
 */
public class CustomerStore {

    private final List<Customer> customers = new ArrayList<>();
    private final OrderStore orderStore;

    public CustomerStore(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    public List<Customer> getAll() { return Collections.unmodifiableList(customers); }
    public int size() { return customers.size(); }

    /**
     * إعادة بناء قائمة العملاء من الطلبات الحالية.
     * كل عميل يجمع: مجموع المشتريات، عدد الطلبات، آخر إيميل، الرتبة.
     */
    public void syncFromOrders() {
        customers.clear();

        Map<String, Double>  spent  = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String>  emails = new LinkedHashMap<>();

        for (Order o : orderStore.getAll()) {
            String name = o.getCustomerName();
            spent.merge(name, o.getTotal(), Double::sum);
            counts.merge(name, 1, Integer::sum);
            // نحفظ آخر إيميل غير فاضي
            String e = o.getCustomerEmail();
            if (e != null && !e.isEmpty()) emails.put(name, e);
        }

        for (String name : spent.keySet()) {
            double total   = spent.get(name);
            int    orderCt = counts.get(name);
            String email   = emails.getOrDefault(name, "");
            String rank    = rankFor(total);
            customers.add(new Customer(name, email, total, orderCt, rank));
        }
    }

    /** تحديد الرتبة حسب إجمالي المشتريات. */
    private String rankFor(double total) {
        if (total >= 50000) return "Platinum";
        if (total >= 10000) return "Gold";
        if (total >= 3000)  return "Silver";
        return "Bronze";
    }
}
