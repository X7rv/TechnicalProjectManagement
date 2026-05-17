package data;

import model.Order;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonStorage.java
 * قارئ/كاتب JSON بسيط - بدون مكتبات خارجية.
 * يحفظ الملف في: data/orders.json (داخل مجلد المشروع)
 *
 * شكل الملف:
 * [
 *   {"id":1,"customer":"احمد","email":"a@x.com","product":"iPhone",
 *    "type":"إلكترونيات","qty":1,"price":4200.0,"status":"مدفوع","date":"2026-05-14"}
 * ]
 */
public class JsonStorage {

    private static final String DIR  = "data";
    private static final String FILE = DIR + "/orders.json";

    private JsonStorage() {}

    // ── الحفظ ─────────────────────────────────────────────────────────────────

    public static void save(List<Order> orders) throws IOException {
        // ننشئ المجلد إذا ما كان موجود
        new File(DIR).mkdirs();

        // نبني نص JSON
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < orders.size(); i++) {
            sb.append("  ").append(toJson(orders.get(i)));
            if (i < orders.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");

        // نكتب الملف بترميز UTF-8 ليدعم العربي
        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        }
    }

    private static String toJson(Order o) {
        return "{"
            + "\"id\":"      + o.getDbId()                    + ","
            + "\"customer\":"+ esc(o.getCustomerName())       + ","
            + "\"email\":"   + esc(o.getCustomerEmail())      + ","
            + "\"product\":" + esc(o.getProductName())        + ","
            + "\"type\":"    + esc(o.getProductType())        + ","
            + "\"qty\":"     + o.getQuantity()                + ","
            + "\"price\":"   + o.getUnitPrice()               + ","
            + "\"status\":"  + esc(o.getStatus())             + ","
            + "\"date\":"    + esc(o.getDate())               + ","
            + "\"notes\":"   + esc(o.getNotes())              + ","
            + "\"priority\":"+ esc(o.getPriority())           + ","
            + "\"discount\":"+ o.getDiscount()                + ","
            + "\"payment\":" + esc(o.getPaymentMethod())
            + "}";
    }

    /** يلف النص بعلامتي اقتباس ويهرب الأحرف الخاصة. */
    private static String esc(String s) {
        if (s == null) return "\"\"";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default   -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        out.append("\"");
        return out.toString();
    }

    // ── القراءة ───────────────────────────────────────────────────────────────

    public static List<Order> load() throws IOException {
        File f = new File(FILE);
        if (!f.exists()) return new ArrayList<>();

        String text;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
            text = sb.toString();
        }
        return parseArray(text);
    }

    /** يحلل المصفوفة الرئيسية ويرجع قائمة الطلبات. */
    private static List<Order> parseArray(String s) {
        List<Order> out = new ArrayList<>();
        Parser p = new Parser(s);
        p.skipWs();
        if (!p.match('[')) return out;
        p.skipWs();
        if (p.match(']')) return out;
        while (true) {
            p.skipWs();
            Order o = parseObject(p);
            if (o != null) out.add(o);
            p.skipWs();
            if (p.match(',')) continue;
            if (p.match(']')) break;
            break;
        }
        return out;
    }

    private static Order parseObject(Parser p) {
        p.skipWs();
        if (!p.match('{')) return null;

        long   id       = -1;
        String cust     = "";
        String email    = "";
        String prod     = "";
        String type     = "";
        int    qty      = 0;
        double price    = 0;
        String status   = "";
        String date     = "";
        String notes    = "";
        String priority = "normal";
        double discount = 0;
        String payment  = "cash";

        while (true) {
            p.skipWs();
            if (p.match('}')) break;
            String key = p.readString();
            p.skipWs();
            p.match(':');
            p.skipWs();
            switch (key) {
                case "id"       -> id       = (long) p.readNumber();
                case "customer" -> cust     = p.readString();
                case "email"    -> email    = p.readString();
                case "product"  -> prod     = p.readString();
                case "type"     -> type     = p.readString();
                case "qty"      -> qty      = (int)  p.readNumber();
                case "price"    -> price    = p.readNumber();
                case "status"   -> status   = p.readString();
                case "date"     -> date     = p.readString();
                case "notes"    -> notes    = p.readString();
                case "priority" -> priority = p.readString();
                case "discount" -> discount = p.readNumber();
                case "payment"  -> payment  = p.readString();
                default         -> p.skipValue();
            }
            p.skipWs();
            if (p.match(',')) continue;
            if (p.match('}')) break;
        }
        Order o = new Order(id, cust, email, prod, type, qty, price, status, date);
        o.setNotes(notes);
        o.setPriority(priority);
        o.setDiscount(discount);
        o.setPaymentMethod(payment);
        return o;
    }

    // ── محلل صغير داخلي ──────────────────────────────────────────────────────

    private static class Parser {
        final String src;
        int i;
        Parser(String s) { this.src = s; this.i = 0; }

        void skipWs() {
            while (i < src.length() && Character.isWhitespace(src.charAt(i))) i++;
        }
        boolean match(char c) {
            if (i < src.length() && src.charAt(i) == c) { i++; return true; }
            return false;
        }
        String readString() {
            skipWs();
            if (!match('"')) return "";
            StringBuilder sb = new StringBuilder();
            while (i < src.length()) {
                char c = src.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\' && i < src.length()) {
                    char esc = src.charAt(i++);
                    switch (esc) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        case 'u'  -> {
                            if (i + 4 <= src.length()) {
                                sb.append((char) Integer.parseInt(src.substring(i, i+4), 16));
                                i += 4;
                            }
                        }
                        default   -> sb.append(esc);
                    }
                } else sb.append(c);
            }
            return sb.toString();
        }
        double readNumber() {
            skipWs();
            int start = i;
            while (i < src.length()) {
                char c = src.charAt(i);
                if (Character.isDigit(c) || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E') i++;
                else break;
            }
            try { return Double.parseDouble(src.substring(start, i)); }
            catch (NumberFormatException e) { return 0; }
        }
        void skipValue() {
            skipWs();
            if (i >= src.length()) return;
            char c = src.charAt(i);
            if (c == '"') readString();
            else if (c == '{' || c == '[') {
                int depth = 1; i++;
                while (i < src.length() && depth > 0) {
                    char ch = src.charAt(i++);
                    if (ch == '{' || ch == '[') depth++;
                    else if (ch == '}' || ch == ']') depth--;
                    else if (ch == '"') { i--; readString(); }
                }
            } else readNumber();
        }
    }
}
