package data;

import model.Product;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductStorage.java
 * قراءة/كتابة المنتجات في data/products.json
 * نفس فكرة JsonStorage لكن للمنتجات.
 */
public class ProductStorage {

    private static final String DIR  = "data";
    private static final String FILE = DIR + "/products.json";

    private ProductStorage() {}

    // ── الحفظ ─────────────────────────────────────────────────────────────────

    public static void save(List<Product> products) throws IOException {
        new File(DIR).mkdirs();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < products.size(); i++) {
            sb.append("  ").append(toJson(products.get(i)));
            if (i < products.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        }
    }

    private static String toJson(Product p) {
        return "{"
            + "\"id\":"        + p.getDbId()              + ","
            + "\"name\":"      + esc(p.getName())         + ","
            + "\"type\":"      + esc(p.getType())         + ","
            + "\"price\":"     + p.getDefaultPrice()      + ","
            + "\"stock\":"     + p.getStock()             + ","
            + "\"threshold\":" + p.getLowStockThreshold()
            + "}";
    }

    private static String esc(String s) {
        if (s == null) return "\"\"";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
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

    public static List<Product> load() throws IOException {
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

    private static List<Product> parseArray(String s) {
        List<Product> out = new ArrayList<>();
        Parser p = new Parser(s);
        p.skipWs();
        if (!p.match('[')) return out;
        p.skipWs();
        if (p.match(']')) return out;
        while (true) {
            p.skipWs();
            Product prod = parseObject(p);
            if (prod != null) out.add(prod);
            p.skipWs();
            if (p.match(',')) continue;
            if (p.match(']')) break;
            break;
        }
        return out;
    }

    private static Product parseObject(Parser p) {
        p.skipWs();
        if (!p.match('{')) return null;

        long   id        = -1;
        String name      = "";
        String type      = "";
        double price     = 0;
        int    stock     = 0;
        int    threshold = 5;

        while (true) {
            p.skipWs();
            if (p.match('}')) break;
            String key = p.readString();
            p.skipWs();
            p.match(':');
            p.skipWs();
            switch (key) {
                case "id"        -> id        = (long) p.readNumber();
                case "name"      -> name      = p.readString();
                case "type"      -> type      = p.readString();
                case "price"     -> price     = p.readNumber();
                case "stock"     -> stock     = (int) p.readNumber();
                case "threshold" -> threshold = (int) p.readNumber();
                default          -> p.skipValue();
            }
            p.skipWs();
            if (p.match(',')) continue;
            if (p.match('}')) break;
        }
        return new Product(id, name, type, price, stock, threshold);
    }

    // ── المحلل الداخلي ────────────────────────────────────────────────────────

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
                    char e = src.charAt(i++);
                    switch (e) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n'  -> sb.append('\n');
                        case 't'  -> sb.append('\t');
                        case 'u'  -> {
                            if (i + 4 <= src.length()) {
                                sb.append((char) Integer.parseInt(src.substring(i, i+4), 16));
                                i += 4;
                            }
                        }
                        default   -> sb.append(e);
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
