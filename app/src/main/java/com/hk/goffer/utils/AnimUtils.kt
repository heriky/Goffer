package com.hk.goffer.utils

import android.content.Context
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import com.hk.goffer.App
import com.hk.goffer.R

/**
 * Created by hk on 2016/8/31.
 */
object AnimUtils {
    val ctx: Context? = App.instance.applicationContext
    fun shake(view : View){
        val anim :Animation = AnimationUtils.loadAnimation(ctx, R.anim.shake_repeat)
        view.startAnimation(anim)
    }

    fun expand(view:View){
        val sa: ScaleAnimation = ScaleAnimation(0f,1f,0f,1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        sa.duration = 300
        sa.interpolator =  AccelerateInterpolator()
        view.startAnimation(sa)
    }

    fun shrink(view: View){
        val sa: ScaleAnimation = ScaleAnimation(1f,0f,1f,0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        sa.duration = 200
        sa.interpolator =  AccelerateInterpolator()
        view.startAnimation(sa)
    }
}