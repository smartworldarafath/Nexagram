package tw.nekomimi.nekogram.helpers.remote;

import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.nekomimi.nekogram.NekoXConfig;
import xyz.nextalone.nagram.ui.NexagramUpdateActivity;

public class UpdateHelper extends BaseRemoteHelper {
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/smartworldarafath/Nexagram/releases";

    private static final class InstanceHolder {
        private static final UpdateHelper instance = new UpdateHelper();
    }

    public static UpdateHelper getInstance() {
        return InstanceHolder.instance;
    }

    private boolean updateAlways = false;

    @Override
    protected void onError(String text, Delegate delegate) {
        if (delegate != null) {
            delegate.onTLResponse(null, text);
        }
    }

    @Override
    protected String getTag() {
        return "nexagram_update";
    }

    @Override
    public void load() {
        // Disabled: do not query nagram_remote_metadata channel
    }

    @Override
    public void load(Delegate delegate) {
        checkNewVersionAvailable(delegate, false);
    }

    public void checkNewVersionAvailable(Delegate delegate) {
        checkNewVersionAvailable(delegate, false);
    }

    public void checkNewVersionAvailable(Delegate delegate, boolean updateAlways_) {
        updateAlways = updateAlways_;

        new Thread(() -> {
            try {
                URL url = new URL(GITHUB_RELEASES_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Nexagram-Android");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONArray releases = new JSONArray(sb.toString());
                    for (int rIdx = 0; rIdx < releases.length(); rIdx++) {
                        JSONObject release = releases.getJSONObject(rIdx);
                        if (release.optBoolean("draft", false)) continue;

                        String tagName = release.optString("tag_name", "").trim();
                        String releaseName = release.optString("name", "").trim();
                        String releaseNotes = release.optString("body", "Nexagram update available with performance improvements and bug fixes.");
                        JSONArray assets = release.optJSONArray("assets");
                        String apkUrl = null;

                        if (assets != null) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.optString("name", "");
                                if (name.endsWith(".apk")) {
                                    apkUrl = asset.optString("browser_download_url", null);
                                    if (name.contains("arm64-v8a")) {
                                        break; // prioritize 64-bit build
                                    }
                                }
                            }
                        }

                        if (apkUrl == null) continue;

                        String cleanRemoteVer = NexagramUpdateActivity.getCleanVersionString(tagName, releaseName);

                        if (updateAlways || NexagramUpdateActivity.isNewerVersion(cleanRemoteVer, BuildConfig.VERSION_NAME)) {
                            final TLRPC.TL_help_appUpdate appUpdate = new TLRPC.TL_help_appUpdate();
                            appUpdate.version = cleanRemoteVer;
                            appUpdate.can_not_skip = false;
                            appUpdate.url = apkUrl;
                            appUpdate.text = releaseNotes;
                            appUpdate.flags = 4; // url flag

                            AndroidUtilities.runOnUIThread(() -> {
                                if (delegate != null) {
                                    delegate.onTLResponse(appUpdate, null);
                                }
                            });
                            return;
                        }
                        // Stop immediately after checking the newest available release with an APK
                        break;
                    }
                }
                AndroidUtilities.runOnUIThread(() -> {
                    if (delegate != null) {
                        delegate.onTLResponse(null, null);
                    }
                });
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    if (delegate != null) {
                        delegate.onTLResponse(null, e.getMessage());
                    }
                });
            }
        }).start();
    }

    public static class Update {
        public Boolean canNotSkip;
        public String version;
        public Integer versionCode;
        public Long timeStamp;
        public Integer sticker;
        public Integer message;
        public Map<String, Integer> gcm;
        public String url;

        public Update(Boolean canNotSkip, String version, int versionCode, long timeStamp, int sticker, int message, Map<String, Integer> gcm, String url) {
            this.canNotSkip = canNotSkip;
            this.version = version;
            this.versionCode = versionCode;
            this.timeStamp = timeStamp;
            this.sticker = sticker;
            this.message = message;
            this.gcm = gcm;
            this.url = url;
        }
    }
}
