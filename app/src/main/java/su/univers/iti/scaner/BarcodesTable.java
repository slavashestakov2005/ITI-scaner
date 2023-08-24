package su.univers.iti.scaner;

import android.util.Log;

import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;

class BarcodesTable {
    public long code;
    public int[] points;    // x, y, w, h
    public ArrayList<Long> barcodes;


    public BarcodesTable(long code, int[] points) {
        Log.e("creating", "" + code + " : " + points[0] + ", " + points[1] + ", " + points[2] + ", " + points[3]);
        this.code = code;
        this.points = points;
        this.barcodes = new ArrayList<>();
    }

    static ArrayList<Long> getEAN13(SymbolSet data) {
        ArrayList<Long> ean13 = new ArrayList<>();
        for (Symbol sym : data){
            String code = sym.getData();
            if (sym.getType() == Symbol.EAN13 && code != null) {
                ean13.add(Long.parseLong(code));}
        }
        if (ean13.size() == 0) ean13.add((long) 0);
        return ean13;
    }

    static ArrayList<BarcodesTable> parseData(SymbolSet data) {
        ArrayList<BarcodesTable> ean8 = new ArrayList<>(), ean13 = new ArrayList<>();
        for (Symbol sym : data) {
            int tp = sym.getType();
            int[] points = sym.getBounds();
            String code = sym.getData();
            if (code != null) {
                long val = Long.parseLong(code);
                if (tp == Symbol.EAN8) ean8.add(new BarcodesTable(val / 10, points));
                if (tp == Symbol.EAN13) ean13.add(new BarcodesTable(val, points));
            }
        }

        if (ean8.size() == 0) ean8.add(new BarcodesTable(0, new int[]{0, 0, 0, 0}));

        for (BarcodesTable code : ean13) {
            if (ean8.size() > 1) {
                int pos = code.points[0] + code.points[2] / 2;
                for (BarcodesTable cur_ean : ean8){
                    if (cur_ean.points[0] <= pos && pos <= cur_ean.points[0] + cur_ean.points[2]){
                        cur_ean.barcodes.add(code.code);
                        break;
                    }
                }

            } else {
                for (BarcodesTable cur_ean : ean8) cur_ean.barcodes.add(code.code);
            }
        }
        return ean8;
    }
}
