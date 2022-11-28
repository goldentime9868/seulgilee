package com.MoP.os_pdf;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class PdfActivity extends AppCompatActivity implements View.OnTouchListener {
    private String filePath;
    private PDFTextStripper pdfStripper;
    private String text;
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private Cursor cursor;

    public List<String> sentences = new ArrayList<>();
    List<Bitmap> images = new ArrayList<>();

    private static ImageButton next;
    private static ImageButton prev;
    private static ImageButton nextImg;
    private static ImageButton prevImg;
    public static TextSwitcher textswitcher;
    private static ImageSwitcher imageswitcher;
    private static TextView myText;

    Button translate;
    Boolean trans = true;

    Animation in;
    Animation out;

    BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    public int count = 0;
    private int imgCount = 0;

    Toolbar myToolbar;

    float initX = 0;
    float initY = 0;
    float changeX = 0;
    float changeY = 0;
    NaverTranslateTask asyncTask;
    int number = 1;
    int fontSize = 30;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent2 = new Intent(PdfActivity.this, LoadingActivity.class);
        startActivity(intent2);
        setContentView(R.layout.pdf_layout);
        helper = new DatabaseHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
        PDFBoxResourceLoader.init(getApplicationContext());
        Intent intent = getIntent();
        filePath = intent.getExtras().getString("fileName");
        fontSize = intent.getExtras().getInt("fontSize");

        cursor = db.rawQuery("SELECT * FROM info WHERE NAME ='"
                + filePath + "';", null);

        // 데이터베이스에 항목이 없을 경우 삽입
        if (cursor.getCount() == 0) {
            db.execSQL("INSERT INTO info VALUES (null, '" + filePath + "', '0', '0', '1');");
        } else { // 항목이 있을 경우 count, imgcount, number 값 가져오기
            while (cursor.moveToNext()) {
                Log.i("DB", cursor.getString(1));
                Log.i("DB", "1 " + cursor.getInt(2));
                Log.i("DB", "2 " + cursor.getInt(3));
                Log.i("DB", "3 " + cursor.getInt(4));
                count = cursor.getInt(2);
                imgCount = cursor.getInt(3);
                number = cursor.getInt(4);
            }
        }

        translate = (Button) findViewById(R.id.trans_button);
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trans == true) {
                    pdfSlide(2, true, true);
                    translate.setText("원본보기");
                    trans = false;
                } else if (trans == false) {
                    pdfSlide(2, true, false);
                    translate.setText("TRANSLATE");
                    trans = true;
                }
            }
        });
        init();
    }

    //  Text switcher
    void init() {
        textswitcher = (TextSwitcher) findViewById(R.id.textSwitcher);
        imageswitcher = (ImageSwitcher) findViewById(R.id.imgSwitcher);
        next = (ImageButton) findViewById(R.id.next);
        prev = (ImageButton) findViewById(R.id.prev);
        nextImg = (ImageButton) findViewById(R.id.nextImage);
        prevImg = (ImageButton) findViewById(R.id.prevImage);

        in = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        out = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfSlide(0, true, false);
                if (trans == false) {
                    translate.setText("TRANSLATE");
                    trans = true;
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pdfSlide(1, true, false);
                if (trans == false) {
                    translate.setText("TRANSLATE");
                    trans = true;
                }
            }
        });
        prevImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pdfSlide(0, false, false);
            }
        });
        nextImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pdfSlide(1, false, false);
            }
        });

        textswitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                myText = new TextView(PdfActivity.this);
                myText.setTextColor(Color.BLACK);
                myText.setGravity(Gravity.TOP | Gravity.START);
                myText.setTextSize(fontSize);
                myText.setEnabled(true);
                myText.setTextIsSelectable(true);
                myText.setFocusable(true);
                myText.setLongClickable(true);
                return myText;
            }
        });

        imageswitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
