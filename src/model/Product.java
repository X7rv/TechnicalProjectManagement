package model;

import util.Money;

/**
 * Product.java
 * نموذج المنتج في المخزون:
 *   • رقم تلقائي
 *   • اسم المنتج
 *   • النوع (إلكترونيات، حاسوب، ...)
 *   • السعر الافتراضي
 *   • الكمية المتبقية في المخزون
 *   • حد التنبيه (تنبيه عند نزول المخزون عنه)
 */
public class Product {

    private long   id = -1;
    private String name;
    private String type;
    private double defaultPrice;
    private int    stock;
    private int    lowStockThreshold = 5;

    /** منشئ لإنشاء منتج جديد. */
    public Product(String name, String type, double defaultPrice, int stock) {
        this.name         = name;
        this.type         = type;
        this.defaultPrice = defaultPrice;
        this.stock        = stock;
    }

    /** منشئ كامل للتحميل من JSON. */
    public Product(long id, String name, String type, double defaultPrice,
                   int stock, int lowStockThreshold) {
        this.id                = id;
        this.name              = name;
        this.type              = type;
        this.defaultPrice      = defaultPrice;
        this.stock             = stock;
        this.lowStockThreshold = lowStockThreshold;
    }

    public long   getDbId()              { return id; }
    public void   setDbId(long id)       { this.id = id; }

    public String getId() {
        return id < 0 ? "—" : String.format("#PRD-%03d", id);
    }

    public String getName()              { return name; }
    public String getType()              { return type; }
    public double getDefaultPrice()      { return defaultPrice; }
    public int    getStock()             { return stock; }
    public int    getLowStockThreshold() { return lowStockThreshold; }

    public void setName(String name)            { this.name = name; }
    public void setType(String type)            { this.type = type; }
    public void setDefaultPrice(double price)   { this.defaultPrice = price; }
    public void setStock(int stock)             { this.stock = stock; }
    public void setLowStockThreshold(int t)     { this.lowStockThreshold = t; }

    /** هل المخزون منخفض ويحتاج تنبيه؟ */
    public boolean isLowStock() {
        return stock <= lowStockThreshold && stock > 0;
    }

    /** هل المنتج نافد (مخزون = 0)؟ */
    public boolean isOutOfStock() {
        return stock <= 0;
    }

    /** يُنقص المخزون بالكمية المحددة. يرجع false لو المخزون لا يكفي. */
    public boolean reduceStock(int qty) {
        if (stock < qty) return false;
        stock -= qty;
        return true;
    }

    /** يزيد المخزون (مثلاً عند إلغاء طلب). */
    public void increaseStock(int qty) {
        stock += qty;
    }

    public String getPriceFormatted() {
        return Money.format(defaultPrice);
    }

    public Product copy() {
        return new Product(id, name, type, defaultPrice, stock, lowStockThreshold);
    }
}
