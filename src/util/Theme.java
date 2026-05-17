package util;

import java.awt.*;

/**
 * Theme.java
 * الألوان والخطوط - متغيرات (وليس constants) لأنها تتبدّل حسب الإعدادات.
 * استدعي Theme.apply() بعد أي تغيير في Settings لتحديث القيم.
 */
public class Theme {

    // ── ألوان الخلفيات ────────────────────────────────────────────────────────
    public static Color BG_DEEP;
    public static Color BG_PANEL;
    public static Color BG_CARD;
    public static Color BG_INPUT;
    public static Color BG_HOVER;

    // ── ألوان التمييز ────────────────────────────────────────────────────────
    public static Color ACCENT;
    public static Color ACCENT_DIM;
    public static Color GREEN     = new Color( 34, 197,  94);
    public static Color GREEN_DIM = new Color( 34, 197,  94,  30);
    public static Color RED       = new Color(239,  68,  68);
    public static Color BLUE      = new Color( 99, 179, 237);

    // ── ألوان النص ────────────────────────────────────────────────────────────
    public static Color TEXT_PRIMARY;
    public static Color TEXT_MUTED;
    public static Color BORDER;
    public static Color BORDER_ACCENT;

    // ── الخطوط (يعتمد حجمها على الإعدادات) ──────────────────────────────────
    public static Font FONT_TITLE;
    public static Font FONT_HEAD;
    public static Font FONT_BODY;
    public static Font FONT_SMALL;

    // ── الأبعاد ───────────────────────────────────────────────────────────────
    public static final int SIDEBAR_W = 240;
    public static int ROW_H = 44;
    public static final int RADIUS    = 12;
    public static final int PAD       = 24;

    // ── لوحة ألوان للرسومات (donut + bar) ───────────────────────────────────
    public static final Color[] CHART_PALETTE = {
        new Color(245, 166,  35),
        new Color( 99, 179, 237),
        new Color( 34, 197,  94),
        new Color(168,  85, 247),
        new Color(239,  68,  68),
        new Color(250, 204,  21),
        new Color(236,  72, 153),
        new Color( 20, 184, 166)
    };

    // ── ألوان الرتب ──────────────────────────────────────────────────────────
    public static final Color RANK_PLATINUM = new Color(160, 220, 255);
    public static final Color RANK_GOLD     = new Color(245, 166,  35);
    public static final Color RANK_SILVER   = new Color(180, 190, 210);
    public static final Color RANK_BRONZE   = new Color(180, 130,  90);

    static { apply(); }

    /** يطبّق الإعدادات الحالية على الثيم. */
    public static void apply() {
        applyTheme();
        applyAccent();
        applyFont();
    }

    private static void applyTheme() {
        if ("light".equals(Settings.theme)) {
            BG_DEEP      = new Color(245, 247, 251);
            BG_PANEL     = new Color(255, 255, 255);
            BG_CARD      = new Color(255, 255, 255);
            BG_INPUT     = new Color(238, 242, 248);
            BG_HOVER     = new Color(228, 234, 244);
            TEXT_PRIMARY = new Color( 25,  30,  45);
            TEXT_MUTED   = new Color(100, 115, 140);
            BORDER       = new Color(  0,   0,   0,  25);
        } else {
            // dark - ألوان أعمق وأنيقة (slate-blue ، أكثر "premium")
            BG_DEEP      = new Color( 12,  15,  24);
            BG_PANEL     = new Color( 18,  23,  37);
            BG_CARD      = new Color( 25,  31,  48);
            BG_INPUT     = new Color( 32,  39,  58);
            BG_HOVER     = new Color( 42,  50,  72);
            TEXT_PRIMARY = new Color(242, 244, 250);
            TEXT_MUTED   = new Color(130, 142, 170);
            BORDER       = new Color(255, 255, 255,  16);
        }
    }

    private static void applyAccent() {
        switch (Settings.accentColor) {
            case "blue"   -> ACCENT = new Color( 99, 179, 237);
            case "green"  -> ACCENT = new Color( 34, 197,  94);
            case "purple" -> ACCENT = new Color(168,  85, 247);
            default       -> ACCENT = new Color(245, 166,  35);  // amber
        }
        ACCENT_DIM = new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40);
        BORDER_ACCENT = new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80);
    }

    private static void applyFont() {
        int titleSize, headSize, bodySize, smallSize;
        switch (Settings.fontSize) {
            case "small" -> { titleSize = 22; headSize = 14; bodySize = 12; smallSize = 10; ROW_H = 38; }
            case "large" -> { titleSize = 30; headSize = 18; bodySize = 16; smallSize = 14; ROW_H = 50; }
            default      -> { titleSize = 26; headSize = 16; bodySize = 14; smallSize = 12; ROW_H = 44; }
        }
        FONT_TITLE = new Font("SansSerif", Font.BOLD,  titleSize);
        FONT_HEAD  = new Font("SansSerif", Font.BOLD,  headSize);
        FONT_BODY  = new Font("SansSerif", Font.PLAIN, bodySize);
        FONT_SMALL = new Font("SansSerif", Font.PLAIN, smallSize);
    }

    private Theme() {}
}
