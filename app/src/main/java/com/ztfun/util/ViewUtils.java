package com.ztfun.util;

import android.view.View;

public class ViewUtils {
    /**
     * Get the string name of a view's id as defined in .xml files.
     */
    public static String getResourceName(View view) {
        if (view.getId() == View.NO_ID) {
            return "no-id";
        } else {
            return view.getResources().getResourceName(view.getId());
        }
    }
}
