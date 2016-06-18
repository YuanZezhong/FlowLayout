package yuanyi.com.flowlayout;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import yuanyi.com.flowlayout.util.DensityUtils;
import yuanyi.com.flowlayout.view.FlowLayout;

public class MainActivity extends AppCompatActivity {
    private static final int[] FLOW_GRAVITYS = {FlowLayout.CENTER, FlowLayout.CENTER_HORIZONTAL,
            FlowLayout.CENTER_VERTICAL, FlowLayout.LEFT, FlowLayout.RIGHT};

    private FlowLayout flowLayout;
    private View button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        registerListener();
    }

    private void initView() {
        flowLayout = ((FlowLayout) findViewById(R.id.flowLayout));
        button = findViewById(R.id.button);
        initFlowLayout();
    }

    private void registerListener() {
        button.setOnClickListener(new View.OnClickListener() {
            private int cnt = 0;

            @Override
            public void onClick(View v) {
                flowLayout.setFlowGravity(FLOW_GRAVITYS[cnt++ % FLOW_GRAVITYS.length]);
            }
        });
    }


    private void initFlowLayout() {
        int padding = DensityUtils.dp2px(getApplicationContext(), 8);
        int height = DensityUtils.dp2px(getApplicationContext(), 36);

        TextView tv;
        Random random = new Random();
        for (int i = 0; i < 20; ++i) {
            tv = new TextView(getApplicationContext());
            tv.setPadding(padding, padding, padding, padding);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            StringBuilder sb = new StringBuilder();
            int cnt = random.nextInt(10) + 2;
            for (int j = 0; j < cnt; ++j) {
                sb.append(CHARS[random.nextInt(CHARS.length)]);
            }
            tv.setText(sb);

            GradientDrawable background = new GradientDrawable(null, null);
            background.setCornerRadius(DensityUtils.dp2px(getApplicationContext(), 8));
            background.setColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            tv.setBackgroundDrawable(background);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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