//                ImageView.
//                        setGravity(Gravity.TOP | Gravity.CENTER_VERTICAL);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);

                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
        myToolbar.setBackgroundColor(Color.argb(50, 50, 50, 50));
        extractText(filePath);
        if (sentences.size() != 0) {
            pdfSlide(2, true, false);
        }
        extractImage(filePath);
        if (images.size() != 0)
            imageswitcher.setImageDrawable(new BitmapDrawable(getResources(), images.get(imgCount)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View v;
        PopupMenu p;
        switch (item.getItemId()) {
            case R.id.setting_number_of_sentences:
                v = findViewById(R.id.empty);
                p = new PopupMenu(
                        getApplicationContext(), // 현재 화면의 제어권자
                        v); // anchor : 팝업을 띄울 기준될 위젯
                getMenuInflater().inflate(R.menu.menu_number_of_sentences, p.getMenu());
                // 이벤트 처리
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sentence_one:
                                number = 1;
                                pdfSlide(2, true, false);
                                break;
                            case R.id.sentence_two:
                                number = 2;
                                pdfSlide(2, true, false);
                                break;
                            case R.id.sentence_three:
                                number = 3;
                                pdfSlide(2, true, false);
                                break;
                            case R.id.sentence_four:
                                number = 4;
                                pdfSlide(2, true, false);
                                break;
                        }
                        return false;
                    }
                });
                p.setGravity(Gravity.CENTER_HORIZONTAL);
                p.show(); // 메뉴를 띄우기
                break;
            case R.id.all_view:
                Intent intent = new Intent(PdfActivity.this, AllPdfActivity.class);
                intent.putExtra("fileName", filePath);
                intent.putExtra("text", (ArrayList<String>) sentences);
                intent.putExtra("count", count);
                intent.putExtra("fontSize", fontSize);
                intent.putExtra("number", number);
                startActivity(intent);
                break;
            case R.id.summary:
                Intent intent2 = new Intent(PdfActivity.this, SummaryActivity.class);
                intent2.putExtra("text", (ArrayList<String>) sentences);
                intent2.putExtra("count", count);
                intent2.putExtra("fontSize", fontSize);
                intent2.putExtra("number", number);
                startActivity(intent2);
                break;
            case R.id.action_search:
                MenuItem mSearch = item;
                SearchView mSearchView = (SearchView) mSearch.getActionView();
                mSearchView.setQueryHint("Search");

                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, s);
                        startActivity(intent);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
        }
        return true;
    }

    private void pdfSlide(int mode, Boolean isText, Boolean translation) { // 0: prev, 1: next, 2: render
        int localCount, localSize;
        String set = "";

        if (isText && sentences.size() == 0)
            return;
        if (!isText && images.size() == 0)
            return;
        if (isText) {
            localCount = count;
            localSize = sentences.size();
        } else {
            localCount = imgCount;
            localSize = images.size();
        }
        if (mode == 0 && localCount > 0) {
            in = AnimationUtils.loadAnimation(PdfActivity.this,
                    R.anim.left_in);
            out = AnimationUtils.loadAnimation(PdfActivity.this,
                    R.anim.right_out);
            if (localCount - number >= 0)
                localCount -= number;
            else
                localCount = -1;
        } else if (mode == 1 && localCount < localSize - 1) {

            in = AnimationUtils.loadAnimation(PdfActivity.this,
                    R.anim.right_in);
            out = AnimationUtils.loadAnimation(PdfActivity.this,
                    R.anim.left_out);
            if (localCount + number < localSize)
                localCount += number;
            else {
                localCount = localSize;
            }
        } else {
            in = null;
            out = null;
        }
        Log.i("Test", "count: " + localCount);
        Log.i("Test", "font: " + fontSize);
        Log.i("Test", "mode: " + mode);
        if (localCount < 0) {
            localCount = 0;
        }
        if (localCount >= localSize) {
            localCount = localSize - 1;
        }
        if (isText) {
            for (int i = 0; (localCount + i) < sentences.size() && i < number; i++) {
                set += sentences.get(localCount + i) + "\n\n";
            }
            if (translation == true) {
                asyncTask = new NaverTranslateTask();
                asyncTask.execute(set, "false");
            }
            textswitcher.setInAnimation(in);
            textswitcher.setOutAnimation(out);
            textswitcher.setText(set);
            count = localCount;
            myText.setText(myText.getText());
        } else {
            imageswitcher.setInAnimation(in);
            imageswitcher.setOutAnimation(out);
            imageswitcher.setImageDrawable(new BitmapDrawable(getResources(), images.get(localCount)));
            imgCount = localCount;
        }
        // Update DB
        db.execSQL("UPDATE info SET COUNT  = '" + count + "', IMGCOUNT = '" + imgCount + "', NUMBER = '" + number + "'  WHERE NAME ='" + filePath + "';");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        initX = event.getX();
        initY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initX = event.getX();
                initY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                changeX = event.getX();
                changeY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                changeX = event.getX();
                changeY = event.getY();
                break;
            default:
                return false;

        }
        return true;
    }

    private void extractText(String pdfFilePath) {
        File file = new File(pdfFilePath);
        try {
            PDDocument document = PDDocument.load(file);
            pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(document);
            if (text.substring(0, 30).matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                // 한글이 포함된 문자열
                parsing_korean(text);
            } else {
                // 영어
                parsing(text);
            }

            document.close();
        } catch (Exception e) {
            //...
        }
        Log.i("Test", " " + sentences.size() + "   " + count);
    }

    private void parsing_korean(String text) {
        try {
            InputStream inputStream = getAssets().open("en-sent.bin");
            SentenceModel model = new SentenceModel(inputStream);
            SentenceDetectorME detector = new SentenceDetectorME(model);
            String sentencesArray[] = detector.sentDetect(text);
            for(int i = 0; i < sentencesArray.length; i ++) {
                sentencesArray[i] = sentencesArray[i].replace("\n", "");
            }
            sentences = new ArrayList<String>(Arrays.asList(sentencesArray));
        } catch (Exception e) {
            //
        }
    }

    private void parsing(String text) {
        iterator.setText(text);

        int lastIndex = iterator.first();
        String temp = " ";
        String comp = " ";

        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = iterator.next();

            if (lastIndex != BreakIterator.DONE) {

                //한 문장씩 끊어줌
                String sentence = text.substring(firstIndex, lastIndex);
                int prev = 1;
                int now = 1;
                int len = 0;
                //sentence 길이가 0 - 빈 문장이면, 추가 하지 않음
                if (sentence.length() == 0) {
                    continue;
                }

                //개행되는 부분을 공백으로 채워줌 - 나중에 equals 비교를 위해
                sentence = sentence.replaceAll(System.getProperty("line.separator"), " ");

                //문장을 일단 추가함.
                sentences.add(sentence);

                //지금 추가한 것의 전 문장의 index와 지금 문장의 index 값을 저장
                if (sentences.size() > 1) {
                    prev = sentences.size() - 2;
                    now = sentences.size() - 1;
                } else
                    continue;
                //직전 문장 == temp
                temp = sentences.get(prev);

                //직전 문장의 길이
                len = temp.length();
                //전 문장이 -으로 끝났는 지 확인하기 위해서

                if (len > 2)
                    comp = temp.substring(len - 2, len);

                if (comp.equals("- ")) {
                    String result = temp.concat(sentence);
                    sentences.remove(sentences.size() - 2);
                    sentences.remove(sentences.size() - 1);
                    sentences.add(result);
                }

                //맨 앞 글자가 소문자이면 그 앞에 것과 합쳐줌
                char check_lower = sentences.get(sentences.size() - 1).charAt(0);
                if (Character.isLowerCase(check_lower) && sentences.size() > 1) {
                    merge();
                }

                // 지금 글자 수가 적고 끝이 .으로 끝나면
                String dot_str = sentences.get(sentences.size() - 1);
//                if(len > 0 && dot_str.substring(len - 1, len).equals(". ")){
//                    sentences.add("dot으로 끝남..");
//                }
                len = dot_str.length();
                if (len > 1) {
                    if (dot_str.substring(len - 2, len).equals(". ") && (dot_str.length() < 25)) {
                        merge();
                    }
                }

                //맨 앞 글자 특수문자이면 그 앞이랑 합쳐줌
                String special = sentences.get(sentences.size() - 1);
                if (special.length() > 0) {
                    String check_spe = special.substring(0, 1);
                    if (!check_spe.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
                        merge();
                    }
                }

                //연도 인지, 앞에 숫자 4개로 이루어져 있는지..
                String number = sentences.get(sentences.size() - 1);
                number = number.trim();
                if (number.length() > 3) {
                    String num = number.substring(0, 4);
                    if (isNumber(num))
                        merge();
                }

                //전치사로 끝나고 끝에 .?!가 없을 때...
                if (sentences.size() > 1) {
                    String str = sentences.get(sentences.size() - 2);
                    String[] array = str.split(" ");
                    String[] preposition = {"about", "above", "after", "against", "along", "around", "at", "beside", "beneath",
                            "between", "but", "by", "down", "during", "for", "from", "in", "into", "of", "off", "on", "out",
                            "over", "per", "round", "since", "through", "till", "to", "toward", "until", "up", "upon", "with",
                            "within", "without", "as"};
                    if (array.length > 0) {
                        //마지막 글자 : array[array.length - 1]

                        String temp_pre = array[array.length - 1].trim();
                        int len_pre = temp_pre.length();
                        if (len_pre > 0) {
                            String temp_pre_check = temp_pre.substring(len_pre - 1, len_pre);
                            if (!(temp_pre_check.equals(".") || temp_pre_check.equals("?") || temp_pre_check.equals("!"))) {
                                temp_pre_check = temp_pre.substring(0, len_pre);
                                for (int i = 0; i < preposition.length; i++) {
                                    String tmp_lower = temp_pre_check.toLowerCase();
                                    if (tmp_lower.equals(preposition[i])) {
                                        merge();
                                    }
                                }
                            }
                        }

                    }
                }
                //관사 체크
                if (sentences.size() > 1) {
                    String str_g = sentences.get(sentences.size() - 2);
                    String[] array_g = str_g.split(" ");
                    String[] article = {"a", "an", "the"};
                    if (array_g.length > 0) {
                        String temp_g = array_g[array_g.length - 1].trim();
                        int len_g = temp_g.length();
                        if (len_g > 0) {
                            String temp_g_check = temp_g.substring(len_g - 1, len_g);
                            if (!(temp_g_check.equals(".") || temp_g_check.equals("?") || temp_g_check.equals("!"))) {
                                temp_g_check = temp_g.substring(0, len_g);
                                for (int i = 0; i < article.length; i++) {
                                    String tmp_lower = temp_g_check.toLowerCase();
                                    if (tmp_lower.equals(article[i])) {
                                        merge();
                                    }
                                }
                            }
                        }
                    }
                }
                //맨 끝이 ,&:(로 끝나면 그 다음거랑 합쳐줌.
                if (sentences.size() > 1) {
                    String temp_g = sentences.get(sentences.size() - 2).trim();
                    int len_g = temp_g.length();
                    if (len_g > 0) {
                        String temp_g_check = temp_g.substring(len_g - 1, len_g);
                        if (temp_g_check.equals(",") || temp_g_check.equals("{") || temp_g_check.equals("&") || temp_g_check.equals(":") || temp_g_check.equals("(") || temp_g_check.equals("[")) {
                            merge();
                        }
                    }
                }
                //reference를 만나면 그 뒤에 잘라버리기
                //무조건 하나는 받고 시작하므로 size는 1이상임.
                String reference = sentences.get(sentences.size() - 1);
                reference = reference.trim();
                reference = reference.toLowerCase();
                if (reference.equals("references")) {
                    sentences.remove(sentences.size() - 1);
                    break;
                }
            }
        }
    }

    private void merge() {
        String upper = sentences.get(sentences.size() - 1);
        String prev_str = sentences.get(sentences.size() - 2);
        String result = prev_str.concat(upper);
        sentences.remove(sentences.size() - 2);
        sentences.remove(sentences.size() - 1);
        sentences.add(result);
    }

    //문자열이 숫자(정수, 실수)인지 아닌지 판별한다.
    static boolean isNumber(String str) {
        char tempCh;
        int dotCount = 0;    //실수일 경우 .의 개수를 체크하는 변수
        boolean result = true;

        for (int i = 0; i < str.length(); i++) {
            tempCh = str.charAt(i);    //입력받은 문자열을 문자단위로 검사
            //아스키 코드 값이 48 ~ 57사이면 0과 9사이의 문자이다.
            if ((int) tempCh < 48 || (int) tempCh > 57) {
                //만약 0~9사이의 문자가 아닌 tempCh가 .도 아니거나
                //.의 개수가 이미 1개 이상이라면 그 문자열은 숫자가 아니다.
                if (tempCh != '.' || dotCount > 0) {
                    result = false;
                    break;
                } else {
                    //.일 경우 .개수 증가
                    dotCount++;
                }
            }
        }
        return result;
    }

    // 이미지 추출
    private void extractImage(String pdfFilePath) {
        try {
            File file = new File(pdfFilePath);
            PDDocument document = PDDocument.load(file);
            PDPageTree list = document.getPages();
            for (PDPage page : list) {
                PDResources pdResources = page.getResources();
                for (COSName name : pdResources.getXObjectNames()) {
                    PDXObject o = pdResources.getXObject(name);
                    if (o instanceof PDImageXObject) {
                        PDImageXObject pdfImage = (PDImageXObject) o;
                        images.add(pdfImage.getImage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Exception while trying to create pdf document - " + e);
        }
    }
}
