package xyz.nextalone.nagram.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NexagramAppUpdateManager {

    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/smartworldarafath/Nexagram/releases";

    public static boolean hasUpdate = false;
    public static String newVersion = null;
    public static String downloadUrl = null;
    public static long updateSize = 0;
    public static String releaseNotes = null;
    private static boolean isChecking = false;

    public static boolean isNexagramUpdateAvailable() {
        return hasUpdate && downloadUrl != null;
    }

    public static void checkUpdates(boolean force, Runnable callback) {
        if (isChecking) return;
        isChecking = true;

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
                    String newestUrl = null;
                    String newestVersion = null;
                    String newestNotes = null;
                    long newestSize = 0;

                    for (int rIdx = 0; rIdx < releases.length(); rIdx++) {
                        JSONObject rel = releases.optJSONObject(rIdx);
                        if (rel == null) continue;
                        String tagName = rel.optString("tag_name", "").replace("v", "").trim();
                        if (!isStrictlyNewerThanBase(tagName) || !isNewerVersion(tagName, BuildConfig.VERSION_NAME)) {
                            continue;
                        }

                        JSONArray assets = rel.optJSONArray("assets");
                        String apkUrl = null;
                        long apkSize = 0;
                        if (assets != null) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.optString("name", "");
                                if (name.endsWith(".apk")) {
                                    apkUrl = asset.optString("browser_download_url", null);
                                    apkSize = asset.optLong("size", 0);
                                    if (name.contains("arm64-v8a")) break;
                                }
                            }
                        }

                        if (apkUrl != null) {
                            newestUrl = apkUrl;
                            newestVersion = tagName;
                            newestNotes = rel.optString("body", "Bug fixes and performance improvements.");
                            newestSize = apkSize;
                            break;
                        }
                    }

                    if (newestUrl != null && newestVersion != null) {
                        hasUpdate = true;
                        downloadUrl = newestUrl;
                        newVersion = newestVersion;
                        releaseNotes = newestNotes;
                        updateSize = newestSize;

                        AndroidUtilities.runOnUIThread(() -> {
                            isChecking = false;
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                            if (callback != null) callback.run();
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            AndroidUtilities.runOnUIThread(() -> {
                isChecking = false;
                if (callback != null) callback.run();
            });
        }).start();
    }

    public static void openUpdate(Activity activity) {
        if (activity instanceof LaunchActivity) {
            ((LaunchActivity) activity).presentFragment(new NexagramUpdateActivity());
        }
    }

    private static boolean isStrictlyNewerThanBase(String remote) {
        if (TextUtils.isEmpty(remote)) return false;
        try {
            String clean = remote.toLowerCase().replace("v", "").trim();
            String[] parts = clean.split("[.\\-_]");
            int major = 0, minor = 0, patch = 0;
            if (parts.length > 0) {
                String s = parts[0].replaceAll("\\D+", "");
                if (!s.isEmpty()) major = Integer.parseInt(s);
            }
            if (parts.length > 1) {
                String s = parts[1].replaceAll("\\D+", "");
                if (!s.isEmpty()) minor = Integer.parseInt(s);
            }
            if (parts.length > 2) {
                String s = parts[2].replaceAll("\\D+", "");
                if (!s.isEmpty()) patch = Integer.parseInt(s);
            }

            if (major > 2) return true;
            if (major == 2) {
                if (minor > 0) return true;
                if (minor == 0 && patch >= 5) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isNewerVersion(String remote, String current) {
        if (TextUtils.isEmpty(remote)) return false;
        if (TextUtils.isEmpty(current)) return true;

        try {
            String cleanRemote = remote.toLowerCase().replace("v", "").trim();
            String cleanCurrent = current.toLowerCase().replace("v", "").trim();

            String[] rParts = cleanRemote.split("[.\\-_]");
            String[] cParts = cleanCurrent.split("[.\\-_]");

            int length = Math.max(rParts.length, cParts.length);
            for (int i = 0; i < length; i++) {
                int rVal = 0, cVal = 0;
                if (i < rParts.length) {
                    String numStr = rParts[i].replaceAll("\\D+", "");
                    if (!numStr.isEmpty()) rVal = Integer.parseInt(numStr);
                }
                if (i < cParts.length) {
                    String numStr = cParts[i].replaceAll("\\D+", "");
                    if (!numStr.isEmpty()) cVal = Integer.parseInt(numStr);
                }
                if (rVal > cVal) return true;
                if (rVal < cVal) return false;
            }

            if (cleanRemote.contains("beta") && cleanCurrent.contains("beta")) {
                int rBeta = extractBetaNumber(cleanRemote);
                int cBeta = extractBetaNumber(cleanCurrent);
                return rBeta > cBeta;
            } else if (!cleanRemote.contains("beta") && cleanCurrent.contains("beta")) {
                return true;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    private static int extractBetaNumber(String s) {
        try {
            int idx = s.indexOf("beta");
            if (idx != -1) {
                String sub = s.substring(idx + 4).replaceAll("\\D+", "");
                if (!sub.isEmpty()) return Integer.parseInt(sub);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
