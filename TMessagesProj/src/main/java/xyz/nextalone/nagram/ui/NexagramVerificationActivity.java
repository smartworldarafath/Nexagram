package xyz.nextalone.nagram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class NexagramVerificationActivity extends BaseFragment {

    private static final int REQ_FACE_CAMERA = 1001;
    private static final int REQ_FACE_GALLERY = 1002;
    private static final int REQ_FRONT_CAMERA = 1003;
    private static final int REQ_FRONT_GALLERY = 1004;
    private static final int REQ_BACK_CAMERA = 1005;
    private static final int REQ_BACK_GALLERY = 1006;

    private final Runnable onSuccessCallback;

    // Step state
    private int currentStep = 0; // 0=Intro, 1=Age, 2=Reason, 3=Details, 4=Face, 5=NID
    private LinearLayout contentContainer;
    private ScrollView scrollView;

    // Data holder
    private String selectedReason = "";
    private String customReasonText = "";
    private String phoneInput = "";
    private String nameInput = "";
    private String nidInput = "";

    private File tempCameraFile;
    private File facePhotoFile;
    private File nidFrontFile;
    private File nidBackFile;

    private ImageView facePreviewView;
    private ImageView nidFrontPreviewView;
    private ImageView nidBackPreviewView;

    public NexagramVerificationActivity(Runnable onSuccessCallback) {
        this.onSuccessCallback = onSuccessCallback;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Verification of Nexagram Bot");
        actionBar.setSubtitle("bot • 18+ Verification");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    confirmExit();
                }
            }
        });

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);

        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(24));

        scrollView.addView(contentContainer, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));
        root.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        renderStep();
        fragmentView = root;
        return fragmentView;
    }

    private void confirmExit() {
        if (currentStep > 0 && currentStep < 6) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("ভেরিফিকেশন বাতিল?");
            builder.setMessage("ভেরিফিকেশন সম্পন্ন না করলে ১৮+ কন্টেন্ট অপশন চালু করা যাবে না। আপনি কি নিশ্চিত বের হতে চান?");
            builder.setPositiveButton("বেরিয়ে যান", (d, w) -> finishFragment());
            builder.setNegativeButton("চালিয়ে যান", null);
            showDialog(builder.create());
        } else {
            finishFragment();
        }
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        confirmExit();
        return false;
    }

    private void renderStep() {
        if (contentContainer == null) return;
        contentContainer.removeAllViews();

        switch (currentStep) {
            case 0:
                buildIntroStep();
                break;
            case 1:
                buildAgeStep();
                break;
            case 2:
                buildReasonStep();
                break;
            case 3:
                buildDetailsStep();
                break;
            case 4:
                buildFaceStep();
                break;
            case 5:
                buildNidStep();
                break;
        }

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    // STEP 0: Welcome & Start
    private void buildIntroStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        ImageView botIcon = new ImageView(context);
        botIcon.setImageResource(R.drawable.msg_bot);
        botIcon.setColorFilter(Theme.getColor(Theme.key_switch2TrackChecked));
        card.addView(botIcon, LayoutHelper.createLinear(64, 64, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView title = new TextView(context);
        title.setText("১৮+ কন্টেন্ট অ্যাক্সেস ভেরিফিকেশন");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        title.setGravity(Gravity.CENTER);
        card.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        TextView desc = new TextView(context);
        desc.setText("Nexagram এ ১৮+ কন্টেন্ট চালু করার জন্য আপনার বয়স ও পরিচয় যাচাই করা বাধ্যতামূলক।\n\nআপনি যদি এটি চালু করতে চান, তবে নিচের ভেরিফিকেশনটি সঠিকভাবে সম্পন্ন করুন।");
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        desc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        desc.setGravity(Gravity.CENTER);
        desc.setLineSpacing(AndroidUtilities.dp(4), 1.0f);
        card.addView(desc, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 24));

        TextView startBtn = createPrimaryButton(context, "▶️ শুরু করুন (Start)");
        startBtn.setOnClickListener(v -> {
            currentStep = 1;
            renderStep();
        });
        card.addView(startBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 8, 0, 8));

        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // STEP 1: Age Check (Yes/No)
    private void buildAgeStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        TextView stepBadge = createStepBadge(context, "প্রশ্ন ১ / ৫");
        card.addView(stepBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView q = new TextView(context);
        q.setText("আপনার বয়স কি ১৮ বা এর বেশি?\n(Is your age 18 or older?)");
        q.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        q.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        q.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(q, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 24));

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView yesBtn = createPrimaryButton(context, "✅ হ্যাঁ (Yes)");
        yesBtn.setOnClickListener(v -> {
            currentStep = 2;
            renderStep();
        });
        btnRow.addView(yesBtn, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(48), 1.0f));

        View space = new View(context);
        btnRow.addView(space, new LinearLayout.LayoutParams(AndroidUtilities.dp(12), AndroidUtilities.dp(48)));

        TextView noBtn = createDangerButton(context, "❌ না (No)");
        noBtn.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
            b.setTitle("ভেরিফিকেশন ব্যর্থ");
            b.setMessage("আপনার বয়স ১৮ এর কম হওয়ায় আপনি ১৮+ কন্টেন্ট অপশন চালু করতে পারবেন না।");
            b.setPositiveButton("ঠিক আছে", (d, w) -> finishFragment());
            showDialog(b.create());
        });
        btnRow.addView(noBtn, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(48), 1.0f));

        card.addView(btnRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 8));
        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // STEP 2: Reason
    private void buildReasonStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        TextView stepBadge = createStepBadge(context, "প্রশ্ন ২ / ৫");
        card.addView(stepBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView q = new TextView(context);
        q.setText("আপনি এই অপশনটি কেন অন করতে চাচ্ছেন?\n(Why do you want to enable this?)");
        q.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        q.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        q.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(q, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        RadioGroup group = new RadioGroup(context);
        group.setOrientation(LinearLayout.VERTICAL);

        RadioButton r1 = new RadioButton(context);
        r1.setText("১. আমি কিছু ১৮ প্লাস কিছু তথ্যের খোঁজ নিচ্ছি");
        r1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        r1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        r1.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        group.addView(r1);

        RadioButton r2 = new RadioButton(context);
        r2.setText("২. আমি কিছু জিনিস দেখতে চাচ্ছি");
        r2.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        r2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        r2.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        group.addView(r2);

        RadioButton r3 = new RadioButton(context);
        r3.setText("৩. অন্যান্য (১৫ শব্দের মধ্যে লিখুন)");
        r3.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        r3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        r3.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        group.addView(r3);

        card.addView(group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        EditText customInput = createEditText(context, "অন্যান্য কারণ লিখুন (সর্বোচ্চ ১৫ শব্দ)...");
        customInput.setVisibility(View.GONE);
        card.addView(customInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        group.setOnCheckedChangeListener((g, checkedId) -> {
            if (r3.isChecked()) {
                customInput.setVisibility(View.VISIBLE);
            } else {
                customInput.setVisibility(View.GONE);
            }
        });

        // Restore if previously selected
        if ("১. আমি কিছু ১৮ প্লাস কিছু তথ্যের খোঁজ নিচ্ছি".equals(selectedReason)) r1.setChecked(true);
        else if ("২. আমি কিছু জিনিস দেখতে চাচ্ছি".equals(selectedReason)) r2.setChecked(true);
        else if (selectedReason.startsWith("৩.") || !customReasonText.isEmpty()) {
            r3.setChecked(true);
            customInput.setVisibility(View.VISIBLE);
            customInput.setText(customReasonText);
        }

        TextView nextBtn = createPrimaryButton(context, "পরবর্তী ধাপ (Next) ➡️");
        nextBtn.setOnClickListener(v -> {
            if (r1.isChecked()) {
                selectedReason = "১. আমি কিছু ১৮ প্লাস কিছু তথ্যের খোঁজ নিচ্ছি";
            } else if (r2.isChecked()) {
                selectedReason = "২. আমি কিছু জিনিস দেখতে চাচ্ছি";
            } else if (r3.isChecked()) {
                String text = customInput.getText().toString().trim();
                String[] words = text.split("\\s+");
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(context, "অনুগ্রহ করে আপনার কারণ লিখুন", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (words.length > 15) {
                    Toast.makeText(context, "১৫ শব্দের বেশি লেখা যাবে না (বর্তমান শব্দ সংখ্যা: " + words.length + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                customReasonText = text;
                selectedReason = "৩. অন্যান্য: " + text;
            } else {
                Toast.makeText(context, "একটি অপশন নির্বাচন করুন", Toast.LENGTH_SHORT).show();
                return;
            }
            currentStep = 3;
            renderStep();
        });
        card.addView(nextBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 8, 0, 8));

        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // STEP 3: User Details
    private void buildDetailsStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        TextView stepBadge = createStepBadge(context, "প্রশ্ন ৩ / ৫");
        card.addView(stepBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView q = new TextView(context);
        q.setText("আপনার তথ্য দিয়ে এখানে পূরণ করতে হবে:\n(Fill in your personal details)");
        q.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        q.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        q.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(q, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Field 1: Phone
        TextView lblPhone = createFieldLabel(context, "১. রেগুলার ফোন নাম্বার (দেশের কোড সহ, যেমন: +8801...):");
        card.addView(lblPhone);
        EditText phoneEdit = createEditText(context, "যেমন: +8801700000000");
        phoneEdit.setInputType(InputType.TYPE_CLASS_PHONE);
        if (TextUtils.isEmpty(phoneInput)) {
            TLRPC.User u = UserConfig.getInstance(currentAccount).getCurrentUser();
            if (u != null && !TextUtils.isEmpty(u.phone)) {
                phoneEdit.setText(u.phone.startsWith("+") ? u.phone : "+" + u.phone);
            }
        } else {
            phoneEdit.setText(phoneInput);
        }
        card.addView(phoneEdit, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 14));

        // Field 2: English Name
        TextView lblName = createFieldLabel(context, "২. আপনার সম্পূর্ণ নাম (ইংলিশে):");
        card.addView(lblName);
        EditText nameEdit = createEditText(context, "Full Name in English");
        if (!TextUtils.isEmpty(nameInput)) {
            nameEdit.setText(nameInput);
        }
        card.addView(nameEdit, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 14));

        // Field 3: NID
        TextView lblNid = createFieldLabel(context, "৩. জাতীয় পরিচয় পত্র নাম্বার (NID Number):");
        card.addView(lblNid);
        EditText nidEdit = createEditText(context, "National ID Card Number");
        nidEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (!TextUtils.isEmpty(nidInput)) {
            nidEdit.setText(nidInput);
        }
        card.addView(nidEdit, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 20));

        TextView nextBtn = createPrimaryButton(context, "পরবর্তী ধাপ (Next) ➡️");
        nextBtn.setOnClickListener(v -> {
            String p = phoneEdit.getText().toString().trim();
            String n = nameEdit.getText().toString().trim();
            String id = nidEdit.getText().toString().trim();

            if (TextUtils.isEmpty(p) || !p.startsWith("+")) {
                Toast.makeText(context, "দেশের কোড সহ ফোন নাম্বার দিন (যেমন: +880...)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(n)) {
                Toast.makeText(context, "আপনার সম্পূর্ণ নাম ইংলিশে লিখুন", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(id)) {
                Toast.makeText(context, "আপনার জাতীয় পরিচয় পত্রের নাম্বার লিখুন", Toast.LENGTH_SHORT).show();
                return;
            }

            phoneInput = p;
            nameInput = n;
            nidInput = id;

            currentStep = 4;
            renderStep();
        });
        card.addView(nextBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 8, 0, 8));

        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // STEP 4: Face Verification
    private void buildFaceStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        TextView stepBadge = createStepBadge(context, "প্রশ্ন ৪ / ৫");
        card.addView(stepBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView q = new TextView(context);
        q.setText("আপনার মুখমণ্ডল ভেরিফিকেশন করতে হবে:\n(Face Verification)");
        q.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        q.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        q.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(q, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        TextView sub = new TextView(context);
        sub.setText("নিচের স্ক্যান বাটনে ট্যাপ করে ফেইস আইডি এর মত আপনার স্পষ্ট চেহারার ছবি তুলুন।");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sub.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        card.addView(sub, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Circular Frame for Face
        FrameLayout frame = new FrameLayout(context);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(0x15000000);
        gd.setStroke(AndroidUtilities.dp(3), Theme.getColor(Theme.key_switch2TrackChecked));
        frame.setBackground(gd);

        facePreviewView = new ImageView(context);
        facePreviewView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (facePhotoFile != null && facePhotoFile.exists()) {
            Bitmap b = BitmapFactory.decodeFile(facePhotoFile.getAbsolutePath());
            if (b != null) facePreviewView.setImageBitmap(b);
        } else {
            facePreviewView.setImageResource(R.drawable.msg_fave);
            facePreviewView.setColorFilter(0x44888888);
        }
        frame.addView(facePreviewView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        card.addView(frame, LayoutHelper.createLinear(160, 160, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 16));

        // Scan button
        TextView scanBtn = createSecondaryButton(context, "📷 স্ক্যান করুন (Scan Face)");
        scanBtn.setOnClickListener(v -> showImagePickerDialog(REQ_FACE_CAMERA, REQ_FACE_GALLERY));
        card.addView(scanBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, 0, 4, 0, 12));

        // Done button
        TextView doneBtn = createPrimaryButton(context, "সম্পন্ন (Done) ➡️");
        doneBtn.setOnClickListener(v -> {
            if (facePhotoFile == null || !facePhotoFile.exists()) {
                Toast.makeText(context, "অনুগ্রহ করে প্রথমে মুখমণ্ডলের ছবি স্ক্যান করুন", Toast.LENGTH_SHORT).show();
                return;
            }
            currentStep = 5;
            renderStep();
        });
        card.addView(doneBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 4, 0, 8));

        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    // STEP 5: National ID Photos & Submit
    private void buildNidStep() {
        Context context = getParentActivity();
        LinearLayout card = createCard(context);

        TextView stepBadge = createStepBadge(context, "প্রশ্ন ৫ / ৫");
        card.addView(stepBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        TextView q = new TextView(context);
        q.setText("আপনার জাতীয় পরিচয় পত্রের ছবি:\n(National ID Photos)");
        q.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        q.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        q.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        card.addView(q, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // 1. Front Side
        TextView lblFront = createFieldLabel(context, "১. পরিচয় পত্রের সামনের দিক (NID Front Side):");
        card.addView(lblFront);

        nidFrontPreviewView = createPreviewBox(context, nidFrontFile);
        card.addView(nidFrontPreviewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 120, 0, 6, 0, 8));

        TextView uploadFrontBtn = createSecondaryButton(context, "📄 সামনের ছবি আপলোড করুন");
        uploadFrontBtn.setOnClickListener(v -> showImagePickerDialog(REQ_FRONT_CAMERA, REQ_FRONT_GALLERY));
        card.addView(uploadFrontBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 0, 0, 0, 16));

        // 2. Back Side
        TextView lblBack = createFieldLabel(context, "২. পরিচয় পত্রের পিছনের দিক (NID Back Side):");
        card.addView(lblBack);

        nidBackPreviewView = createPreviewBox(context, nidBackFile);
        card.addView(nidBackPreviewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 120, 0, 6, 0, 8));

        TextView uploadBackBtn = createSecondaryButton(context, "📄 পিছনের ছবি আপলোড করুন");
        uploadBackBtn.setOnClickListener(v -> showImagePickerDialog(REQ_BACK_CAMERA, REQ_BACK_GALLERY));
        card.addView(uploadBackBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 0, 0, 0, 24));

        // Submit Button
        TextView submitBtn = createPrimaryButton(context, "🚀 ভেরিফিকেশন জমা দিন (Submit)");
        submitBtn.setOnClickListener(v -> doSubmitVerification());
        card.addView(submitBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 0, 8, 0, 8));

        contentContainer.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void doSubmitVerification() {
        Context context = getParentActivity();
        if (nidFrontFile == null || !nidFrontFile.exists()) {
            Toast.makeText(context, "পরিচয় পত্রের সামনের দিকের ছবি প্রদান করুন", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nidBackFile == null || !nidBackFile.exists()) {
            Toast.makeText(context, "পরিচয় পত্রের পিছনের দিকের ছবি প্রদান করুন", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog progressDialog = new AlertDialog(context, 3);
        progressDialog.setMessage("যাচাই করা হচ্ছে...\nবটে তথ্য ও ছবি পাঠানো হচ্ছে...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        long tgUserId = UserConfig.getInstance(currentAccount).getClientUserId();
        TLRPC.User u = UserConfig.getInstance(currentAccount).getCurrentUser();
        String tgUsername = u != null ? u.username : "";
        String tgFullName = u != null ? (u.first_name + " " + (u.last_name != null ? u.last_name : "")).trim() : "Nexagram User";

        NexagramVerificationManager.sendVerificationData(
                currentAccount,
                tgUserId,
                tgUsername,
                tgFullName,
                phoneInput,
                nameInput,
                nidInput,
                selectedReason,
                facePhotoFile,
                nidFrontFile,
                nidBackFile,
                new NexagramVerificationManager.VerificationCallback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
                        b.setTitle("🎉 ভেরিফিকেশন সফল!");
                        b.setMessage("আপনার তথ্য সফলভাবে যাচাই করা হয়েছে। ১৮+ কন্টেন্ট অপশনটি চালু করা হয়েছে।");
                        b.setPositiveButton("সম্পন্ন (Done)", (d, w) -> {
                            if (onSuccessCallback != null) {
                                onSuccessCallback.run();
                            }
                            finishFragment();
                        });
                        showDialog(b.create());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressDialog.dismiss();
                        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
                        b.setTitle("ভেরিফিকেশন ত্রুটি");
                        b.setMessage("তথ্য পাঠাতে সমস্যা হয়েছে:\n" + errorMessage + "\n\nদয়া করে ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় চেষ্টা করুন।");
                        b.setPositiveButton("পুনরায় চেষ্টা করুন", null);
                        showDialog(b.create());
                    }
                }
        );
    }

    // Helper to pick image via Camera or Gallery
    private void showImagePickerDialog(int cameraCode, int galleryCode) {
        Context context = getParentActivity();
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("ছবি যুক্ত করুন");
        b.setItems(new CharSequence[]{"📷 ক্যামেরা দিয়ে ছবি তুলুন", "🖼️ গ্যালারি থেকে নির্বাচন করুন"}, (dialog, which) -> {
            if (which == 0) {
                launchCamera(cameraCode);
            } else {
                launchGallery(galleryCode);
            }
        });
        b.setNegativeButton("বাতিল", null);
        showDialog(b.create());
    }

    private void launchCamera(int requestCode) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File cacheDir = ApplicationLoader.applicationContext.getCacheDir();
            tempCameraFile = new File(cacheDir, "camera_snap_" + System.currentTimeMillis() + ".jpg");

            Uri photoURI = FileProvider.getUriForFile(
                    ApplicationLoader.applicationContext,
                    ApplicationLoader.applicationContext.getPackageName() + ".provider",
                    tempCameraFile
            );

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(takePictureIntent, requestCode);
        } catch (Exception e) {
            FileLog.e(e);
            Toast.makeText(getParentActivity(), "ক্যামেরা খুলতে সমস্যা হয়েছে: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            FileLog.e(e);
            Toast.makeText(getParentActivity(), "গ্যালারি খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        try {
            File dest = null;
            if (requestCode == REQ_FACE_CAMERA) {
                facePhotoFile = compressImageFile(tempCameraFile, "face_verified.jpg");
                if (facePreviewView != null && facePhotoFile != null) {
                    facePreviewView.setImageBitmap(BitmapFactory.decodeFile(facePhotoFile.getAbsolutePath()));
                }
            } else if (requestCode == REQ_FACE_GALLERY && data != null && data.getData() != null) {
                facePhotoFile = copyAndCompressUri(data.getData(), "face_verified.jpg");
                if (facePreviewView != null && facePhotoFile != null) {
                    facePreviewView.setImageBitmap(BitmapFactory.decodeFile(facePhotoFile.getAbsolutePath()));
                }
            } else if (requestCode == REQ_FRONT_CAMERA) {
                nidFrontFile = compressImageFile(tempCameraFile, "nid_front.jpg");
                if (nidFrontPreviewView != null && nidFrontFile != null) {
                    nidFrontPreviewView.setImageBitmap(BitmapFactory.decodeFile(nidFrontFile.getAbsolutePath()));
                }
            } else if (requestCode == REQ_FRONT_GALLERY && data != null && data.getData() != null) {
                nidFrontFile = copyAndCompressUri(data.getData(), "nid_front.jpg");
                if (nidFrontPreviewView != null && nidFrontFile != null) {
                    nidFrontPreviewView.setImageBitmap(BitmapFactory.decodeFile(nidFrontFile.getAbsolutePath()));
                }
            } else if (requestCode == REQ_BACK_CAMERA) {
                nidBackFile = compressImageFile(tempCameraFile, "nid_back.jpg");
                if (nidBackPreviewView != null && nidBackFile != null) {
                    nidBackPreviewView.setImageBitmap(BitmapFactory.decodeFile(nidBackFile.getAbsolutePath()));
                }
            } else if (requestCode == REQ_BACK_GALLERY && data != null && data.getData() != null) {
                nidBackFile = copyAndCompressUri(data.getData(), "nid_back.jpg");
                if (nidBackPreviewView != null && nidBackFile != null) {
                    nidBackPreviewView.setImageBitmap(BitmapFactory.decodeFile(nidBackFile.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
            Toast.makeText(getParentActivity(), "ছবি প্রক্রিয়াকরণে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show();
        }
    }

    private File compressImageFile(File src, String outName) {
        if (src == null || !src.exists()) return null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(src.getAbsolutePath(), opts);

            int scale = 1;
            while (opts.outWidth / scale > 1280 || opts.outHeight / scale > 1280) {
                scale *= 2;
            }

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scale;
            Bitmap b = BitmapFactory.decodeFile(src.getAbsolutePath(), opts);

            File out = new File(ApplicationLoader.applicationContext.getCacheDir(), outName);
            FileOutputStream fos = new FileOutputStream(out);
            b.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            return out;
        } catch (Exception e) {
            FileLog.e(e);
            return src;
        }
    }

    private File copyAndCompressUri(Uri uri, String outName) {
        try {
            InputStream is = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            Bitmap b = BitmapFactory.decodeStream(is, null, opts);
            if (is != null) is.close();

            File out = new File(ApplicationLoader.applicationContext.getCacheDir(), outName);
            FileOutputStream fos = new FileOutputStream(out);
            if (b != null) {
                b.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }
            fos.flush();
            fos.close();
            return out;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    // UI Helpers
    private LinearLayout createCard(Context context) {
        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20));

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        gd.setCornerRadius(AndroidUtilities.dp(16));
        l.setBackground(gd);
        return l;
    }

    private TextView createStepBadge(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        tv.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(3), AndroidUtilities.dp(8), AndroidUtilities.dp(3));

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Theme.getColor(Theme.key_switch2TrackChecked));
        gd.setCornerRadius(AndroidUtilities.dp(8));
        tv.setBackground(gd);
        return tv;
    }

    private TextView createFieldLabel(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        return tv;
    }

    private EditText createEditText(Context context, String hint) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        et.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        et.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        et.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12));

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        gd.setCornerRadius(AndroidUtilities.dp(8));
        gd.setStroke(AndroidUtilities.dp(1), 0x22888888);
        et.setBackground(gd);
        return et;
    }

    private ImageView createPreviewBox(Context context, File file) {
        ImageView iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0x11000000);
        gd.setCornerRadius(AndroidUtilities.dp(8));
        gd.setStroke(AndroidUtilities.dp(1.5f), 0x33888888, AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        iv.setBackground(gd);

        if (file != null && file.exists()) {
            Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (b != null) iv.setImageBitmap(b);
        } else {
            iv.setImageResource(R.drawable.msg_media);
            iv.setColorFilter(0x44888888);
        }
        return iv;
    }

    private TextView createPrimaryButton(Context context, String text) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        btn.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Theme.getColor(Theme.key_switch2TrackChecked));
        gd.setCornerRadius(AndroidUtilities.dp(12));
        btn.setBackground(gd);
        return btn;
    }

    private TextView createSecondaryButton(Context context, String text) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btn.setTextColor(Theme.getColor(Theme.key_switch2TrackChecked));
        btn.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        btn.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0x18000000);
        gd.setCornerRadius(AndroidUtilities.dp(10));
        gd.setStroke(AndroidUtilities.dp(1), Theme.getColor(Theme.key_switch2TrackChecked));
        btn.setBackground(gd);
        return btn;
    }

    private TextView createDangerButton(Context context, String text) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        btn.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFFE53935);
        gd.setCornerRadius(AndroidUtilities.dp(12));
        btn.setBackground(gd);
        return btn;
    }
}
