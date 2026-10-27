package xyz.nextalone.nagram.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NexagramUpdateActivity extends BaseFragment {

    private TextView statusTextView;
    private TextView versionTextView;
    private TextView newVersionTextView;
    private TextView changelogTextView;
    private TextView progressTextView;
    private ProgressBar downloadProgressBar;
    private SpeedometerView speedometerView;
    private TextView actionButton;
    private LinearLayout updateCardLayout;
    private LinearLayout speedCardLayout;
    private ImageView iconView;

    private String downloadUrl = null;
    private String remoteVersion = null;
    private File downloadedApkFile = null;
    private boolean isDownloading = false;
    private ObjectAnimator pulseAnimator = null;

    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/smartworldarafath/Nexagram/releases";

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Nexagram Update");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = root;

        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        root.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(32));
        scrollView.addView(contentLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        // 1. Header Liquid Glass Card (App Icon & Current Version)
        LinearLayout headerCard = createGlassCard(context);
        headerCard.setGravity(Gravity.CENTER_HORIZONTAL);
        headerCard.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(24), AndroidUtilities.dp(20), AndroidUtilities.dp(24));

        iconView = new ImageView(context);
        iconView.setImageResource(R.mipmap.ic_launcher);
        headerCard.addView(iconView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        TextView appTitle = new TextView(context);
        appTitle.setText("Nexagram");
        appTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        appTitle.setTypeface(Typeface.DEFAULT_BOLD);
        appTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        headerCard.addView(appTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

        versionTextView = new TextView(context);
        versionTextView.setText("Installed Version: v" + BuildConfig.VERSION_NAME);
        versionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        versionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        headerCard.addView(versionTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 8));

        statusTextView = new TextView(context);
        statusTextView.setText("Checking for updates...");
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        headerCard.addView(statusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        contentLayout.addView(headerCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 2. Update Details Liquid Glass Card
        updateCardLayout = createGlassCard(context);
        updateCardLayout.setVisibility(View.GONE);
        updateCardLayout.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20));

        // New version row with badge
        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        newVersionTextView = new TextView(context);
        newVersionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        newVersionTextView.setTypeface(Typeface.DEFAULT_BOLD);
        newVersionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleRow.addView(newVersionTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView newBadge = new TextView(context);
        newBadge.setText("NEW");
        newBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        newBadge.setTypeface(Typeface.DEFAULT_BOLD);
        newBadge.setTextColor(0xFFFFFFFF);
        newBadge.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(3), AndroidUtilities.dp(8), AndroidUtilities.dp(3));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xFF4CAF50);
        badgeBg.setCornerRadius(AndroidUtilities.dp(10));
        newBadge.setBackground(badgeBg);
        titleRow.addView(newBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 10, 0, 0, 0));

        updateCardLayout.addView(titleRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

        View divider = new View(context);
        divider.setBackgroundColor(Theme.multAlpha(Theme.getColor(Theme.key_divider), 0.5f));
        updateCardLayout.addView(divider, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 0, 0, 0, 12));

        changelogTextView = new TextView(context);
        changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        changelogTextView.setLineSpacing(AndroidUtilities.dp(2), 1f);
        updateCardLayout.addView(changelogTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        contentLayout.addView(updateCardLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 3. Download & Live Analog Speedometer Liquid Glass Card
        speedCardLayout = createGlassCard(context);
        speedCardLayout.setVisibility(View.GONE);
        speedCardLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        speedCardLayout.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(20), AndroidUtilities.dp(18), AndroidUtilities.dp(20));

        TextView speedMeterTitle = new TextView(context);
        speedMeterTitle.setText("Live Download Speed");
        speedMeterTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        speedMeterTitle.setTypeface(Typeface.DEFAULT_BOLD);
        speedMeterTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        speedCardLayout.addView(speedMeterTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 10));

        speedometerView = new SpeedometerView(context);
        speedCardLayout.addView(speedometerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 14));

        downloadProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        downloadProgressBar.setMax(100);
        downloadProgressBar.setProgress(0);
        downloadProgressBar.setIndeterminate(false);
        speedCardLayout.addView(downloadProgressBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8, 0, 0, 0, 10));

        progressTextView = new TextView(context);
        progressTextView.setText("Downloading 0%");
        progressTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        progressTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        speedCardLayout.addView(progressTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        contentLayout.addView(speedCardLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 4. Action Glass Button
        actionButton = new TextView(context);
        actionButton.setText("Check Again");
        actionButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        actionButton.setTypeface(Typeface.DEFAULT_BOLD);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setTextColor(0xFFFFFFFF);
        actionButton.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(AndroidUtilities.dp(14));
        btnBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        btnBg.setColors(new int[]{0xFF007AFF, 0xFF00C6FF});
        actionButton.setBackground(btnBg);

        actionButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
            }
            return false;
        });

        actionButton.setOnClickListener(v -> {
            if (downloadedApkFile != null && downloadedApkFile.exists()) {
                installApk(downloadedApkFile);
            } else if (downloadUrl != null && !isDownloading) {
                startDownload(downloadUrl);
            } else if (!isDownloading) {
                checkUpdates();
            }
        });

        contentLayout.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 0));

        checkUpdates();

        return fragmentView;
    }

    private LinearLayout createGlassCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        boolean isDark = Theme.isCurrentThemeDark();
        GradientDrawable bg = new GradientDrawable();
        if (isDark) {
            bg.setColor(0x281E2732);
            bg.setStroke(AndroidUtilities.dp(1), 0x22FFFFFF);
        } else {
            bg.setColor(0xD9FFFFFF);
            bg.setStroke(AndroidUtilities.dp(1), 0x18000000);
        }
        bg.setCornerRadius(AndroidUtilities.dp(20));
        card.setBackground(bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(AndroidUtilities.dp(2));
        }
        return card;
    }

    private void startIconPulse() {
        if (iconView == null) return;
        if (pulseAnimator != null) pulseAnimator.cancel();
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.08f, 1.0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.08f, 1.0f);
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(iconView, pvhX, pvhY);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
    }

    private void stopIconPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (iconView != null) {
            iconView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start();
        }
    }

    private void animateShowCard(View card) {
        if (card.getVisibility() == View.VISIBLE) return;
        card.setAlpha(0f);
        card.setTranslationY(AndroidUtilities.dp(18));
        card.setVisibility(View.VISIBLE);
        card.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void checkUpdates() {
        statusTextView.setText("Checking for updates...");
        statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        actionButton.setEnabled(false);
        actionButton.setAlpha(0.6f);
        startIconPulse();

        new Thread(() -> {
            try {
                URL url = new URL(GITHUB_RELEASES_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Nexagram-Android");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONArray releases = new JSONArray(sb.toString());
                    JSONObject targetRelease = null;
                    String targetApkUrl = null;
                    long targetApkSize = 0;

                    for (int rIdx = 0; rIdx < releases.length(); rIdx++) {
                        JSONObject release = releases.getJSONObject(rIdx);
                        if (release.optBoolean("draft", false)) continue;

                        JSONArray assets = release.optJSONArray("assets");
                        if (assets != null) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.optString("name", "");
                                if (name.endsWith(".apk")) {
                                    targetApkUrl = asset.optString("browser_download_url", null);
                                    targetApkSize = asset.optLong("size", 0);
                                    if (name.contains("arm64-v8a")) break; // prioritize 64-bit build
                                }
                            }
                        }

                        if (targetApkUrl != null) {
                            targetRelease = release;
                            break; // Stop immediately! First non-draft release with an APK is the latest available APK release
                        }
                    }

                    if (targetRelease != null) {
                        String tagName = targetRelease.optString("tag_name", "").trim();
                        String releaseName = targetRelease.optString("name", "").trim();
                        String releaseNotes = targetRelease.optString("body", "Bug fixes and performance improvements.");
                        String cleanRemoteVer = getCleanVersionString(tagName, releaseName);

                        if (isNewerVersion(cleanRemoteVer, BuildConfig.VERSION_NAME)) {
                            final String foundUrl = targetApkUrl;
                            final String foundVersion = cleanRemoteVer;
                            final String foundNotes = releaseNotes;
                            final long foundSize = targetApkSize;

                            AndroidUtilities.runOnUIThread(() -> {
                                stopIconPulse();
                                actionButton.setEnabled(true);
                                actionButton.setAlpha(1.0f);

                                downloadUrl = foundUrl;
                                remoteVersion = foundVersion;
                                statusTextView.setText("New update available!");
                                statusTextView.setTextColor(0xFF4CAF50);

                                newVersionTextView.setText("Version " + foundVersion + (foundSize > 0 ? " (" + String.format(java.util.Locale.US, "%.1f MB", foundSize / (1024f * 1024f)) + ")" : ""));
                                changelogTextView.setText(foundNotes);
                                animateShowCard(updateCardLayout);

                                actionButton.setText("Download & Install Update");
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            AndroidUtilities.runOnUIThread(() -> {
                stopIconPulse();
                actionButton.setEnabled(true);
                actionButton.setAlpha(1.0f);
                statusTextView.setText("Your app is up to date.");
                statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                updateCardLayout.setVisibility(View.GONE);
                actionButton.setText("Check Again");
            });
        }).start();
    }

    public static String getCleanVersionString(String tagName, String releaseName) {
        String ver = extractSemVerFromText(tagName);
        if (!TextUtils.isEmpty(ver)) {
            return ver;
        }
        ver = extractSemVerFromText(releaseName);
        if (!TextUtils.isEmpty(ver)) {
            return ver;
        }
        if (!TextUtils.isEmpty(tagName)) {
            return tagName.replaceAll("^[vV]", "").trim();
        }
        return "0.0.0";
    }

    private static String extractSemVerFromText(String text) {
        if (TextUtils.isEmpty(text)) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:[vV])?(\\d+\\.\\d+(?:\\.\\d+)?(?:[\\s-]*(?:beta|alpha|rc)[\\s-]*[0-9.]+)?)(?:\\b|\\s|\\))").matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    public static boolean isNewerVersion(String remote, String current) {
        if (TextUtils.isEmpty(remote) || TextUtils.isEmpty(current)) return false;
        try {
            int[] rMain = extractMainVersion(remote);
            int[] cMain = extractMainVersion(current);
            int maxMain = Math.max(rMain.length, cMain.length);
            for (int i = 0; i < maxMain; i++) {
                int r = i < rMain.length ? rMain[i] : 0;
                int c = i < cMain.length ? cMain[i] : 0;
                if (r > c) return true;
                if (r < c) return false;
            }

            int[] rBeta = extractBetaVersion(remote);
            int[] cBeta = extractBetaVersion(current);
            if (rBeta.length == 0 && cBeta.length > 0) {
                return true;
            }
            if (rBeta.length > 0 && cBeta.length == 0) {
                return false;
            }
            if (rBeta.length > 0 && cBeta.length > 0) {
                int maxBeta = Math.max(rBeta.length, cBeta.length);
                for (int i = 0; i < maxBeta; i++) {
                    int r = i < rBeta.length ? rBeta[i] : 0;
                    int c = i < cBeta.length ? cBeta[i] : 0;
                    if (r > c) return true;
                    if (r < c) return false;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    private static int[] extractMainVersion(String v) {
        if (TextUtils.isEmpty(v)) return new int[]{0, 0, 0};
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\.\\d+(?:\\.\\d+)?").matcher(v);
        if (m.find()) {
            java.util.regex.Matcher full = java.util.regex.Pattern.compile("(\\d+)\\.((\\d+))(?:\\.(\\d+))?").matcher(v);
            if (full.find()) {
                int major = Integer.parseInt(full.group(1));
                int minor = Integer.parseInt(full.group(2));
                int patch = full.group(3) != null ? Integer.parseInt(full.group(3)) : 0;
                return new int[]{major, minor, patch};
            }
        }
        return new int[]{0, 0, 0};
    }

    private static int[] extractBetaVersion(String v) {
        if (TextUtils.isEmpty(v)) return new int[0];
        String lower = v.toLowerCase();
        int betaIdx = lower.indexOf("beta");
        if (betaIdx < 0) return new int[0];
        String sub = lower.substring(betaIdx + 4).trim();
        sub = sub.replace(")", "").replace("(", "").trim();
        String[] parts = sub.split("[.\\s]+");
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (String p : parts) {
            String digits = p.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                try {
                    list.add(Integer.parseInt(digits));
                } catch (Exception ignored) {}
            }
        }
        if (list.isEmpty()) return new int[]{1};
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) res[i] = list.get(i);
        return res;
    }

    public static void checkForUpdateInBackground(Activity activity, boolean forceNotify) {
        new Thread(() -> {
            try {
                URL url = new URL(GITHUB_RELEASES_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Nexagram-Android");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONArray releases = new JSONArray(sb.toString());
                    for (int i = 0; i < releases.length(); i++) {
                        JSONObject rel = releases.getJSONObject(i);
                        if (rel.optBoolean("draft", false)) continue;

                        boolean hasApk = false;
                        JSONArray assets = rel.optJSONArray("assets");
                        if (assets != null) {
                            for (int a = 0; a < assets.length(); a++) {
                                if (assets.getJSONObject(a).optString("name", "").endsWith(".apk")) {
                                    hasApk = true;
                                    break;
                                }
                            }
                        }
                        if (!hasApk) continue;

                        String tag = rel.optString("tag_name", "").trim();
                        String name = rel.optString("name", "").trim();
                        String cleanRemoteVer = getCleanVersionString(tag, name);

                        if (isNewerVersion(cleanRemoteVer, BuildConfig.VERSION_NAME)) {
                            final String newVer = cleanRemoteVer;
                            AndroidUtilities.runOnUIThread(() -> {
                                if (activity != null && !activity.isFinishing()) {
                                    new AlertDialog.Builder(activity)
                                            .setTitle("Nexagram Update Available")
                                            .setMessage("A new version (" + newVer + ") is available. Would you like to update now?")
                                            .setPositiveButton("Update", (di, w) -> {
                                                if (LaunchActivity.instance != null) {
                                                    LaunchActivity.instance.presentFragment(new NexagramUpdateActivity());
                                                }
                                            })
                                            .setNegativeButton("Later", null)
                                            .show();
                                }
                            });
                        }
                        // Always break immediately after checking the newest available release with an APK
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void startDownload(String downloadUrlStr) {
        isDownloading = true;
        animateShowCard(speedCardLayout);
        speedometerView.reset();
        actionButton.setEnabled(false);
        actionButton.setText("Downloading...");
        actionButton.setAlpha(0.6f);
        startIconPulse();

        new Thread(() -> {
            try {
                URL url = new URL(downloadUrlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "Nexagram-Android");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.connect();
                }

                int totalLength = conn.getContentLength();
                InputStream input = conn.getInputStream();

                File cacheDir = ApplicationLoader.applicationContext.getExternalFilesDir(null);
                if (cacheDir == null) cacheDir = ApplicationLoader.applicationContext.getCacheDir();
                File outputFile = new File(cacheDir, "Nexagram-v" + remoteVersion + ".apk");
                FileOutputStream output = new FileOutputStream(outputFile);

                byte[] data = new byte[8192];
                long totalDownloaded = 0;
                int count;

                long lastTime = System.currentTimeMillis();
                long lastBytes = 0;

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                    totalDownloaded += count;

                    long now = System.currentTimeMillis();
                    if (now - lastTime >= 250) {
                        long bytesDiff = totalDownloaded - lastBytes;
                        double timeDiffSec = (now - lastTime) / 1000.0;
                        double speedMbps = (bytesDiff * 8.0) / (timeDiffSec * 1_000_000.0);

                        int progress = totalLength > 0 ? (int) ((totalDownloaded * 100) / totalLength) : 0;
                        long finalTotalDownloaded = totalDownloaded;

                        AndroidUtilities.runOnUIThread(() -> {
                            speedometerView.setSpeed((float) speedMbps);
                            downloadProgressBar.setProgress(progress);
                            progressTextView.setText(String.format(java.util.Locale.US, "Downloading: %d%% (%.1f MB / %.1f MB)",
                                    progress,
                                    finalTotalDownloaded / (1024f * 1024f),
                                    totalLength / (1024f * 1024f)));
                        });

                        lastTime = now;
                        lastBytes = totalDownloaded;
                    }
                }

                output.flush();
                output.close();
                input.close();

                downloadedApkFile = outputFile;
                isDownloading = false;

                AndroidUtilities.runOnUIThread(() -> {
                    stopIconPulse();
                    speedometerView.reset();
                    actionButton.setEnabled(true);
                    actionButton.setAlpha(1.0f);
                    actionButton.setText("Install Update");
                    progressTextView.setText("Download Complete (100%)");
                    downloadProgressBar.setProgress(100);
                    installApk(downloadedApkFile);
                });

            } catch (Exception e) {
                FileLog.e(e);
                isDownloading = false;
                AndroidUtilities.runOnUIThread(() -> {
                    stopIconPulse();
                    actionButton.setEnabled(true);
                    actionButton.setAlpha(1.0f);
                    actionButton.setText("Download Failed - Retry");
                    speedometerView.reset();
                });
            }
        }).start();
    }

    private void installApk(File apkFile) {
        if (apkFile == null || !apkFile.exists() || getParentActivity() == null) return;
        try {
            Uri apkUri = FileProvider.getUriForFile(getParentActivity(),
                    ApplicationLoader.getApplicationId() + ".provider", apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getParentActivity().startActivity(intent);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        stopIconPulse();
    }
}
