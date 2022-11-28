package com.MoP.os_pdf;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SummaryTask extends AsyncTask<String, Void, String> {
    public String Final;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String sumURL = strings[0];
        String set = strings[1];
        String sumCount = strings[2];
        String algorithm = strings[3];
        Log.i("Summary", sumURL + ", " + set + ", " + sumCount + ", " + algorithm);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("text", set);
            jsonObject.accumulate("number", sumCount);
            jsonObject.accumulate("algorithm", algorithm);
            URL url = new URL(sumURL);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();

            OutputStream outStream = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();

            int responseCode = con.getResponseCode();
            Log.i("Summary", "Response Code: " + responseCode);
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            Log.d("error", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Final = "";
        SummaryActivity sa = new SummaryActivity();
        Final = result.replace("\\n", "\n\n");
        sa.textview.setText(Final);
        sa.textview.setTextSize(sa.fontSize);
    }
}