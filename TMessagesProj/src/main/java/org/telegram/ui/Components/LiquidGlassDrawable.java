package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * LiquidGlassDrawable
 * Apple iOS Frosted Liquid Glass Material
 * Clean, flat, elegant frosted glass matching iOS system controls.
 */
public class LiquidGlassDrawable extends Drawable {

    private float cornerRadiusDp = -1; // -1 means capsule (height / 2)
    private int tintColor = 0;
    private boolean isCustomTint = false;
    private boolean useCapsule = true;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF boundsF = new RectF();
    private final RectF strokeRectF = new RectF();
    private final RectF glareRectF = new RectF();
    private final Path clipPath = new Path();

    private LinearGradient strokeShader;
    private LinearGradient fillShader;
    private LinearGradient glareShader;

    // Press animation
    private boolean isPressed = false;
    private float pressScale = 1.0f;
    private float targetScale = 1.0f;
    private long lastAnimTime = 0;

    private int alpha = 255;

    public LiquidGlassDrawable() {
        this(-1, 0);
    }

    public LiquidGlassDrawable(float cornerRadiusDp) {
        this(cornerRadiusDp, 0);
    }

    public LiquidGlassDrawable(float cornerRadiusDp, int tintColor) {
        this.cornerRadiusDp = cornerRadiusDp;
        this.tintColor = tintColor;
        this.isCustomTint = (tintColor != 0);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpf2(1.0f));

