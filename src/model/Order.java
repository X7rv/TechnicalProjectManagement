package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import util.Money;
import util.OrderStatus;
import util.Settings;

/**
 * Order.java
 * نموذج الطلب — يحوي كل بيانات الطلب الواحد.
 *
 * الحقول الأساسية:  رقم، عميل، إيميل، منتج، نوع، كمية، سعر، حالة، تاريخ
 * الحقول الجديدة:   ملاحظات، أولوية، خصم، طريقة دفع
 */
public class Order {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // أساسي
    private long   id = -1;
    private String customerName;
    private String customerEmail;
    private String productName;
    private String productType;
    private int    quantity;
    private double unitPrice;
    private String status;
    private String date;

    // جديد
    private String notes        = "";       // ملاحظات حرة
    private String priority     = "normal"; // urgent / normal / low
    private double discount     = 0;        // نسبة الخصم (0-100)
    private String paymentMethod= "cash";   // cash / transfer / card

    /** منشئ لإنشاء طلب جديد. */
    public Order(String customerName, String customerEmail, String productName,
                 String productType, int quantity, double unitPrice, String status) {
        this.customerName  = customerName;
        this.customerEmail = customerEmail;
        this.productName   = productName;
        this.productType   = productType;
        this.quantity      = quantity;
        this.unitPrice     = unitPrice;
        this.status        = status;
        this.date          = LocalDate.now().format(FMT);
    }

    /** منشئ كامل للتحميل من JSON. */
    public Order(long id, String customerName, String customerEmail, String productName,
                 String productType, int quantity, double unitPrice, String status, String date) {
        this.id            = id;
        this.customerName  = customerName;
        this.customerEmail = customerEmail;
        this.productName   = productName;
        this.productType   = productType;
        this.quantity      = quantity;
        this.unitPrice     = unitPrice;
        this.status        = status;
        this.date          = date;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public long   getDbId()          { return id; }
    public void   setDbId(long id)   { this.id = id; }

    public String getId() {
        return id < 0 ? "—" : String.format("#ORD-%03d", id);
    }

    public String getCustomerName()  { return customerName; }
    public String getCustomerEmail() { return customerEmail == null ? "" : customerEmail; }
    public String getProductName()   { return productName; }
    public String getProductType()   { return productType; }
    public int    getQuantity()      { return quantity; }
    public double getUnitPrice()     { return unitPrice; }
    public String getStatus()        { return status; }
    public String getDate()          { return date; }

    public String getNotes()         { return notes == null ? "" : notes; }
    public String getPriority()      { return priority == null ? "normal" : priority; }
    public double getDiscount()      { return discount; }
    public String getPaymentMethod() { return paymentMethod == null ? "cash" : paymentMethod; }

    public void setCustomerName(String name)    { this.customerName  = name; }
    public void setCustomerEmail(String email)  { this.customerEmail = email; }
    public void setProductName(String product)  { this.productName   = product; }
    public void setProductType(String type)     { this.productType   = type; }
    public void setQuantity(int qty)            { this.quantity      = qty; }
    public void setUnitPrice(double price)      { this.unitPrice     = price; }
    public void setStatus(String status)        { this.status        = status; }
    public void setNotes(String notes)          { this.notes         = notes; }
    public void setPriority(String priority)    { this.priority      = priority; }
    public void setDiscount(double discount)    { this.discount      = discount; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }
    public void setDate(String date)            { this.date          = date; }

    // ── الحسابات ─────────────────────────────────────────────────────────────

    /** المجموع الفرعي قبل أي خصم أو ضريبة. */
    public double getSubtotal() { return quantity * unitPrice; }

    /** قيمة الخصم بالريال. */
    public double getDiscountAmount() {
        return getSubtotal() * (discount / 100.0);
    }

    /** المجموع بعد الخصم وقبل الضريبة. */
    public double getAfterDiscount() {
        return getSubtotal() - getDiscountAmount();
    }

    /** قيمة ضريبة VAT (0 إذا الضريبة معطلة). تُحسب بعد الخصم. */
    public double getVatAmount() {
        if (!Settings.vatEnabled) return 0;
        return getAfterDiscount() * (Settings.vatRate / 100.0);
    }

    /** المجموع النهائي = (الكمية × السعر) − الخصم + الضريبة. */
    public double getTotal() {
        return getAfterDiscount() + getVatAmount();
    }

    public boolean isPaid() {
        return OrderStatus.isPaidStatus(status);
    }

    public String getTotalFormatted() {
        return Money.format(getTotal());
    }

    /** نسخة طبق الأصل من الطلب - تُستخدم لـ Undo وللتكرار (Duplicate). */
    public Order copy() {
        Order c = new Order(id, customerName, customerEmail, productName,
            productType, quantity, unitPrice, status, date);
        c.notes         = this.notes;
        c.priority      = this.priority;
        c.discount      = this.discount;
        c.paymentMethod = this.paymentMethod;
        return c;
    }
}
