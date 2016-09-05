package com.petmeds1800.util;

import com.petmeds1800.R;

import android.app.Activity;
import android.text.Spanned;
import android.view.Gravity;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by pooja on 8/27/2016.
 */
public class Utils {

    public static String changeDateFormat(long millisecond, String dateFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String dateString = formatter.format(new Date(millisecond));
        return dateString;
    }

    public static void displayCrouton(Activity activity, Spanned messageString, ViewGroup attachToView) {
        Crouton.makeText(activity,
                messageString,
                new Style.Builder()
                        .setBackgroundColor(R.color.color_snackbar)
                        .setTextAppearance(R.style.StyleCrouton)
                        .setTextColor(R.color.color_snackbar_text)
                        .setHeight(activity.getResources().getDimensionPixelSize(R.dimen.height_snackbar))
                        .setGravity(Gravity.CENTER)
                        .setTextColor(android.R.color.white)
                        .build(),
                attachToView).show();
    }

    public static void displayCrouton(Activity activity, String messageString, ViewGroup attachToView) {
        Crouton.makeText(activity,
                messageString,
                new Style.Builder()
                        .setBackgroundColor(R.color.color_snackbar)
                        .setTextAppearance(R.style.StyleCrouton)
                        .setTextColor(R.color.color_snackbar_text)
                        .setHeight(activity.getResources().getDimensionPixelSize(R.dimen.height_snackbar))
                        .setGravity(Gravity.CENTER)
                        .setTextColor(android.R.color.white)
                        .build(),
                attachToView).show();
    }
}
