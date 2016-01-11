package com.jebstern.lahinotto;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MarkerSetup {

    Context context;
    String path;

    public MarkerSetup(Context context, String path) {
        this.context = context;
        this.path = path;
    }


    public final List<MarkerBean> readCsv() {
        List<MarkerBean> markers = new ArrayList<>();
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open(path);

            BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, "ISO-8859-1"));
            try {
                String line;
                int counter = 0;
                while ((line = reader.readLine()) != null) {
                    counter++;
                    if (counter == 1) {
                        continue;
                    }
                    line = line.replace("\"", "");
                    line = line.replace("=", "");
                    String[] RowData = line.split(";");
                    String malli = RowData[0];
                    String numero = RowData[1];
                    String osoite = RowData[2];
                    String postinumero = RowData[3];
                    String postitoimipaikka = RowData[4];
                    String tyyppi = RowData[5];
                    String sijainti = RowData[6];
                    String aukioloaika = RowData[7];
                    String lisatiedot = RowData[8];
                    double latitude = Double.parseDouble(RowData[9]);
                    double longitude = Double.parseDouble(RowData[10]);
                    MarkerBean marker = new MarkerBean(malli, numero, osoite, postinumero, postitoimipaikka, tyyppi, sijainti, aukioloaika, lisatiedot, latitude, longitude);
                    markers.add(marker);
                }
            } catch (IOException ex) {
                Log.e("MarkerSetup/readLine()", "IOException");
            } finally {
                try {
                    csvStream.close();
                } catch (IOException e) {
                    Log.e("MarkerSetup/close()", "IOException");
                }
            }
        } catch (IOException e) {
            Log.e("MarkerSetup/open(path)", "IOException");
        }
        return markers;
    }

}
