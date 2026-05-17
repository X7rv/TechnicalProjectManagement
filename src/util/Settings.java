package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings.java
 * إعدادات البرنامج - تُحفظ في data/settings.json
 * متغيرات public static علشان أي ملف يقدر يقرأها مباشرة.
 */
public class Settings {

    private static final String FILE = "data/settings.json";

    // ── الإعدادات المتاحة ─────────────────────────────────────────────────────

    /** اللغة: "ar" أو "en" */
    public static String language = "ar";

    /** اللون الرئيسي: "amber" / "blue" / "green" / "purple" */
    public static String accentColor = "amber";

    /** الثيم: "dark" أو "light" */
    public static String theme = "dark";

    /** حجم النص: "small" / "normal" / "large" */
    public static String fontSize = "normal";

    /** اسم المالك (يظهر في الترحيب) */
    public static String ownerName = "";

    /** ترتيب الطلبات: "newest" / "oldest" / "price_high" / "price_low" / "status" */
    public static String orderSort = "newest";

    /** نسبة ضريبة القيمة المضافة (%). 0 = لا توجد ضريبة */
    public static double vatRate = 15.0;

    /** هل ضريبة VAT مفعّلة؟ */
    public static boolean vatEnabled = false;

    /** أنواع المنتجات (قابلة للتعديل من المستخدم) */
    public static List<String> productTypes = new ArrayList<>(List.of(
        "إلكترونيات", "حاسوب", "أجهزة لوحية",
        "صوتيات", "إكسسوار", "ملابس", "أثاث", "أخرى"
    ));

    private Settings() {}

    // ── الحفظ ─────────────────────────────────────────────────────────────────

    public static void save() {
        new File("data").mkdirs();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"language\":").append(esc(language)).append(",\n");
        sb.append("  \"accentColor\":").append(esc(accentColor)).append(",\n");
        sb.append("  \"theme\":").append(esc(theme)).append(",\n");
        sb.append("  \"fontSize\":").append(esc(fontSize)).append(",\n");
        sb.append("  \"ownerName\":").append(esc(ownerName)).append(",\n");
        sb.append("  \"orderSort\":").append(esc(orderSort)).append(",\n");
        sb.append("  \"vatRate\":").append(vatRate).append(",\n");
        sb.append("  \"vatEnabled\":").append(vatEnabled).append(",\n");
        sb.append("  \"productTypes\":[");
        for (int i = 0; i < productTypes.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(esc(productTypes.get(i)));
        }
        sb.append("]\n");
        sb.append("}\n");

        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        } catch (IOException e) {
            System.err.println("فشل حفظ الإعدادات: " + e.getMessage());
        }
    }

    // ── القراءة ───────────────────────────────────────────────────────────────

    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
            parse(sb.toString());
        } catch (IOException e) {
            System.err.println("فشل تحميل الإعدادات: " + e.getMessage());
        }
    }

    private static void parse(String text) {
        // قراءة بسيطة - نبحث عن المفاتيح المعروفة
        language    = readStr(text, "language",    language);
        accentColor = readStr(text, "accentColor", accentColor);
        theme       = readStr(text, "theme",       theme);
        fontSize    = readStr(text, "fontSize",    fontSize);
        ownerName   = readStr(text, "ownerName",   ownerName);
        orderSort   = readStr(text, "orderSort",   orderSort);
        vatRate     = readNum(text, "vatRate",     vatRate);
        vatEnabled  = readBool(text, "vatEnabled", vatEnabled);

        // قراءة قائمة productTypes
        int idx = text.indexOf("\"productTypes\"");
        if (idx >= 0) {
            int open  = text.indexOf('[', idx);
            int close = text.indexOf(']', open);
            if (open > 0 && close > open) {
                List<String> types = new ArrayList<>();
                String inside = text.substring(open + 1, close);
                int pos = 0;
                while (pos < inside.length()) {
                    int q1 = inside.indexOf('"', pos);
                    if (q1 < 0) break;
                    int q2 = inside.indexOf('"', q1 + 1);
                    if (q2 < 0) break;
                    types.add(inside.substring(q1 + 1, q2));
                    pos = q2 + 1;
                }
                if (!types.isEmpty()) productTypes = types;
            }
        }
    }

    private static String readStr(String text, String key, String def) {
        String pat = "\"" + key + "\":";
        int i = text.indexOf(pat);
        if (i < 0) return def;
        int q1 = text.indexOf('"', i + pat.length());
        if (q1 < 0) return def;
        int q2 = text.indexOf('"', q1 + 1);
        if (q2 < 0) return def;
        return text.substring(q1 + 1, q2);
    }

    private static double readNum(String text, String key, double def) {
        String pat = "\"" + key + "\":";
        int i = text.indexOf(pat);
        if (i < 0) return def;
        i += pat.length();
        StringBuilder sb = new StringBuilder();
        while (i < text.length()) {
            char c = text.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-') { sb.append(c); i++; }
            else break;
        }
        try { return Double.parseDouble(sb.toString()); }
        catch (Exception e) { return def; }
    }

    private static boolean readBool(String text, String key, boolean def) {
        String pat = "\"" + key + "\":";
        int i = text.indexOf(pat);
        if (i < 0) return def;
        i += pat.length();
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
        if (text.startsWith("true", i))  return true;
        if (text.startsWith("false", i)) return false;
        return def;
    }

    private static String esc(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
