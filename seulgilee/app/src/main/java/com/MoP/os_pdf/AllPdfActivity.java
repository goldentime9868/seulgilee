package com.MoP.os_pdf;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.ArrayList;

public class AllPdfActivity extends MainActivity {
    String filePath;
    Toolbar myToolbar;
    ArrayList<String> test;
    int count;
    int fontSize;
    int number;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_pdf_layout);

        Intent intent = getIntent();
        filePath = intent.getExtras().getString("fileName");
        viewPdf(filePath);
        myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
        myToolbar.setBackgroundColor(Color.argb(50, 50, 50, 50));

        test = getIntent().getStringArrayListExtra("text");
        count = intent.getExtras().getInt("count");
        fontSize = intent.getExtras().getInt("fontSize");
        number = intent.getExtras().getInt("number");
        Log.i("Test", "pdf view " + test.size() + "    " + count);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.summary:
                Toast.makeText(AllPdfActivity.this, "summary.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AllPdfActivity.this, SummaryActivity.class);
                intent.putExtra("text", (ArrayList<String>)test);
                intent.putExtra("count", count);
                intent.putExtra("fontSize", fontSize);
                intent.putExtra("number", number);

                startActivity(intent);
                break;
        }

        return true;
    }

    private void viewPdf(String pdfFileName) {
        File pdfFile = new File(pdfFileName);
        setContentView(R.layout.all_pdf_layout);
        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.fromFile(pdfFile).load();
    }
}
