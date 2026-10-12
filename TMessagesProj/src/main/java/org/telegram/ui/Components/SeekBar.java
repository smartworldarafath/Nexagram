/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.Collections;

public class SeekBar {

    public interface SeekBarDelegate {
        void onSeekBarDrag(float progress);
        default void onSeekBarContinuousDrag(float progress) {

        }
        default void onSeekBarPressed() {}
        default void onSeekBarReleased() {}

        default boolean isSeekBarDragAllowed() {
            return true;
        }

        default boolean reverseWaveform() {
            return false;
        }
    }

    private static Paint paint;
    private static int thumbWidth;
    private float thumbProgress;
    private int thumbX = 0;
    private int draggingThumbX = 0;
    private int thumbDX = 0;
    private boolean pressed = false;
    private int width;
    private int height;
    private SeekBarDelegate delegate;
    private int backgroundColor;
    private int cacheColor;
    private int circleColor;
    private int progressColor;
    private int backgroundSelectedColor;
    private RectF rect = new RectF();
    private int lineHeight = AndroidUtilities.dp(2);
    private boolean selected;
    private float bufferedProgress;
    private float currentRadius;
    private long lastUpdateTime;
    private View parentView;
    private float alpha = 1f;

    private Paint glassThumbPillPaint;
    private Paint glassThumbOpticalGlassPaint;
    private Paint glassThumbRimPaint;
    private Paint glassThumbRefractionPaint;
    private Paint glassThumbGlarePaint;
    private final RectF glassThumbBounds = new RectF();
    private final RectF glassThumbInnerBounds = new RectF();
    private final Path glassThumbClipPath = new Path();
    private float squishProgress = 0f;
    private long lastSquishTime = 0;

    public SeekBar(View parent) {
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        parentView = parent;
        thumbWidth = AndroidUtilities.dp(24);
        currentRadius = AndroidUtilities.dp(6);
    }

    public void setParent(View parent) {
        parentView = parent;
    }

    public void setDelegate(SeekBarDelegate seekBarDelegate) {
        delegate = seekBarDelegate;
    }

