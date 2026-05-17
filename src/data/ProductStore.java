package data;

import model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProductStore.java
 * مستودع المنتجات. يحفظ تلقائياً + يُعلم المستمعين عند أي تغيير.
 */
public class ProductStore {

    private final List<Product>  products  = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final AtomicLong     idSeq     = new AtomicLong(1);
    private String lastError = null;

    public ProductStore() {
        loadFromDisk();
    }

    private void loadFromDisk() {
        try {
            List<Product> loaded = ProductStorage.load();
            products.clear();
            products.addAll(loaded);
            long max = 0;
            for (Product p : products) if (p.getDbId() > max) max = p.getDbId();
            idSeq.set(max + 1);
        } catch (IOException e) {
            lastError = e.getMessage();
            System.err.println("فشل تحميل المنتجات: " + e.getMessage());
        }
    }

    private boolean saveToDisk() {
        try {
            ProductStorage.save(products);
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

    public boolean add(Product p) {
        p.setDbId(idSeq.getAndIncrement());
        products.add(0, p);
        if (!saveToDisk()) {
            products.remove(0);
            return false;
        }
        notifyChange();
        return true;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= products.size()) return false;
        Product removed = products.remove(index);
        if (!saveToDisk()) {
            products.add(index, removed);
            return false;
        }
        notifyChange();
        return true;
    }

    public boolean update(Product p) {
        if (!saveToDisk()) return false;
        notifyChange();
        return true;
    }

    public Product get(int index)  { return products.get(index); }
    public List<Product> getAll()  { return Collections.unmodifiableList(products); }
    public int size()              { return products.size(); }

    // ── البحث ─────────────────────────────────────────────────────────────────

    /** يبحث عن منتج بالاسم (تجاهل حالة الحروف). يرجع null إن لم يوجد. */
    public Product findByName(String name) {
        if (name == null) return null;
        String needle = name.trim().toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().equals(needle)) return p;
        }
        return null;
    }

    // ── الإحصائيات ────────────────────────────────────────────────────────────

    public long countLowStock() {
        long c = 0;
        for (Product p : products) if (p.isLowStock()) c++;
        return c;
    }

    public long countOutOfStock() {
        long c = 0;
        for (Product p : products) if (p.isOutOfStock()) c++;
        return c;
    }

    public int totalUnitsInStock() {
        int sum = 0;
        for (Product p : products) sum += p.getStock();
        return sum;
    }
}
