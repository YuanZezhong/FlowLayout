package yuanyi.com.flowlayout;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import yuanyi.com.flowlayout.view.FlowLayout;

public class MainActivity extends AppCompatActivity {
    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        //initData();
        registerListener();
    }

    private void initView() {
        flowLayout = ((FlowLayout) findViewById(R.id.flowLayout));
    }

    private void registerListener() {
    }

    private int dip2px(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    private void initData() {
        int padding = dip2px(8);
        int minWidth = dip2px(30);
        int height = dip2px(40);
        int textSize = dip2px(14);

        TextView tv;
        Random random = new Random();
        for (int i = 0; i < 20; ++i) {
            tv = new TextView(getApplicationContext());
            tv.setPadding(padding, padding, padding, padding);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            tv.setTextColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            tv.setTextSize(textSize);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 4; ++j) {
                sb.append(CHARS[random.nextInt(CHARS.length)]);
            }
            tv.setText(sb);

            int width = random.nextInt(minWidth) + 100;
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
            flowLayout.addView(tv, layoutParams);
        }
    }

    private static final char[] CHARS;
    static {
        CHARS = new char[26 + 26 + 10];
        char ch = '0';
        for (int i = 0; i < CHARS.length; ++i) {
            CHARS[i] = ch;
            if (ch == '9') {
                ch = 'A' - 1;
            } else if (ch == 'Z') {
                ch = 'a' - 1;
            }
            ch += 1;
        }
    }
}
