package com.hk.goffer.utils

import android.content.Context
import org.jetbrains.anko.displayMetrics

/**
 * Created by hk on 2016/8/30.
 */
object WindowUtil {

    fun getWinHeight(ctx: Context):Int{
        return ctx.displayMetrics.heightPixels
    }

    fun getWinWidth(ctx: Context): Int{
        return ctx.displayMetrics.widthPixels
    }
}