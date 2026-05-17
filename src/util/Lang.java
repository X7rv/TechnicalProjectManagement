package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Lang.java
 * النصوص بالعربي والإنجليزي. تُحدَّد اللغة من Settings.language
 */
public class Lang {

    private static final Map<String, String[]> STRINGS = new HashMap<>();

    static {
        // key → [Arabic, English]
        put("app_title",       "TPM — نظام إدارة الطلبات",   "TPM — Order Management");
        put("nav_overview",    "📊  نظرة عامة",              "📊  Overview");
        put("nav_orders",      "📦  الطلبات",                "📦  Orders");
        put("nav_customers",   "👥  العملاء",                "👥  Customers");
        put("nav_settings",    "⚙️  الإعدادات",              "⚙️  Settings");
        put("welcome",         "مرحباً بك في نظام TPM",      "Welcome to TPM");
        put("welcome_named",   "مرحباً %s 👋",               "Welcome, %s 👋");

        // KPIs
        put("total_sales",     "إجمالي المبيعات",            "Total Revenue");
        put("total_orders",    "عدد الطلبات",                "Orders");
        put("total_clients",   "عدد العملاء",                "Customers");
        put("total_paid",      "الطلبات المدفوعة",           "Paid Orders");

        // الأزرار
        put("add_order",       "＋  إضافة طلب",              "＋  Add Order");
        put("edit_order",      "✎  تعديل",                   "✎  Edit");
        put("btn_save",        "حفظ",                        "Save");
        put("btn_cancel",      "إلغاء",                      "Cancel");
        put("btn_delete",      "🗑  حذف",                    "🗑  Delete");
        put("btn_apply",       "تطبيق",                      "Apply");
        put("btn_reset",       "إعادة تعيين",                "Reset");
        put("btn_add",         "إضافة",                      "Add");
        put("btn_remove",      "حذف",                        "Remove");

        // أعمدة الجدول
        put("col_id",          "رقم الطلب",                  "Order ID");
        put("col_name",        "اسم العميل",                 "Customer");
        put("col_email",       "الإيميل",                    "Email");
        put("col_product",     "المنتج",                     "Product");
        put("col_type",        "النوع",                      "Type");
        put("col_qty",         "الكمية",                     "Qty");
        put("col_price",       "سعر الوحدة",                "Unit Price");
        put("col_total",       "الإجمالي",                   "Total");
        put("col_date",        "التاريخ",                    "Date");
        put("col_status",      "الحالة",                     "Status");
        put("col_spent",       "إجمالي المشتريات",          "Total Spent");
        put("col_rank",        "الرتبة",                     "Rank");
        put("col_orders_count","عدد الطلبات",                "Orders");

        // النوافذ
        put("dialog_add",      "إضافة طلب جديد",            "Add New Order");
        put("dialog_edit",     "تعديل الطلب",                "Edit Order");
        put("lbl_name",        "اسم العميل",                 "Customer Name");
        put("lbl_email",       "الإيميل",                    "Email");
        put("lbl_product",     "اسم المنتج",                 "Product Name");
        put("lbl_type",        "نوع المنتج",                 "Product Type");
        put("lbl_qty",         "الكمية",                     "Quantity");
        put("lbl_price",       "سعر الوحدة (ر.س)",          "Unit Price (SAR)");

        // الحالات (ثابتة بالعربي - تخزين موحّد)
        put("status_paid",     "مدفوع",                      "مدفوع");
        put("status_pending",  "معلق",                       "معلق");
        put("status_shipped",  "مشحون",                      "مشحون");
        put("status_cancelled","ملغي",                       "ملغي");

        // الرسائل
        put("err_empty",       "يرجى ملء اسم العميل والمنتج", "Please fill customer and product name");
        put("err_number",      "الكمية والسعر يجب أن تكون أرقاماً", "Qty and price must be numbers");
        put("err_select",      "اختر طلباً أولاً",            "Select an order first");
        put("confirm_delete",  "هل تريد حذف هذا الطلب؟",     "Delete this order?");

        // الإعدادات
        put("settings_title",    "⚙️  الإعدادات",            "⚙️  Settings");
        put("settings_general",  "عام",                       "General");
        put("settings_appearance","المظهر",                   "Appearance");
        put("settings_data",     "البيانات",                  "Data");
        put("settings_advanced", "متقدم",                     "Advanced");

        put("setting_owner",     "اسم المالك",                "Owner Name");
        put("setting_owner_hint","يظهر في صفحة الترحيب",     "Shown in welcome page");
        put("setting_language",  "اللغة",                     "Language");
        put("setting_theme",     "الثيم",                     "Theme");
        put("setting_accent",    "اللون الرئيسي",             "Accent Color");
        put("setting_font_size", "حجم النص",                  "Font Size");
        put("setting_sort",      "ترتيب الطلبات الافتراضي",   "Default Order Sort");
        put("setting_vat",       "ضريبة القيمة المضافة (VAT)","VAT Tax");
        put("setting_vat_enabled","تفعيل الضريبة",            "Enable VAT");
        put("setting_vat_rate",  "نسبة الضريبة (%)",          "VAT Rate (%)");
        put("setting_types",     "أنواع المنتجات",            "Product Types");
        put("setting_clear",     "مسح كل البيانات",           "Clear All Data");
        put("setting_clear_warn","تحذير: لا يمكن التراجع",    "Warning: cannot be undone");
        put("setting_clear_btn", "مسح جميع الطلبات",         "Delete All Orders");

        put("theme_dark",        "داكن",                      "Dark");
        put("theme_light",       "فاتح",                      "Light");
        put("color_amber",       "برتقالي",                   "Amber");
        put("color_blue",        "أزرق",                      "Blue");
        put("color_green",       "أخضر",                      "Green");
        put("color_purple",      "بنفسجي",                    "Purple");
        put("font_small",        "صغير",                      "Small");
        put("font_normal",       "عادي",                      "Normal");
        put("font_large",        "كبير",                      "Large");
        put("sort_newest",       "الأحدث أولاً",              "Newest First");
        put("sort_oldest",       "الأقدم أولاً",              "Oldest First");
        put("sort_price_high",   "الأعلى سعراً",              "Highest Price");
        put("sort_price_low",    "الأقل سعراً",               "Lowest Price");
        put("sort_status",       "حسب الحالة",                "By Status");
        put("lang_arabic",       "العربية",                   "العربية");
        put("lang_english",      "English",                   "English");

        put("settings_saved",    "تم حفظ الإعدادات ✓",       "Settings saved ✓");
        put("confirm_clear",     "هل أنت متأكد؟ سيتم حذف كل الطلبات نهائياً",
                                 "Are you sure? All orders will be deleted permanently");
        put("type_add_prompt",   "أدخل اسم النوع الجديد:",    "Enter new type name:");
        put("type_exists",       "هذا النوع موجود مسبقاً",    "This type already exists");

        // رسائل Toast
        put("toast_added",       "تمت إضافة الطلب",           "Order added");
        put("toast_updated",     "تم تحديث الطلب",            "Order updated");
        put("toast_deleted",     "تم حذف الطلب",              "Order deleted");
        put("toast_undone",      "تم التراجع",                "Action undone");
        put("toast_no_undo",     "لا توجد عملية للتراجع",     "Nothing to undo");
        put("toast_cleared",     "تم مسح جميع الطلبات",       "All orders deleted");

        // الحالة الفاضية
        put("empty_orders_title","لا توجد طلبات بعد",         "No orders yet");
        put("empty_orders_hint", "اضغط ＋ إضافة طلب للبدء",    "Press ＋ Add Order to start");
        put("empty_customers_title","لا يوجد عملاء بعد",      "No customers yet");
        put("empty_customers_hint", "العملاء يظهرون تلقائياً من الطلبات",
                                    "Customers appear automatically from orders");

        // البحث
        put("search_placeholder","ابحث في الطلبات...",         "Search orders...");
        put("filter_all",        "الكل",                       "All");

        // اختصارات
        put("shortcut_add",      "Ctrl+N للإضافة",            "Ctrl+N to add");
        put("shortcut_undo",     "Ctrl+Z للتراجع",            "Ctrl+Z to undo");
        put("btn_undo",          "↶  تراجع",                   "↶  Undo");
        put("btn_duplicate",     "⎘  تكرار",                   "⎘  Duplicate");

        // الحقول الجديدة
        put("lbl_notes",         "ملاحظات",                   "Notes");
        put("lbl_priority",      "الأولوية",                  "Priority");
        put("lbl_discount",      "الخصم (%)",                "Discount (%)");
        put("lbl_payment",       "طريقة الدفع",               "Payment Method");

        put("priority_urgent",   "عاجل",                      "Urgent");
        put("priority_normal",   "عادي",                      "Normal");
        put("priority_low",      "منخفض",                     "Low");

        put("payment_cash",      "نقدي",                      "Cash");
        put("payment_transfer",  "تحويل بنكي",                "Bank Transfer");
        put("payment_card",      "بطاقة",                     "Card");

        put("col_priority",      "الأولوية",                  "Priority");
        put("col_payment",       "الدفع",                     "Payment");

        // تكرار الطلب
        put("toast_duplicated",  "تم تكرار الطلب",            "Order duplicated");

        // محتوى المجموعات في النافذة
        put("section_main",      "المعلومات الأساسية",       "Main Info");
        put("section_extra",     "تفاصيل إضافية",            "Extra Details");

        // المنتجات
        put("nav_products",      "🛒  المنتجات",              "🛒  Products");
        put("col_stock",         "المخزون",                   "Stock");
        put("lbl_stock",         "الكمية في المخزون",         "Stock Quantity");
        put("lbl_low_threshold", "حد التنبيه",                "Low Stock Alert");

        put("add_product",       "＋  إضافة منتج",            "＋  Add Product");
        put("dialog_add_product","إضافة منتج جديد",          "Add New Product");
        put("dialog_edit_product","تعديل المنتج",             "Edit Product");
        put("confirm_delete_product","حذف هذا المنتج؟",      "Delete this product?");

        put("stock_ok",          "متوفر",                     "In Stock");
        put("stock_low",         "منخفض",                     "Low");
        put("stock_out",         "نافد",                      "Out");

        put("err_empty_product", "أدخل اسم المنتج",          "Enter product name");
        put("toast_product_added",   "تمت إضافة المنتج",     "Product added");
        put("toast_product_updated", "تم تحديث المنتج",      "Product updated");
        put("toast_product_deleted", "تم حذف المنتج",        "Product deleted");
        put("toast_stock_low",       "تنبيه: مخزون منخفض",   "Alert: Low stock");
        put("toast_stock_out",       "تحذير: نفد المخزون",   "Warning: Out of stock");

        put("empty_products_title", "لا توجد منتجات بعد",   "No products yet");
        put("empty_products_hint",  "اضغط ＋ إضافة منتج للبدء", "Press ＋ Add Product to start");

        // KPIs منتجات في نظرة عامة
        put("total_products",    "عدد المنتجات",              "Products");
        put("low_stock_count",   "منتجات منخفضة المخزون",    "Low Stock Items");

        // التحليلات
        put("nav_analytics",     "📈  التحليلات",            "📈  Analytics");
        put("avg_order",         "متوسط قيمة الطلب",         "Average Order Value");
        put("highest_order",     "أعلى طلب",                  "Highest Order");
        put("lowest_order",      "أقل طلب",                   "Lowest Order");
        put("units_sold",        "الكميات المباعة",          "Units Sold");
        put("top_customer",      "أفضل عميل",                 "Top Customer");
        put("top_product",       "أكثر منتج مبيعاً",         "Best Selling Product");
        put("chart_status",      "توزيع حالات الطلبات",      "Order Status Breakdown");
        put("chart_types",       "المبيعات حسب النوع",       "Sales by Type");
        put("chart_timeline",    "المبيعات اليومية (آخر 30 يوم)", "Daily Sales (Last 30 Days)");

        // التنبيهات
        put("alert_pending_fmt",   "لديك %d طلب معلق يحتاج متابعة",
                                   "You have %d pending order(s) to follow up");
        put("alert_low_stock_fmt", "%d منتج بمخزون منخفض",   "%d product(s) with low stock");
        put("alert_out_stock_fmt", "%d منتج نفد من المخزون", "%d product(s) out of stock");
        put("alert_all_good",      "كل شي تمام، لا توجد تنبيهات", "All good, no alerts");

        // طلب من المخزون
        put("add_stock_order",       "📦  من المخزون",          "📦  From Stock");
        put("add_custom_order",      "✏  طلب مخصص",            "✏  Custom Order");
        put("dialog_stock_order",    "طلب من المخزون",          "Stock Order");
        put("unit",                  "وحدة",                    "unit(s)");
        put("err_no_product_selected","اختر منتج أولاً",        "Select a product first");
        put("err_no_stock",          "لا توجد منتجات بمخزون متاح", "No products with stock available");
        put("err_insufficient_stock","المخزون لا يكفي - المتوفر: %d", "Insufficient stock - available: %d");
    }

    private static void put(String key, String ar, String en) {
        STRINGS.put(key, new String[]{ar, en});
    }

    /** يرجع true إذا اللغة عربية. */
    public static boolean isArabic() {
        return "ar".equals(Settings.language);
    }

    /** ترجمة المفتاح. */
    public static String t(String key) {
        String[] pair = STRINGS.get(key);
        if (pair == null) return key;
        return isArabic() ? pair[0] : pair[1];
    }
}