    public boolean onTouch(int action, float x, float y) {
        if (action == MotionEvent.ACTION_DOWN) {
            int additionWidth = (height - thumbWidth) / 2;
            if (x >= -additionWidth && x <= width + additionWidth && y >= 0 && y <= height) {
                if (!(thumbX - additionWidth <= x && x <= thumbX + thumbWidth + additionWidth)) {
                    thumbX = (int) x - thumbWidth / 2;
                    if (thumbX < 0) {
                        thumbX = 0;
                    } else if (thumbX > width - thumbWidth) {
                        thumbX = width - thumbWidth;
                    }
                }
                pressed = true;
                draggingThumbX = thumbX;
                thumbDX = (int) (x - thumbX);
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (pressed) {
                thumbX = draggingThumbX;
                if (action == MotionEvent.ACTION_UP && delegate != null) {
                    delegate.onSeekBarDrag((float) thumbX / (float) (width - thumbWidth));
                }
                pressed = false;
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (pressed) {
                draggingThumbX = (int) (x - thumbDX);
                if (draggingThumbX < 0) {
                    draggingThumbX = 0;
                } else if (draggingThumbX > width - thumbWidth) {
                    draggingThumbX = width - thumbWidth;
                }
                if (delegate != null) {
                    delegate.onSeekBarContinuousDrag((float) draggingThumbX / (float) (width - thumbWidth));
                }
                return true;
            }
        }
        return false;
    }

    public void setColors(int background, int cache, int progress, int circle, int selected) {
        backgroundColor = background;
        cacheColor = cache;
        circleColor = circle;
        progressColor = progress;
        backgroundSelectedColor = selected;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setProgress(float progress) {
        thumbProgress = progress;
        thumbX = (int) Math.ceil((width - thumbWidth) * thumbProgress);
        if (thumbX < 0) {
            thumbX = 0;
        } else if (thumbX > width - thumbWidth) {
            thumbX = width - thumbWidth;
        }
    }

    public void setBufferedProgress(float value) {
        bufferedProgress = value;
    }

    public float getProgress() {
        return (float) thumbX / (float) (width - thumbWidth);
    }

    public int getThumbX() {
        return (pressed ? draggingThumbX : thumbX) + thumbWidth / 2;
    }

    public boolean isDragging() {
        return pressed;
    }

    public void setSelected(boolean value) {
        selected = value;
    }

    public void setSize(int w, int h) {
        if (width == w && height == h) {
            return;
        }
        width = w;
        height = h;
        setProgress(thumbProgress);
    }

    public int getWidth() {
        return width - thumbWidth;
    }

    public void setLineHeight(int value) {
        lineHeight = value;
    }

    public void draw(Canvas canvas) {
        if (alpha <= 0) {
            return;
        }
        if (alpha < 1) {
            canvas.saveLayerAlpha(0, 0, width, height, (int) (255 * alpha), Canvas.ALL_SAVE_FLAG);
        }
        int effectiveLineH = LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS) ? AndroidUtilities.dp(5) : lineHeight;
        rect.set(thumbWidth / 2, height / 2 - effectiveLineH / 2, width - thumbWidth / 2, height / 2 + effectiveLineH / 2);
        paint.setColor(selected ? backgroundSelectedColor : backgroundColor);
        drawProgressBar(canvas, rect, paint);
        if (bufferedProgress > 0) {
            paint.setColor(selected ? backgroundSelectedColor : cacheColor);
            rect.set(thumbWidth / 2, height / 2 - effectiveLineH / 2, thumbWidth / 2 + bufferedProgress * (width - thumbWidth), height / 2 + effectiveLineH / 2);
            drawProgressBar(canvas, rect, paint);
        }
        rect.set(thumbWidth / 2, height / 2 - effectiveLineH / 2, thumbWidth / 2 + (pressed ? draggingThumbX : thumbX), height / 2 + effectiveLineH / 2);
        paint.setColor(progressColor);
        drawProgressBar(canvas, rect, paint);
        paint.setColor(circleColor);

        int newRad = AndroidUtilities.dp(pressed ? 8 : 6);
        if (currentRadius != newRad) {
            long newUpdateTime = SystemClock.elapsedRealtime();
            long dt = newUpdateTime - lastUpdateTime;
            if (dt > 18) {
                dt = 16;
            }
            if (currentRadius < newRad) {
                currentRadius += AndroidUtilities.dp(1) * (dt / 60.0f);
                if (currentRadius > newRad) {
                    currentRadius = newRad;
                }
            } else {
                currentRadius -= AndroidUtilities.dp(1) * (dt / 60.0f);
                if (currentRadius < newRad) {
                    currentRadius = newRad;
                }
            }
            if (parentView != null) {
                parentView.invalidate();
            }
        }

        if (LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS)) {
            drawLiquidGlassThumb(canvas, (pressed ? draggingThumbX : thumbX) + thumbWidth / 2f, height / 2f);
            if (Math.abs((pressed ? 1.0f : 0.0f) - squishProgress) > 0.005f && parentView != null) {
                parentView.invalidate();
            }
        } else {
            canvas.drawCircle((pressed ? draggingThumbX : thumbX) + thumbWidth / 2, height / 2, currentRadius, paint);
        }

        if (alpha < 1) {
            canvas.restore();
        }

        updateTimestampAnimation();
    }

    private void drawLiquidGlassThumb(Canvas canvas, float cx, float cy) {
        if (glassThumbPillPaint == null) {
            glassThumbPillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glassThumbPillPaint.setStyle(Paint.Style.FILL);

            glassThumbOpticalGlassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glassThumbOpticalGlassPaint.setStyle(Paint.Style.FILL);

            glassThumbRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glassThumbRimPaint.setStyle(Paint.Style.STROKE);
            glassThumbRimPaint.setStrokeWidth(AndroidUtilities.dpf2(1.1f));

            glassThumbRefractionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glassThumbRefractionPaint.setStyle(Paint.Style.FILL);

            glassThumbGlarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glassThumbGlarePaint.setStyle(Paint.Style.FILL);
        }

        float targetSquish = pressed ? 1.0f : 0.0f;
        long now = SystemClock.uptimeMillis();
        if (lastSquishTime > 0) {
            float dt = Math.min(32, now - lastSquishTime) / 1000f;
            float diff = targetSquish - squishProgress;
            if (Math.abs(diff) > 0.002f) {
                float springK = pressed ? 20f : 24f;
                squishProgress += diff * Math.min(1.0f, dt * springK);
            } else {
                squishProgress = targetSquish;
            }
        }
        lastSquishTime = now;

        // Rest: 24dp x 16dp. Pressed: 36dp x 20dp capsule pill
        float thumbW = AndroidUtilities.dpf2(24f + 12f * squishProgress);
        float thumbH = AndroidUtilities.dpf2(16f + 4f * squishProgress);
        float thumbR = thumbH / 2f;

        canvas.save();
        glassThumbBounds.set(cx - thumbW / 2f, cy - thumbH / 2f, cx + thumbW / 2f, cy + thumbH / 2f);

        boolean isDark = Theme.isCurrentThemeDark();

        // 1. Clear Liquid Glass Core when pressed
        if (squishProgress > 0.01f) {
            int coreColor = isDark ? 0x22FFFFFF : 0x16FFFFFF;
            glassThumbOpticalGlassPaint.setColor(coreColor);
            glassThumbOpticalGlassPaint.setAlpha((int) (Color.alpha(coreColor) * squishProgress));
            canvas.drawRoundRect(glassThumbBounds, thumbR, thumbR, glassThumbOpticalGlassPaint);

            canvas.save();
            glassThumbClipPath.reset();
            glassThumbClipPath.addRoundRect(glassThumbBounds, thumbR, thumbR, Path.Direction.CW);
            canvas.clipPath(glassThumbClipPath);

            RadialGradient leftRefract = new RadialGradient(
                    glassThumbBounds.left + thumbR * 0.7f, cy,
                    thumbR * 1.5f,
                    new int[]{0x7545C5FF, 0x3030A0FF, 0x00000000},
                    new float[]{0.0f, 0.55f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            glassThumbRefractionPaint.setShader(leftRefract);
            glassThumbRefractionPaint.setAlpha((int) (220 * squishProgress));
            canvas.drawRect(glassThumbBounds.left, glassThumbBounds.top, glassThumbBounds.left + thumbR * 1.6f, glassThumbBounds.bottom, glassThumbRefractionPaint);

            RadialGradient rightRefract = new RadialGradient(
                    glassThumbBounds.right - thumbR * 0.7f, cy,
                    thumbR * 1.5f,
                    new int[]{0x7545C5FF, 0x3030A0FF, 0x00000000},
                    new float[]{0.0f, 0.55f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            glassThumbRefractionPaint.setShader(rightRefract);
            glassThumbRefractionPaint.setAlpha((int) (220 * squishProgress));
            canvas.drawRect(glassThumbBounds.right - thumbR * 1.6f, glassThumbBounds.top, glassThumbBounds.right, glassThumbBounds.bottom, glassThumbRefractionPaint);

            LinearGradient glare = new LinearGradient(
                    cx, glassThumbBounds.top,
                    cx, glassThumbBounds.top + thumbH * 0.45f,
                    new int[]{0x40FFFFFF, 0x00FFFFFF},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            );
            glassThumbGlarePaint.setShader(glare);
            glassThumbGlarePaint.setAlpha((int) (200 * squishProgress));
            canvas.drawRect(glassThumbBounds.left, glassThumbBounds.top, glassThumbBounds.right, glassThumbBounds.top + thumbH * 0.45f, glassThumbGlarePaint);

            canvas.restore();
        }

        // 2. Rest state Frosted Pill Fill
        if (squishProgress < 0.99f) {
            float restAlpha = 1.0f - squishProgress;
            int restFillColor = isDark ? 0xF0FFFFFF : 0xFFFFFFFF;
            glassThumbPillPaint.setColor(restFillColor);
            glassThumbPillPaint.setAlpha((int) (255 * restAlpha));
            canvas.drawRoundRect(glassThumbBounds, thumbR, thumbR, glassThumbPillPaint);
        }

        // 3. Specular Hairline Rim Outline
        float halfStroke = glassThumbRimPaint.getStrokeWidth() / 2f;
        glassThumbInnerBounds.set(
                glassThumbBounds.left + halfStroke,
                glassThumbBounds.top + halfStroke,
                glassThumbBounds.right - halfStroke,
                glassThumbBounds.bottom - halfStroke
        );
        float innerR = thumbR - halfStroke;

        int strokeTop = 0xFFFFFFFF;
        int strokeBottom = isDark ? 0xD0FFFFFF : 0xF0FFFFFF;
        LinearGradient rimShader = new LinearGradient(
                cx, glassThumbBounds.top,
                cx, glassThumbBounds.bottom,
                new int[]{strokeTop, strokeBottom},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glassThumbRimPaint.setShader(rimShader);
        canvas.drawRoundRect(glassThumbInnerBounds, innerR, innerR, glassThumbRimPaint);

        canvas.restore();
    }

    protected void onTimestampUpdate(URLSpanNoUnderline link) {

    }

    private ArrayList<Pair<Float, URLSpanNoUnderline>> timestamps;
    private CharSequence lastCaption;
    private long lastVideoDuration;

    private float timestampsAppearing = 0;
    private long lastTimestampsAppearingUpdate;
    private final float TIMESTAMP_GAP = 1f;
    private static float[] tmpRadii;
    private static Path tmpPath;

    private int currentTimestamp = -1, lastTimestamp = -1;
    private StaticLayout[] timestampLabel;
    private TextPaint timestampLabelPaint;
    private float timestampChangeT = 1;
    private int timestampChangeDirection;
    private long lastTimestampUpdate;
    private float lastWidth = -1;

    public void clearTimestamps() {
        timestamps = null;
        currentTimestamp = -1;
        timestampsAppearing = 0;
        if (timestampLabel != null) {
            timestampLabel[0] = timestampLabel[1] = null;
        }
        lastCaption = null;
        lastVideoDuration = -1;
    }

    public void updateTimestamps(MessageObject messageObject, Long videoDuration) {
        if (messageObject == null) {
            clearTimestamps();
            return;
        }
        if (videoDuration == null) {
            videoDuration = (long) messageObject.getDuration() * 1000L;
        }
        if (videoDuration == null || videoDuration < 0) {
            clearTimestamps();
            return;
        }
        CharSequence text = messageObject.caption;
        if (messageObject.isYouTubeVideo()) {
            if (messageObject.youtubeDescription == null && messageObject.messageOwner.media.webpage.description != null) {
                messageObject.youtubeDescription = SpannableString.valueOf(messageObject.messageOwner.media.webpage.description);
                MessageObject.addUrlsByPattern(messageObject.isOut(), messageObject.youtubeDescription, false, 3, (int) (long) videoDuration, false);
            }
            text = messageObject.youtubeDescription;
        }
        if (text == lastCaption && lastVideoDuration == videoDuration) {
            return;
        }
        lastCaption = text;
        lastVideoDuration = videoDuration;
        if (!(text instanceof Spanned)) {
            timestamps = null;
            currentTimestamp = -1;
            timestampsAppearing = 0;
            if (timestampLabel != null) {
                timestampLabel[0] = timestampLabel[1] = null;
            }
            return;
        }
        Spanned spanned = (Spanned) text;
        URLSpanNoUnderline[] links;
        try {
            links = spanned.getSpans(0, spanned.length(), URLSpanNoUnderline.class);
        } catch (Exception e) {
            FileLog.e(e);
            timestamps = null;
            currentTimestamp = -1;
            timestampsAppearing = 0;
            if (timestampLabel != null) {
                timestampLabel[0] = timestampLabel[1] = null;
            }
            return;
        }
        timestamps = new ArrayList<>();
        timestampsAppearing = 0;
        if (timestampLabelPaint == null) {
            timestampLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            timestampLabelPaint.setTextSize(AndroidUtilities.dp(12));
            timestampLabelPaint.setColor(0xffffffff);
        }
        for (int i = 0; i < links.length; ++i) {
            try {
                URLSpanNoUnderline link = links[i];
                if (link != null && link.getURL() != null && link.label != null && link.getURL().startsWith("audio?")) {
                    Integer seconds = Utilities.parseInt(link.getURL().substring(6));
                    if (seconds != null && seconds >= 0) {
                        float position = seconds * 1000L / (float) videoDuration;
                        String label = link.label;
                        SpannableStringBuilder builder = new SpannableStringBuilder(label);
                        Emoji.replaceEmoji(builder, timestampLabelPaint.getFontMetricsInt(), false);
                        timestamps.add(new Pair<>(position, link));
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        Collections.sort(timestamps, (a, b) -> {
            if (a.first > b.first) {
                return 1;
            } else if (b.first > a.first) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    private void drawProgressBar(Canvas canvas, RectF rect, Paint paint) {
        float radius = thumbWidth / 2f;
        if (timestamps == null || timestamps.isEmpty()) {
            canvas.drawRoundRect(rect, radius, radius, paint);
        } else {
            float lineWidth = rect.bottom - rect.top;
            float left = thumbWidth / 2f;
            float right = width - thumbWidth / 2f;
            AndroidUtilities.rectTmp.set(rect);
            float halfGap = AndroidUtilities.dp(TIMESTAMP_GAP * timestampsAppearing) / 2f;
            if (tmpPath == null) {
                tmpPath = new Path();
            }
            tmpPath.reset();
            float minDur = AndroidUtilities.dp(4) / (right - left);
            int start = -1, end = -1;
            for (int i = 0; i < timestamps.size(); ++i) {
                if (timestamps.get(i).first >= minDur) {
                    start = i;
                    break;
                }
            }
            if (start < 0) {
                start = 0;
            }
            for (int i = timestamps.size() - 1; i >= 0; --i) {
                if (1f - timestamps.get(i).first >= minDur) {
                    end = i + 1;
                    break;
                }
            }
            if (end < 0) {
                end = timestamps.size();
            }
            boolean first = true;
            for (int i = start; i <= end; ++i) {
                float from = i == start ? 0 : timestamps.get(i - 1).first;
                float to = i == end ? 1 : timestamps.get(i).first;
                while (i != end && i != 0 && i < timestamps.size() - 1 && timestamps.get(i).first - from <= minDur) {
                    i++;
                    to = timestamps.get(i).first;
                }

                AndroidUtilities.rectTmp.left = AndroidUtilities.lerp(left, right, from) + (i > 0 ? halfGap : 0);
                AndroidUtilities.rectTmp.right = AndroidUtilities.lerp(left, right, to) - (i < end ? halfGap : 0);

                boolean last;
                if (last = AndroidUtilities.rectTmp.right > rect.right) {
                    AndroidUtilities.rectTmp.right = rect.right;
                }
                if (AndroidUtilities.rectTmp.right < rect.left) {
                    continue;
                }
                if (AndroidUtilities.rectTmp.left < rect.left) {
                    AndroidUtilities.rectTmp.left = rect.left;
                }

                if (tmpRadii == null) {
                    tmpRadii = new float[8];
                }
                if (i == start || last && AndroidUtilities.rectTmp.left >= rect.left) {
                    tmpRadii[0] = tmpRadii[1] = tmpRadii[6] = tmpRadii[7] = radius;
                    tmpRadii[2] = tmpRadii[3] = tmpRadii[4] = tmpRadii[5] = radius * 0.7f * timestampsAppearing;
                } else if (i >= end) {
                    tmpRadii[0] = tmpRadii[1] = tmpRadii[6] = tmpRadii[7] = radius * 0.7f * timestampsAppearing;
                    tmpRadii[2] = tmpRadii[3] = tmpRadii[4] = tmpRadii[5] = radius;
                } else {
                    tmpRadii[0] = tmpRadii[1] = tmpRadii[6] = tmpRadii[7] =
                    tmpRadii[2] = tmpRadii[3] = tmpRadii[4] = tmpRadii[5] = radius * 0.7f * timestampsAppearing;
                }
                tmpPath.addRoundRect(AndroidUtilities.rectTmp, tmpRadii, Path.Direction.CW);

                if (last) {
                    break;
                }
            }
            canvas.drawPath(tmpPath, paint);
        }
    }

    private void updateTimestampAnimation() {
        if (timestamps == null || timestamps.isEmpty()) {
            return;
        }

        float progress = (pressed ? draggingThumbX : thumbX) / (float) (width - thumbWidth);

        int timestampIndex = -1;
        for (int i = timestamps.size() - 1; i >= 0; --i) {
            if (timestamps.get(i).first - 0.001f <= progress) {
                timestampIndex = i;
                break;
            }
        }

        if (timestampLabel == null) {
            timestampLabel = new StaticLayout[2];
        }

        float left = thumbWidth / 2f;
        float right = width - thumbWidth / 2f;
        float rightPadded = right;
        float width = Math.abs(left - rightPadded) - AndroidUtilities.dp(16 + 50);

        lastWidth = width;

        if (timestampIndex != currentTimestamp) {
            if (pressed) {
                AndroidUtilities.vibrateCursor(parentView);
            }
            currentTimestamp = timestampIndex;
            if (currentTimestamp >= 0 && currentTimestamp < timestamps.size()) {
                onTimestampUpdate(timestamps.get(currentTimestamp).second);
            }
        }
        if (timestampChangeT < 1f) {
            long tx = Math.min(17, Math.abs(SystemClock.elapsedRealtime() - lastTimestampUpdate));
            float duration = timestamps.size() > 8 ? 160f : 220f;
            timestampChangeT = Math.min(timestampChangeT + tx / duration, 1);
            if (parentView != null) {
                parentView.invalidate();
            }
            lastTimestampUpdate = SystemClock.elapsedRealtime();
        }
        if (timestampsAppearing < 1f) {
            long tx = Math.min(17, Math.abs(SystemClock.elapsedRealtime() - lastTimestampUpdate));
            timestampsAppearing = Math.min(timestampsAppearing + tx / 200f, 1);
            if (parentView != null) {
                parentView.invalidate();
            }
            lastTimestampsAppearingUpdate = SystemClock.elapsedRealtime();
        }
    }
}
