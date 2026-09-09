package xyz.nextalone.nagram.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class SpeedometerView extends View {

    private Paint backgroundArcPaint;
    private Paint progressArcPaint;
    private Paint tickPaint;
    private Paint tickTextPaint;
    private Paint needlePaint;
    private Paint hubPaint;
    private Paint speedTextPaint;
    private Paint unitTextPaint;

    private RectF arcBounds = new RectF();
    private float currentSpeed = 0f;
    private float animatedSpeed = 0f;
    private float maxSpeed = 100f; // in Mbps
    private ValueAnimator speedAnimator;

    private final float START_ANGLE = 140f;
    private final float SWEEP_ANGLE = 260f;

    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundArcPaint.setStyle(Paint.Style.STROKE);
        backgroundArcPaint.setStrokeCap(Paint.Cap.ROUND);

        progressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressArcPaint.setStyle(Paint.Style.STROKE);
        progressArcPaint.setStrokeCap(Paint.Cap.ROUND);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        tickTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickTextPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);

        hubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hubPaint.setStyle(Paint.Style.FILL);

        speedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint.setTextAlign(Paint.Align.CENTER);
        speedTextPaint.setFakeBoldText(true);

        unitTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unitTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setSpeed(float speedMbps) {
        if (speedMbps < 0) speedMbps = 0;
        if (speedMbps > maxSpeed) {
            maxSpeed = (float) (Math.ceil(speedMbps / 50.0) * 50.0);
        }
        currentSpeed = speedMbps;

        if (speedAnimator != null) {
            speedAnimator.cancel();
        }
        speedAnimator = ValueAnimator.ofFloat(animatedSpeed, currentSpeed);
        speedAnimator.setDuration(400);
        speedAnimator.setInterpolator(new DecelerateInterpolator());
        speedAnimator.addUpdateListener(animation -> {
            animatedSpeed = (float) animation.getAnimatedValue();
            invalidate();
        });
        speedAnimator.start();
    }

    public void reset() {
        setSpeed(0f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * 0.85f);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h * 0.52f;
        float radius = Math.min(w, h) * 0.40f;
        float strokeWidth = AndroidUtilities.dp(12);

        backgroundArcPaint.setStrokeWidth(strokeWidth);
        backgroundArcPaint.setColor(Theme.getColor(Theme.key_switchTrack) != 0 ? 
                Theme.getColor(Theme.key_switchTrack) : 0x22888888);

        int accentColor = Theme.getColor(Theme.key_featuredStickers_addButton);
        if (accentColor == 0) accentColor = 0xFF2EA6FF;

        progressArcPaint.setStrokeWidth(strokeWidth);
        progressArcPaint.setColor(accentColor);

        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw background arc
        canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE, false, backgroundArcPaint);

        // Draw active arc based on speed ratio
        float ratio = Math.min(1f, animatedSpeed / maxSpeed);
        if (ratio > 0.005f) {
            canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE * ratio, false, progressArcPaint);
        }

        // Ticks
        tickPaint.setStrokeWidth(AndroidUtilities.dp(1.5f));
        tickPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        tickTextPaint.setTextSize(AndroidUtilities.dp(10));
        tickTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));

        int tickCount = 10;
        for (int i = 0; i <= tickCount; i++) {
            float tRatio = i / (float) tickCount;
            float angle = (float) Math.toRadians(START_ANGLE + SWEEP_ANGLE * tRatio);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float rInner = radius - strokeWidth * 0.7f;
            float rOuter = radius + strokeWidth * 0.7f;
            float startX = cx + rInner * cos;
            float startY = cy + rInner * sin;
            float stopX = cx + rOuter * cos;
            float stopY = cy + rOuter * sin;

            canvas.drawLine(startX, startY, stopX, stopY, tickPaint);

            // Tick labels
            int val = Math.round(maxSpeed * tRatio);
            float rText = radius - strokeWidth * 1.6f;
            float textX = cx + rText * cos;
            float textY = cy + rText * sin + AndroidUtilities.dp(3.5f);
            canvas.drawText(String.valueOf(val), textX, textY, tickTextPaint);
        }

        // Needle
        float needleAngle = (float) Math.toRadians(START_ANGLE + SWEEP_ANGLE * ratio);
        float needleCos = (float) Math.cos(needleAngle);
        float needleSin = (float) Math.sin(needleAngle);

        float needleLen = radius - strokeWidth * 0.5f;
        needlePaint.setColor(accentColor);
        needlePaint.setStrokeWidth(AndroidUtilities.dp(3.5f));

        canvas.drawLine(cx, cy, cx + needleLen * needleCos, cy + needleLen * needleSin, needlePaint);

        // Hub circle
        hubPaint.setColor(accentColor);
        canvas.drawCircle(cx, cy, AndroidUtilities.dp(7), hubPaint);
        hubPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, AndroidUtilities.dp(3), hubPaint);

        // Speed text in center-bottom
        speedTextPaint.setTextSize(AndroidUtilities.dp(32));
        speedTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        String speedStr = String.format(java.util.Locale.US, "%.1f", animatedSpeed);
        canvas.drawText(speedStr, cx, cy + AndroidUtilities.dp(44), speedTextPaint);

        unitTextPaint.setTextSize(AndroidUtilities.dp(13));
        unitTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        canvas.drawText("Mbps", cx, cy + AndroidUtilities.dp(60), unitTextPaint);
    }
}
