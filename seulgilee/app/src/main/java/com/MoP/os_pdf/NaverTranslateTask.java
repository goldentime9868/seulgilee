package com.MoP.os_pdf;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaverTranslateTask extends AsyncTask<String, Void, String> {
    String clientId = "z7J6NPnUTX43IST_x_Tw";
    String clientSecret = "RoQrH2ZPJA";
    String sourceLang = "en";
    String targetLang = "ko";
    boolean isEntire = false;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String sourceText = strings[0];
        String check = strings[1];
        Log.i("AAAA", check);
        if(check.equals("false")) {
            Log.i("AAAA", "wow");
            isEntire = false;
        } else if(check.equals(("true"))) {
            isEntire = true;
        }
        try {
            String text = URLEncoder.encode(sourceText, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
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
            //System.out.println(e);
            Log.d("error", e.getMessage());
            return null;
        }
    }

    // 번역된 결과를 받아서 처리
    @Override
    protected void onPostExecute(String result) {
        PdfActivity p = new PdfActivity();
        SummaryActivity s = new SummaryActivity();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);
        if (element.getAsJsonObject().get("errorMessage") != null) {
            Log.i("error", "Error!! " +
                    "[Code: " + element.getAsJsonObject().get("errorCode").getAsString() + "]");
        } else if (element.getAsJsonObject().get("message") != null) {
            // 번역 결과 출력
            if(isEntire == false) {
                p.textswitcher.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());
            }
            else if(isEntire == true) {
                s.textview.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());
            }
        }
    }
}