package data;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Backup.java
 * يأخذ نسخة احتياطية يومية من orders.json تلقائياً.
 * يحتفظ بآخر 10 نسخ ثم يحذف الأقدم.
 *
 * المسار: data/backups/orders_YYYY-MM-DD.json
 */
public class Backup {

    private static final String SOURCE     = "data/orders.json";
    private static final String BACKUP_DIR = "data/backups";
    private static final int    MAX_BACKUPS = 10;

    private Backup() {}

    /** يأخذ نسخة احتياطية إذا اليوم ما أُخذت نسخة بعد. */
    public static void runDailyBackup() {
        File source = new File(SOURCE);
        if (!source.exists()) return;

        new File(BACKUP_DIR).mkdirs();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        File target = new File(BACKUP_DIR + "/orders_" + today + ".json");

        // إذا نسخة اليوم موجودة، نستبدلها فقط (لتعكس آخر تعديل)
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            cleanOldBackups();
        } catch (IOException e) {
            System.err.println("فشل النسخ الاحتياطي: " + e.getMessage());
        }
    }

    /** يحذف النسخ القديمة ويبقي على آخر MAX_BACKUPS فقط. */
    private static void cleanOldBackups() {
        File dir = new File(BACKUP_DIR);
        File[] files = dir.listFiles((f, name) -> name.startsWith("orders_") && name.endsWith(".json"));
        if (files == null || files.length <= MAX_BACKUPS) return;

        List<File> sorted = new ArrayList<>(List.of(files));
        sorted.sort(Comparator.comparing(File::getName).reversed());

        for (int i = MAX_BACKUPS; i < sorted.size(); i++) {
            sorted.get(i).delete();
        }
    }

    public static List<String> listBackups() {
        File dir = new File(BACKUP_DIR);
        File[] files = dir.listFiles((f, name) -> name.startsWith("orders_") && name.endsWith(".json"));
        List<String> names = new ArrayList<>();
        if (files == null) return names;
        for (File f : files) names.add(f.getName());
        names.sort(Comparator.reverseOrder());
        return names;
    }
}
