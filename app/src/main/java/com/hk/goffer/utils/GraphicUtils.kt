package com.hk.goffer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.hk.goffer.App
import org.jetbrains.anko.dip
import java.util.*

/**
 * Created by hk on 2016/8/28.
 */

object GraphicUtils {

    /**
     * 画一个圆形
     * str 为内部文字
     * radius 为原型半径
     * textSize为文字尺寸
      */
    fun drawCircle(str: String, radius: Int, textSize: Int): Bitmap {
        val height = 2 * App.instance.dip(radius)  //画布的宽高
        val width = height

        val bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.isDither = true

        // 绘制背景
        val rand = Random()
        val r = rand.nextInt(100) + 100
        val g = rand.nextInt(100) + 100
        val b = rand.nextInt(100) + 100
        paint.color = Color.rgb(r, g, b)
        canvas.drawCircle(width/2f, height/2f, width/2f, paint)

        // 绘制文字
        paint.color = Color.WHITE
        paint.textSize = App.instance.dip(textSize).toFloat() // 34dp文字
        val textWidth = paint.measureText(str)
        val fm = paint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        canvas.drawText(str, width/2 - textWidth / 2, height/2 + textHeight / 2 - fm.descent, paint)

        return bitmap
    }

    fun drawRect(str: String, dp: Int, textSize: Int): Bitmap{
        val height = App.instance.dip(dp)
        val width = height
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.isDither = true
        val rand = Random()
        val r = rand.nextInt(100) + 100
        val g = rand.nextInt(100) + 100
        val b = rand.nextInt(100) + 100
        paint.color = Color.rgb(r, g, b)

        canvas.drawRect(0f,0f,width/1f,height/1f,paint)

        // 绘制文字
        paint.color = Color.WHITE
        paint.textSize = App.instance.dip(textSize).toFloat() // 34dp文字
        val textWidth = paint.measureText(str)
        val fm = paint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        canvas.drawText(str, width/2 - textWidth / 2, height/2 + textHeight / 2 - fm.descent, paint)

        return bitmap
    }

//    fun gaussBlur(bitmap: Bitmap){
//        val rs = RenderScript.create(App.instance)
//        //ScriptIntrinsicBlur.create(rs, Element.RGB_888(rs))
//    }

    /**
     * 获取Drawable类型的圆形
     */
    fun getCircleDrawable(ctx: Context,str: String, radius: Int, textSize: Int):Drawable{
        val bitmap =  drawCircle(str,radius,textSize)
        return BitmapDrawable(ctx.resources,bitmap)
    }
}
