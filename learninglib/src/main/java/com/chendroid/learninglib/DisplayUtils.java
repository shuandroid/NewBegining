package com.chendroid.learninglib;

import android.content.Context;

/**
 * @author zhaochen @ Zhihu Inc.
 * @intro
 * @since 2018/8/4
 */
public class DisplayUtils {


    public static int dpToPixel(final Context pContext, final float pDp) {
        if (pContext == null) {
            return 0;
        }

        final float density = pContext.getResources().getDisplayMetrics().density;

        return (int) ((pDp * density) + 0.5f);
    }
}
