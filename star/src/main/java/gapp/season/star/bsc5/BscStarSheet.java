package gapp.season.star.bsc5;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import gapp.season.star.util.StarLog;

//耶鲁明星目录: http://tdc-www.harvard.edu/catalogs/bsc5.html
public class BscStarSheet {
    private static final String TAG = "BscStarSheet";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static List<Star> convertBSCStars(Context context) {
        try {
            AssetManager am = context.getResources().getAssets();
            InputStream is = am.open("bsc5.dat");
            List<Star> list = fromData(is);
            File f = new File(context.getExternalFilesDir(null), "bsc5.txt");
            f.getParentFile().mkdirs();
            f.createNewFile();
            writeData(list, f);
            StarLog.i(TAG, "convert BSC Stars success");
            return list;
        } catch (Exception e) {
            StarLog.i(TAG, "convert BSC Stars failed");
            e.printStackTrace();
        }
        return null;
    }

    public static List<Star> fromData(InputStream is) throws IOException {
        List<Star> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));//StandardCharsets.ISO_8859_1
        String line;
        while ((line = reader.readLine()) != null) {
            Star star = Star.readLine(line);
            list.add(star);
        }
        reader.close();
        return list;
    }

    public static void writeData(List<Star> list, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (Star star : list) {
            String fLine = star.toFormatLine();
            bw.write(fLine);
            bw.newLine();
        }
        bw.close();
    }
}
