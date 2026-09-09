package xyz.nextalone.nagram.ui;

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
import android.view.View;
import android.view.ViewGroup;
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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private String downloadUrl = null;
    private String remoteVersion = null;
    private File downloadedApkFile = null;
    private boolean isDownloading = false;

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

        // Header Card (App Icon & Current Version)
        LinearLayout headerCard = createCard(context);
        headerCard.setGravity(Gravity.CENTER_HORIZONTAL);
        headerCard.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(24), AndroidUtilities.dp(20), AndroidUtilities.dp(24));

        ImageView iconView = new ImageView(context);
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

        // Update Card
        updateCardLayout = createCard(context);
        updateCardLayout.setVisibility(View.GONE);
        updateCardLayout.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18));

        newVersionTextView = new TextView(context);
        newVersionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        newVersionTextView.setTypeface(Typeface.DEFAULT_BOLD);
        newVersionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        updateCardLayout.addView(newVersionTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        changelogTextView = new TextView(context);
        changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        changelogTextView.setLineSpacing(AndroidUtilities.dp(2), 1f);
        updateCardLayout.addView(changelogTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        contentLayout.addView(updateCardLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Download & Analog Speedometer Card
        speedCardLayout = createCard(context);
        speedCardLayout.setVisibility(View.GONE);
        speedCardLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        speedCardLayout.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(20));

        TextView speedMeterTitle = new TextView(context);
        speedMeterTitle.setText("Live Download Speed");
        speedMeterTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        speedMeterTitle.setTypeface(Typeface.DEFAULT_BOLD);
        speedMeterTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        speedCardLayout.addView(speedMeterTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 8));

        speedometerView = new SpeedometerView(context);
        speedCardLayout.addView(speedometerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        downloadProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        downloadProgressBar.setMax(100);
        downloadProgressBar.setProgress(0);
        downloadProgressBar.setIndeterminate(false);
        speedCardLayout.addView(downloadProgressBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8, 0, 0, 0, 8));

        progressTextView = new TextView(context);
        progressTextView.setText("Downloading 0%");
        progressTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        progressTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        speedCardLayout.addView(progressTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        contentLayout.addView(speedCardLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Action Button
        actionButton = new TextView(context);
        actionButton.setText("Check Again");
        actionButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        actionButton.setTypeface(Typeface.DEFAULT_BOLD);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setTextColor(0xFFFFFFFF);
        actionButton.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Theme.getColor(Theme.key_featuredStickers_addButton) != 0 ? Theme.getColor(Theme.key_featuredStickers_addButton) : 0xFF2EA6FF);
        btnBg.setCornerRadius(AndroidUtilities.dp(12));
        actionButton.setBackground(btnBg);

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

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(16));
        card.setBackground(bg);
        return card;
    }

    private void checkUpdates() {
        statusTextView.setText("Checking for updates...");
        actionButton.setEnabled(false);
        actionButton.setAlpha(0.6f);

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
                    if (releases.length() > 0) {
                        JSONObject latest = releases.getJSONObject(0);
                        String tagName = latest.optString("tag_name", "").replace("v", "").trim();
                        String releaseNotes = latest.optString("body", "Bug fixes and improvements.");
                        JSONArray assets = latest.optJSONArray("assets");
                        String apkUrl = null;
                        long apkSize = 0;

                        if (assets != null) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.optString("name", "");
                                if (name.endsWith(".apk")) {
                                    apkUrl = asset.optString("browser_download_url", null);
                                    apkSize = asset.optLong("size", 0);
                                    if (name.contains("arm64-v8a")) break; // prioritize user abi
                                }
                            }
                        }

                        final String foundUrl = apkUrl;
                        final String foundVersion = tagName;
                        final String foundNotes = releaseNotes;
                        final long foundSize = apkSize;

                        AndroidUtilities.runOnUIThread(() -> {
                            actionButton.setEnabled(true);
                            actionButton.setAlpha(1.0f);

                            if (isNewerVersion(foundVersion, BuildConfig.VERSION_NAME)) {
                                downloadUrl = foundUrl;
                                remoteVersion = foundVersion;
                                statusTextView.setText("New update available!");
                                statusTextView.setTextColor(0xFF4CAF50);

                                updateCardLayout.setVisibility(View.VISIBLE);
                                newVersionTextView.setText("Version " + foundVersion + (foundSize > 0 ? " (" + String.format(java.util.Locale.US, "%.1f MB", foundSize / (1024f * 1024f)) + ")" : ""));
                                changelogTextView.setText(foundNotes);

                                actionButton.setText("Download & Install Update");
                            } else {
                                statusTextView.setText("You are using the latest version!");
                                statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                                updateCardLayout.setVisibility(View.GONE);
                                actionButton.setText("Check Again");
                            }
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            AndroidUtilities.runOnUIThread(() -> {
                actionButton.setEnabled(true);
                actionButton.setAlpha(1.0f);
                statusTextView.setText("Your app is up to date.");
                statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                actionButton.setText("Check Again");
            });
        }).start();
    }

    private boolean isNewerVersion(String remote, String current) {
        if (TextUtils.isEmpty(remote)) return false;
        try {
            String[] rParts = remote.split("\\.");
            String[] cParts = current.split("\\.");
            int max = Math.max(rParts.length, cParts.length);
            for (int i = 0; i < max; i++) {
                int r = i < rParts.length ? Integer.parseInt(rParts[i].replaceAll("\\D+", "")) : 0;
                int c = i < cParts.length ? Integer.parseInt(cParts[i].replaceAll("\\D+", "")) : 0;
                if (r > c) return true;
                if (r < c) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void startDownload(String downloadUrlStr) {
        isDownloading = true;
        speedCardLayout.setVisibility(View.VISIBLE);
        speedometerView.reset();
        actionButton.setEnabled(false);
        actionButton.setText("Downloading...");
        actionButton.setAlpha(0.6f);

        new Thread(() -> {
            try {
                URL url = new URL(downloadUrlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "Nexagram-Android");
                conn.connect();

                // Handle redirect for GitHub Release CDN
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
}
