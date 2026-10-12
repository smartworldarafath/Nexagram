package tw.nekomimi.nekogram.settings;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.EmojiThemes;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

public class UiAppearanceCell extends FrameLayout {

    public static final int[] COLORS = {
            0xFF5890C5, // 0: Sky Blue (Default)
            0xFF239853, // 1: Green
            0xFFE5A93C, // 2: Yellow
            0xFFF0F0F0, // 3: White
            0xFF8366CC  // 4: Violet
    };

    private static final String PREFS_KEY = "nagram_ui_appearance_color_idx";
    private final ColorCircleView[] circles = new ColorCircleView[COLORS.length];
    private int selectedIndex;

    public UiAppearanceCell(Context context) {
        super(context);
        selectedIndex = getSavedColorIndex();

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);

        for (int i = 0; i < COLORS.length; i++) {
            final int idx = i;
            ColorCircleView circle = new ColorCircleView(context, COLORS[i], i == 3);
            circle.setSelected(idx == selectedIndex, false);
            circle.setOnClickListener(v -> selectColor(idx));
            circles[i] = circle;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40));
            lp.leftMargin = AndroidUtilities.dp(8);
            lp.rightMargin = AndroidUtilities.dp(8);
            container.addView(circle, lp);
        }

        addView(container, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.CENTER));
    }

    public static int getSavedColorIndex() {
        SharedPreferences sp = ApplicationLoader.applicationContext.getSharedPreferences("nagram_ui", Context.MODE_PRIVATE);
        return sp.getInt(PREFS_KEY, 0);
    }

    public static void setSavedColorIndex(int index) {
        SharedPreferences sp = ApplicationLoader.applicationContext.getSharedPreferences("nagram_ui", Context.MODE_PRIVATE);
        sp.edit().putInt(PREFS_KEY, index).apply();
    }

    private void selectColor(int idx) {
        if (selectedIndex == idx) return;
        selectedIndex = idx;
        setSavedColorIndex(idx);

        for (int i = 0; i < circles.length; i++) {
            circles[i].setSelected(i == selectedIndex, true);
        }

        applyUiAppearanceColor(idx);
    }

    public static void applyUiAppearanceColor(int colorIndex) {
        try {
            Theme.ThemeInfo currentTheme = Theme.getCurrentTheme();
            if (currentTheme == null) return;
            boolean isDark = Theme.isCurrentThemeDark();

            int accentId = 99; // Default
            if (colorIndex == 0) {
                accentId = 99;
            } else if (colorIndex == 1) {
                accentId = 9;
            } else if (colorIndex == 2) {
                accentId = 7;
            } else if (colorIndex == 3) {
                accentId = 1003;
            } else if (colorIndex == 4) {
                accentId = 5;
            }

            Theme.ThemeAccent accent = null;
            if (currentTheme.themeAccentsMap != null) {
                accent = currentTheme.themeAccentsMap.get(accentId);
            }
            if (accent == null) {
                accent = new Theme.ThemeAccent();
                accent.id = accentId;
                accent.parentTheme = currentTheme;
                accent.accentColor = COLORS[colorIndex];
                if (colorIndex == 3) {
                    accent.accentColor = 0xFFEEEEEE;
                    accent.accentColor2 = 0xFFCCCCCC;
                }
                if (currentTheme.themeAccentsMap != null) {
                    currentTheme.themeAccentsMap.put(accentId, accent);
                }
                if (currentTheme.themeAccents != null) {
                    currentTheme.themeAccents.add(accent);
                }
            }

            currentTheme.currentAccentId = accentId;
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, currentTheme, isDark, null, accentId);
            EmojiThemes.saveCustomTheme(currentTheme, accentId);
            Theme.turnOffAutoNight(null);
            Theme.refreshThemeColors();
        } catch (Exception e) {
            org.telegram.messenger.FileLog.e(e);
        }
    }

    private static class ColorCircleView extends View {
        private final int color;
        private final boolean isLight;
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path checkPath = new Path();
        private boolean isSelected = false;
        private float scale = 1.0f;

        public ColorCircleView(Context context, int color, boolean isLight) {
            super(context);
            this.color = color;
            this.isLight = isLight;

            fillPaint.setColor(color);
            fillPaint.setStyle(Paint.Style.FILL);

            strokePaint.setColor(isLight ? 0x44888888 : 0x22000000);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(AndroidUtilities.dp(1.5f));

            checkPaint.setColor(isLight ? 0xFF333333 : 0xFFFFFFFF);
            checkPaint.setStyle(Paint.Style.STROKE);
            checkPaint.setStrokeWidth(AndroidUtilities.dp(2.5f));
            checkPaint.setStrokeCap(Paint.Cap.ROUND);
            checkPaint.setStrokeJoin(Paint.Join.ROUND);
        }

        public void setSelected(boolean selected, boolean animated) {
            this.isSelected = selected;
            if (animated) {
                ValueAnimator anim = ValueAnimator.ofFloat(scale, selected ? 1.15f : 1.0f);
                anim.setDuration(180);
                anim.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                anim.addUpdateListener(a -> {
                    scale = (float) a.getAnimatedValue();
                    invalidate();
                });
                anim.start();
            } else {
                scale = selected ? 1.15f : 1.0f;
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            float radius = (Math.min(getWidth(), getHeight()) / 2f - AndroidUtilities.dp(3)) * scale;

            // Draw color circle
            canvas.drawCircle(cx, cy, radius, fillPaint);
            // Draw border stroke
            canvas.drawCircle(cx, cy, radius, strokePaint);

            // Draw checkmark if selected
            if (isSelected) {
                checkPath.reset();
                float r = radius * 0.45f;
                checkPath.moveTo(cx - r, cy);
                checkPath.lineTo(cx - r * 0.2f, cy + r * 0.7f);
                checkPath.lineTo(cx + r, cy - r * 0.6f);
                canvas.drawPath(checkPath, checkPaint);
            }
        }
    }
}