        glarePaint.setStyle(Paint.Style.FILL);
    }

    public void setCornerRadius(float radiusDp) {
        if (this.cornerRadiusDp != radiusDp) {
            this.cornerRadiusDp = radiusDp;
            this.useCapsule = (radiusDp < 0);
            updateShaders();
            invalidateSelf();
        }
    }

    public void setTintColor(int color) {
        this.tintColor = color;
        this.isCustomTint = (color != 0);
        updateShaders();
        invalidateSelf();
    }

    public void setUseCapsule(boolean useCapsule) {
        this.useCapsule = useCapsule;
        invalidateSelf();
    }

    public void setHasShadow(boolean hasShadow) {
        // No 3D shadows per user specification
    }

    public void setSpecularIntensity(float intensity) {
        updateShaders();
        invalidateSelf();
    }

    public void setPressed(boolean pressed) {
        if (this.isPressed != pressed) {
            this.isPressed = pressed;
            this.targetScale = pressed ? 0.96f : 1.0f;
            lastAnimTime = SystemClock.uptimeMillis();
            invalidateSelf();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateShaders();
    }

    private void updateShaders() {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;

        float w = bounds.width();
        float h = bounds.height();
        float l = bounds.left;
        float t = bounds.top;
        float r = bounds.right;
        float b = bounds.bottom;

        boolean isDark = Theme.isCurrentThemeDark();

        // 1. Frosted Glass Fill: clean translucent glass
        int baseTop;
        int baseBottom;

        if (isCustomTint) {
            if (isDark) {
                baseTop = ColorUtils.setAlphaComponent(tintColor, 0x55);
                baseBottom = ColorUtils.setAlphaComponent(tintColor, 0x80);
            } else {
                baseTop = ColorUtils.setAlphaComponent(tintColor, 0x65);
                baseBottom = ColorUtils.setAlphaComponent(tintColor, 0x95);
            }
        } else {
            // Neutral Apple-style frosted liquid glass
            if (isDark) {
                baseTop = 0x30FFFFFF;
                baseBottom = 0x1EFFFFFF;
            } else {
                baseTop = 0xEEFFFFFF;
                baseBottom = 0xC8FFFFFF;
            }
        }

        fillShader = new LinearGradient(
                l, t, l, b,
                new int[]{baseTop, baseBottom},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(fillShader);

        // 2. Refined hairline border highlight
        int strokeTop = isDark ? 0x40FFFFFF : 0x60FFFFFF;
        int strokeBottom = isDark ? 0x18FFFFFF : 0x20000000;

        strokeShader = new LinearGradient(
                l, t, l, b,
                new int[]{strokeTop, strokeBottom},
                new float[]{0.0f, 1.0f},
                Shader.TileMode.CLAMP
        );
        strokePaint.setShader(strokeShader);

        // 3. Subtle top glare reflection
        int glareStart = isDark ? 0x20FFFFFF : 0x30FFFFFF;
        int glareEnd = 0x00FFFFFF;
        glareShader = new LinearGradient(
                l, t, l, t + h * 0.35f,
                new int[]{glareStart, glareEnd},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glarePaint.setShader(glareShader);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;

        // Animate press scale with viscoelastic spring damping
        long now = SystemClock.uptimeMillis();
        if (lastAnimTime > 0) {
            float dt = Math.min(32, now - lastAnimTime) / 1000f;
            float diff = targetScale - pressScale;
            if (Math.abs(diff) > 0.002f) {
                float springK = isPressed ? 20f : 26f;
                pressScale += diff * Math.min(1.0f, dt * springK);
                invalidateSelf();
            } else {
                pressScale = targetScale;
            }
        }
        lastAnimTime = now;

        float w = bounds.width();
        float h = bounds.height();
        float radius = (cornerRadiusDp >= 0) ? (cornerRadiusDp > 45 ? cornerRadiusDp : dpf2(cornerRadiusDp)) : (h / 2f);
        if (useCapsule && cornerRadiusDp < 0) {
            radius = h / 2f;
        }
        radius = Math.min(radius, h / 2f);

        canvas.save();

        // Viscoelastic scaling from center on press
        if (pressScale != 1.0f) {
            float cx = bounds.exactCenterX();
            float cy = bounds.exactCenterY();
            canvas.scale(pressScale, pressScale, cx, cy);
        }

        boundsF.set(bounds);

        // 1. Translucent frosted glass core
        if (fillShader == null) updateShaders();
        fillPaint.setAlpha(alpha);
        canvas.drawRoundRect(boundsF, radius, radius, fillPaint);

        // 2. Subtle top glare
        clipPath.reset();
        clipPath.addRoundRect(boundsF, radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);

        glareRectF.set(bounds.left, bounds.top, bounds.right, bounds.top + h * 0.35f);
        glarePaint.setAlpha(alpha);
        canvas.drawRect(glareRectF, glarePaint);
        canvas.restore();

        // 3. Crisp hairline border outline
        float halfStroke = strokePaint.getStrokeWidth() / 2f;
        strokeRectF.set(
                bounds.left + halfStroke,
                bounds.top + halfStroke,
                bounds.right - halfStroke,
                bounds.bottom - halfStroke
        );
        strokePaint.setAlpha(alpha);
        canvas.drawRoundRect(strokeRectF, radius - halfStroke, radius - halfStroke, strokePaint);

        canvas.restore();
    }

    /**
     * Static helper for directly rendering Apple Frosted Liquid Glass onto any canvas bounds.
     */
    public static void drawGlass(Canvas canvas, RectF bounds, float radius, int tintColor, boolean pressed, float alphaFactor) {
        if (bounds == null || bounds.isEmpty()) return;
        float w = bounds.width();
        float h = bounds.height();
        float l = bounds.left;
        float t = bounds.top;
        float r = bounds.right;
        float b = bounds.bottom;

        boolean isDark = Theme.isCurrentThemeDark();
        float rad = Math.min(radius, h / 2f);

        // 1. Translucent frosted glass fill
        Paint fPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int baseTop, baseBottom;
        if (tintColor != 0) {
            if (isDark) {
                baseTop = ColorUtils.setAlphaComponent(tintColor, 0x55);
                baseBottom = ColorUtils.setAlphaComponent(tintColor, 0x80);
            } else {
                baseTop = ColorUtils.setAlphaComponent(tintColor, 0x65);
                baseBottom = ColorUtils.setAlphaComponent(tintColor, 0x95);
            }
        } else {
            if (isDark) {
                baseTop = 0x30FFFFFF;
                baseBottom = 0x1EFFFFFF;
            } else {
                baseTop = 0xEEFFFFFF;
                baseBottom = 0xC8FFFFFF;
            }
        }
        fPaint.setShader(new LinearGradient(l, t, l, b, new int[]{baseTop, baseBottom}, new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        fPaint.setAlpha((int) (255 * alphaFactor));
        canvas.drawRoundRect(bounds, rad, rad, fPaint);

        // 2. Subtle top glare
        Path cPath = new Path();
        cPath.addRoundRect(bounds, rad, rad, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(cPath);
        Paint gPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int glareStart = isDark ? 0x20FFFFFF : 0x30FFFFFF;
        gPaint.setShader(new LinearGradient(l, t, l, t + h * 0.35f, new int[]{glareStart, 0x00FFFFFF}, new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        gPaint.setAlpha((int) (200 * alphaFactor));
        canvas.drawRect(l, t, r, t + h * 0.35f, gPaint);
        canvas.restore();

        // 3. Clean hairline border outline
        Paint stPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stPaint.setStyle(Paint.Style.STROKE);
        stPaint.setStrokeWidth(dpf2(1.0f));
        int strokeTop = isDark ? 0x40FFFFFF : 0x60FFFFFF;
        int strokeBottom = isDark ? 0x18FFFFFF : 0x20000000;
        stPaint.setShader(new LinearGradient(l, t, l, b, new int[]{strokeTop, strokeBottom}, new float[]{0.0f, 1.0f}, Shader.TileMode.CLAMP));
        stPaint.setAlpha((int) (255 * alphaFactor));
        float halfStroke = stPaint.getStrokeWidth() / 2f;
        canvas.drawRoundRect(l + halfStroke, t + halfStroke, r - halfStroke, b - halfStroke, rad - halfStroke, rad - halfStroke, stPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
