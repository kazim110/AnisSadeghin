package ir.anis.reader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;

/** A single branded entry screen, followed by MainActivity without leaving a splash in the back stack. */
public final class SplashActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean opened;
    private long startAt;
    private final Runnable proceed = this::openBook;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        startAt = state == null ? SystemClock.uptimeMillis() : state.getLong("startAt", SystemClock.uptimeMillis());
        getWindow().setStatusBarColor(Color.rgb(17, 55, 46));
        getWindow().setNavigationBarColor(Color.rgb(17, 55, 46));
        getWindow().getDecorView().setSystemUiVisibility(0);

        FrameLayout frame = new FrameLayout(this);
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(29, 82, 66), Color.rgb(12, 44, 38)});
        frame.setBackground(background);
        frame.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(frame);
        frame.setOnApplyWindowInsetsListener((v, insets) -> {
            frame.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        frame.requestApplyInsets();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        frame.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(28), dp(28), dp(28), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -1));
        addText(page, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 15, "#D3C193", false, 22);
        PortraitView photo = new PortraitView();
        photo.setContentDescription("تصویر آیت‌الله سید حسین یعقوبی قائنی");
        LinearLayout.LayoutParams portrait = new LinearLayout.LayoutParams(dp(216), dp(258));
        portrait.gravity = Gravity.CENTER;
        page.addView(photo, portrait);
        addText(page, "انیس الصادقین", 35, "#FFF8E6", true, 8);
        addText(page, "همراهی آرام در دعا و ذکر", 15, "#C7DBD0", false, 25);
        View rule = new View(this);
        rule.setBackgroundColor(Color.parseColor("#BDA976"));
        page.addView(rule, new LinearLayout.LayoutParams(dp(36), dp(1)));
        addText(page, "از رهنمودهای", 12, "#BBD0C4", false, 5);
        addText(page, "آیت‌الله سید حسین یعقوبی قائنی", 18, "#F2EAD2", true, 24);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D3C193")));
        progress.setContentDescription("آماده‌سازی کتاب");
        page.addView(progress, new LinearLayout.LayoutParams(dp(78), dp(3)));
    }

    private void addText(LinearLayout page, String value, int size, String color, boolean bold, int bottom) {
        TextView text = new TextView(this);
        text.setText(value); text.setTextSize(size); text.setTextColor(Color.parseColor(color));
        text.setGravity(Gravity.CENTER); text.setTextDirection(View.TEXT_DIRECTION_RTL);
        text.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(12); p.bottomMargin = dp(bottom);
        page.addView(text, p);
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onResume() {
        super.onResume();
        handler.postDelayed(proceed, Math.max(0, 1500 - (SystemClock.uptimeMillis() - startAt)));
    }
    @Override protected void onPause() { handler.removeCallbacks(proceed); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacks(proceed); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putLong("startAt", startAt); super.onSaveInstanceState(out); }
    void openBook() {
        if (opened || isFinishing() || isDestroyed()) return;
        opened = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    final class PortraitView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Bitmap portrait = BitmapFactory.decodeResource(getResources(), R.drawable.author_portrait);
        PortraitView() { super(SplashActivity.this); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float border = dp(7);
            RectF outer = new RectF(border, border, getWidth() - border, getHeight() - border);
            paint.setColor(Color.parseColor("#C3AD77")); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(1));
            canvas.drawRoundRect(outer, outer.width()/2, outer.width()/2, paint);
            RectF inner = new RectF(outer); inner.inset(dp(7), dp(7));
            canvas.save();
            Path clip = new Path(); clip.addRoundRect(inner, inner.width()/2, inner.width()/2, Path.Direction.CW); canvas.clipPath(clip);
            float scale = Math.max(inner.width()/portrait.getWidth(), inner.height()/portrait.getHeight());
            float w = portrait.getWidth()*scale, h=portrait.getHeight()*scale;
            RectF target = new RectF(inner.centerX()-w/2, inner.centerY()-h/2, inner.centerX()+w/2, inner.centerY()+h/2);
            paint.setStyle(Paint.Style.FILL); canvas.drawBitmap(portrait, null, target, paint);
            canvas.restore();
            paint.setColor(Color.parseColor("#D3C193"));
            canvas.drawCircle(getWidth()/2f, getHeight()-border, dp(3), paint);
        }
    }
}
