package data;

import model.Order;
import util.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OrderStore.java
 * المخزن الرئيسي للطلبات. يحفظ على القرص (JSON) في كل تعديل.
 * يُعلِم المستمعين (Overview + Customers) عند أي تغيير.
 */
public class OrderStore {

    private final List<Order>    orders    = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final AtomicLong     idSeq     = new AtomicLong(1);
    private final UndoManager    undo      = new UndoManager();
    private ProductStore         productStore = null;   // اختياري - للربط مع المخزون
    private String lastError = null;

    public OrderStore() {
        loadFromDisk();
        Backup.runDailyBackup();
    }

    /** ربط مع مستودع المنتجات لتحديث المخزون تلقائياً. */
    public void setProductStore(ProductStore ps) {
        this.productStore = ps;
    }

    public UndoManager getUndoManager() { return undo; }

    private void loadFromDisk() {
        try {
            List<Order> loaded = JsonStorage.load();
            orders.clear();
            orders.addAll(loaded);
            long max = 0;
            for (Order o : orders) if (o.getDbId() > max) max = o.getDbId();
            idSeq.set(max + 1);
            applySort();
        } catch (IOException e) {
            lastError = e.getMessage();
            System.err.println("فشل تحميل الطلبات: " + e.getMessage());
        }
    }

    /** ترتيب الطلبات حسب Settings.orderSort */
    public void applySort() {
        switch (Settings.orderSort) {
            case "oldest"     -> orders.sort(Comparator.comparingLong(Order::getDbId));
            case "price_high" -> orders.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));
            case "price_low"  -> orders.sort(Comparator.comparingDouble(Order::getTotal));
            case "status"     -> orders.sort(Comparator.comparing(Order::getStatus));
            default           -> orders.sort((a, b) -> Long.compare(b.getDbId(), a.getDbId()));
        }
        notifyChange();
    }

    private boolean saveToDisk() {
        try {
            JsonStorage.save(orders);
            lastError = null;
            return true;
        } catch (IOException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public String getLastError() { return lastError; }

    // ── المستمعون ─────────────────────────────────────────────────────────────

    public void addChangeListener(Runnable r) { listeners.add(r); }
    private void notifyChange() { for (Runnable r : listeners) r.run(); }

    // ── العمليات الأساسية ────────────────────────────────────────────────────

    public boolean add(Order o) {
        o.setDbId(idSeq.getAndIncrement());
        orders.add(0, o);
        if (!saveToDisk()) {
            orders.remove(0);
            return false;
        }
        // ننقص المخزون إذا الطلب مدفوع/مشحون والمنتج موجود
        adjustStockForAdd(o);
        undo.recordAdd(o);
        notifyChange();
        return true;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= orders.size()) return false;
        Order removed = orders.remove(index);
        if (!saveToDisk()) {
            orders.add(index, removed);
            return false;
        }
        // نرجع الكمية للمخزون عند الحذف
        adjustStockForDelete(removed);
        undo.recordDelete(removed, index);
        notifyChange();
        return true;
    }

    /**
     * تعديل المخزون عند إضافة طلب - ينقص فقط إذا الحالة مدفوع/مشحون.
     */
    private void adjustStockForAdd(Order o) {
        if (productStore == null) return;
        if (!o.isPaid()) return;
        var product = productStore.findByName(o.getProductName());
        if (product == null) return;
        product.reduceStock(o.getQuantity());
        productStore.update(product);
    }

    /**
     * تعديل المخزون عند حذف طلب - يرجع الكمية إذا كان الطلب محسوب.
     */
    private void adjustStockForDelete(Order o) {
        if (productStore == null) return;
        if (!o.isPaid()) return;
        var product = productStore.findByName(o.getProductName());
        if (product == null) return;
        product.increaseStock(o.getQuantity());
        productStore.update(product);
    }

    public boolean update(Order o) {
        if (!saveToDisk()) return false;
        // ملاحظة: snapshot يجب أن يُلتقط قبل التعديل من OrdersPanel
        notifyChange();
        return true;
    }

    /**
     * تنفيذ التراجع. يرجع true إذا تم التراجع، false إذا ما في عمليات.
     */
    public boolean performUndo() {
        UndoManager.Op op = undo.pop();
        if (op == null) return false;

        switch (op.action) {
            case ADD -> {
                // التراجع عن الإضافة = حذف الطلب (ويرجع المخزون)
                orders.removeIf(o -> o.getDbId() == op.order.getDbId());
                adjustStockForDelete(op.order);
            }
            case DELETE -> {
                // التراجع عن الحذف = إعادة الطلب (وينقص المخزون مرة ثانية)
                int idx = Math.min(op.index, orders.size());
                orders.add(idx, op.order);
                adjustStockForAdd(op.order);
            }
            case UPDATE -> {
                // التراجع عن التعديل = استعادة الحالة السابقة
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getDbId() == op.order.getDbId()) {
                        // إذا الحالة تغيرت بين isPaid<->not, نعدل المخزون
                        boolean wasPaid = op.previousState.isPaid();
                        boolean isPaid  = op.order.isPaid();
                        if (wasPaid && !isPaid) {
                            // كان مدفوع وصار غير مدفوع → نرجع للمخزون لما نتراجع
                            adjustStockForAdd(op.previousState);
                        } else if (!wasPaid && isPaid) {
                            adjustStockForDelete(op.previousState);
                        }
                        orders.set(i, op.previousState);
                        break;
                    }
                }
            }
        }
        saveToDisk();
        notifyChange();
        return true;
    }

    /** يحذف كل الطلبات (للاستخدام من إعدادات → مسح البيانات). */
    public boolean clearAll() {
        List<Order> backup = new ArrayList<>(orders);
        orders.clear();
        idSeq.set(1);
        if (!saveToDisk()) {
            orders.addAll(backup);
            return false;
        }
        notifyChange();
        return true;
    }

    public Order get(int index) { return orders.get(index); }
    public List<Order> getAll() { return Collections.unmodifiableList(orders); }
    public int size()           { return orders.size(); }

    // ── الإحصائيات ────────────────────────────────────────────────────────────

    public double totalRevenue() {
        double sum = 0;
        for (Order o : orders) sum += o.getTotal();
        return sum;
    }

    /** عدد المدفوع + المشحون (لأن isPaid() تشمل الاثنين). */
    public long countPaid() {
        long count = 0;
        for (Order o : orders) if (o.isPaid()) count++;
        return count;
    }

    public int uniqueCustomerCount() {
        List<String> names = new ArrayList<>();
        for (Order o : orders) {
            if (!names.contains(o.getCustomerName())) names.add(o.getCustomerName());
        }
        return names.size();
    }
}
