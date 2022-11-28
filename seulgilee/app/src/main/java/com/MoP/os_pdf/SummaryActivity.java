package com.MoP.os_pdf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;

public class SummaryActivity extends Activity {
    public static TextView textview;
    static ArrayList<String> sentences;
    static int count;
    public static int fontSize;
    static int number;
    static int sumCount;
    static String set = "Empty sentence";
    static boolean check = false;
    Spinner spinner;
    Spinner spinner_al;
    SummaryTask asyncTask;
    String algorithms = "luhn";
    public Button button;
    public boolean trans_check = false;
    NaverTranslateTask async;
    String temp = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_layout);
        Switch sw = (Switch) findViewById(R.id.switch1);
        Intent intent = getIntent();
        textview = (TextView) findViewById(R.id.textview_summary);
        sentences = getIntent().getStringArrayListExtra("text");
        count = intent.getExtras().getInt("count");
        fontSize = intent.getExtras().getInt("fontSize");
        number = intent.getExtras().getInt("number");
        Log.i("Test", "pdf view " + sentences.size() + " count: " + count + " font size: " + fontSize + " number: " + number);
        spinner = findViewById(R.id.spinner1);
        spinner_al = findViewById(R.id.spinner2);
        button = findViewById(R.id.trans);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(trans_check == false){
                    trans_check = true;
                    temp = textview.getText().toString();
                    async = new NaverTranslateTask();
                    async.execute(temp, "true");
                    button.setText("원본보기");
                }
                else if(trans_check == true){
                    trans_check = false;
                    textview.setText(temp);
                    button.setText("Translation");
                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sumCount = position + 3;
                setting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_al.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    algorithms = "luhn";
                }
                else if(position == 1){
                    algorithms = "edmundson";
                }
                else if(position == 2){
                    algorithms = "lsa";
                }
                else if(position == 3){
                    algorithms = "text-rank";
                }
                else if(position == 4){
                    algorithms = "lex-rank";
                }
                else if(position == 5){
                    algorithms = "sum-basic";
                }
                else if(position == 6){
                    algorithms = "kl";
                }
                setting();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == false) {
                    TextView t1 = findViewById(R.id.false_check);
                    TextView t2 = findViewById(R.id.true_check);
                    t1.setTextColor(Color.parseColor("#000000"));
                    t2.setTextColor(Color.parseColor("#808080"));
                    trans_check = false;
                    button.setText("Translation");

                } else if (isChecked == true) {
                    TextView t1 = findViewById(R.id.false_check);
                    TextView t2 = findViewById(R.id.true_check);
                    t2.setTextColor(Color.parseColor("#000000"));
                    t1.setTextColor(Color.parseColor("#808080"));
                    trans_check = false;
                    button.setText("Translation");
                }
                check = isChecked;
                setting();
            }

        });
    }

    public void setting() {
        int end;
        if (check) {
            set = "";
            for (int i = 0; i < sentences.size(); i++) {
                set += sentences.get(i) + "\n";
            }
        } else {
            if (sentences.size() >= 0) {
                set = "";
                if (count + number >= sentences.size()) {
                    end = sentences.size();
                } else {
                    end = count + number;
                }
                Log.i("Test", "" + end);
                for (int i = 0; i < end; i++) {
                    set += sentences.get(i) + "\n";
                }
            }
        }
        asyncTask = new SummaryTask();
        asyncTask.execute("http://13.209.168.0:3000/summary", set, String.valueOf(sumCount), algorithms);
    }
}