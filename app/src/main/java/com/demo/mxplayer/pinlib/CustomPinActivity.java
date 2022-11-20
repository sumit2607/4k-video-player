package com.demo.mxplayer.pinlib;

import android.content.Intent;
import android.util.Log;

import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.pinlib.managers.AppLockActivity;

/**
 * Created by oliviergoutay on 1/14/15.
 */
public class CustomPinActivity extends AppLockActivity {

    @Override
    public void showForgotDialog() {
//        Resources res = getResources();
//        // Create the builder with required paramaters - Context, Title, Positive Text
//        CustomDialog.Builder builder = new CustomDialog.Builder(this,
//                res.getString(R.string.activity_dialog_title),
//                res.getString(R.string.activity_dialog_accept));
//        builder.content(res.getString(R.string.activity_dialog_content));
//        builder.negativeText(res.getString(R.string.activity_dialog_decline));
//
//        //Set theme
//        builder.darkTheme(false);
//        builder.typeface(Typeface.SANS_SERIF);
//        builder.positiveColor(res.getColor(R.color.light_blue_500)); // int res, or int colorRes parameter versions available as well.
//        builder.negativeColor(res.getColor(R.color.light_blue_500));
//        builder.rightToLeft(false); // Enables right to left positioning for languages that may require so.
//        builder.titleAlignment(BaseDialog.Alignment.CENTER);
//        builder.buttonAlignment(BaseDialog.Alignment.CENTER);
//        builder.setButtonStacking(false);
//
//        //Set text sizes
//        builder.titleTextSize((int) res.getDimension(R.dimen.activity_dialog_title_size));
//        builder.contentTextSize((int) res.getDimension(R.dimen.activity_dialog_content_size));
//        builder.positiveButtonTextSize((int) res.getDimension(R.dimen.activity_dialog_positive_button_size));
//        builder.negativeButtonTextSize((int) res.getDimension(R.dimen.activity_dialog_negative_button_size));
//
//        //Build the dialog.
//        CustomDialog customDialog = builder.build();
//        customDialog.setCanceledOnTouchOutside(false);
//        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        customDialog.setClickListener(new CustomDialog.ClickListener() {
//            @Override
//            public void onConfirmClick() {
//                Toast.makeText(getApplicationContext(), "Yes", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelClick() {
//                Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Show the dialog.
//        customDialog.show();
    }
    public void onBackPressed() {
        startActivity(new Intent(CustomPinActivity.this, MainActivity.class));
        finish();
        super.onBackPressed();
    }
    @Override
    public void onPinFailure(int attempts) {

    }

    public final void onPinSuccess(int attempts) {
        Log.e(TAG, "onPinSuccess() called with: attempts = [" + attempts + "]");
        setResult(-1, getIntent());
    }

    @Override
    public int getPinLength() {
        return super.getPinLength();//you can override this method to change the pin length from the default 4
    }
}
