package su.univers.iti.scaner.utils;

import android.util.Log;

import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarcodesTable {
    static private int updates_count;
    static private HashMap<Long, Long> ean8;
    static private HashMap<Long, Long> ean13;

    public static void clear() {
        updates_count = 0;
        ean8 = new HashMap<>();
        ean13 = new HashMap<>();
    }
    public static void parse(SymbolSet data) {
        ++updates_count;
        ArrayList<Long> cur_ean8 = new ArrayList<>(), cur_ean13 = new ArrayList<>();
        for (Symbol sym : data) {
            int tp = sym.getType();
            String code = sym.getData();
            if (code != null) {
                long val = Long.parseLong(code);
                if (tp == Symbol.EAN8) cur_ean8.add(val / 10);
                if (tp == Symbol.EAN13) cur_ean13.add(val);
            }
        }
        for (Long code : cur_ean8) {
            ean8.put(code, 1 + ean8.getOrDefault(code, 0L));
        }
        for (Long code : cur_ean13) {
            ean13.put(code, 1 + ean13.getOrDefault(code, 0L));
        }
    }

    public static long getEAN8() {
        if (ean8.isEmpty()) return 0;
        long max_count = 0;
        for (Long value : ean8.values()) {
            max_count = Math.max(max_count, value);
        }
        for (Map.Entry<Long, Long> cur : ean8.entrySet()) {
            if (cur.getValue() == max_count) return cur.getKey();
        }
        return 0;
    }

    public static ArrayList<Long> getEAN13() {
        ArrayList<Long> codes = new ArrayList<>();
        for (Map.Entry<Long, Long> cur : ean13.entrySet()) {
            Long code = cur.getKey(), count = cur.getValue();
            if (count > updates_count / 3) codes.add(code);
        }
        return codes;
    }

    public static long getOneEAN13() {
        if (ean13.isEmpty()) return 0;
        long max_count = 0;
        for (Long value : ean13.values()) {
            max_count = Math.max(max_count, value);
        }
        for (Map.Entry<Long, Long> cur : ean13.entrySet()) {
            if (cur.getValue() == max_count) return cur.getKey();
        }
        return 0;
    }

    public static int getUpdatesCount() {
        return updates_count;
    }
}
