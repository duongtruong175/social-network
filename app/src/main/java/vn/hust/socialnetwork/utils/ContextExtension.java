package vn.hust.socialnetwork.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import vn.hust.socialnetwork.R;

public class ContextExtension {

    /**
     * Create a full screen dialog with progress and text loading
     *
     * @return Return a progress dialog
     */
    public static Dialog createProgressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_progress, null));
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    /**
     * Hide keyboard
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        currentFocus.clearFocus();
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Show keyboard
     */
    public static void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    // view is a edit text
    public static void showKeyboard(View view) {
        if (!view.hasFocus()) {
            view.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    /**
     * Copy text to clipboard
     */
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.copy_to_clip_board), text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Get a ReactPopup
     *
     * @return
     */
    public static ReactionPopup createReactPostPopup(Context context) {
        String[] textReact = {
                context.getString(R.string.like),
                context.getString(R.string.heart),
                context.getString(R.string.haha),
                context.getString(R.string.wow),
                context.getString(R.string.sad),
                context.getString(R.string.angry)
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(new int[]{
                        R.drawable.ic_like,
                        R.drawable.ic_heart,
                        R.drawable.ic_haha,
                        R.drawable.ic_wow,
                        R.drawable.ic_sad,
                        R.drawable.ic_angry
                })
                .withReactionTexts(index -> textReact[index])
                .withReactionSize(context.getResources().getDimensionPixelSize(R.dimen.react_item_size))
                .withHorizontalMargin(context.getResources().getDimensionPixelSize(R.dimen.margin_horizontal_react_item))
                .withVerticalMargin(context.getResources().getDimensionPixelSize(R.dimen.margin_vertical_react_item))
                .withPopupMargin(context.getResources().getDimensionPixelSize(R.dimen.margin_between_react_item))
                .withPopupColor(ContextCompat.getColor(context, R.color.color_background_popup_react))
                .withPopupAlpha(255)
                .withTextBackground(ContextCompat.getDrawable(context, R.drawable.bg_react_text_popup))
                .withTextColor(ContextCompat.getColor(context, R.color.white))
                .withTextSize(16f)
                .build();

        return new ReactionPopup(context, config, pos -> {
            return true;
        });
    }

    /**
     * Convert a view to a image and save to file
     *
     * @return file
     */
    public static File getImageFromLayout(View view) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();
        if (background != null) {
            background.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);

        File file = FileExtension.getPathScreenPhoto(view.getContext());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file;
    }

    /**
     * Check app is running background or not
     */
    public static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
