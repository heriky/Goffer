package com.hk.goffer.utils

import android.net.NetworkInfo
import android.os.Environment
import com.hk.goffer.App
import org.jetbrains.anko.connectivityManager
import java.io.File
import java.security.MessageDigest

/**
 * Created by hk on 2016/8/29.
 */
object CommonUtil {
    val ctx = App.instance

    /**
     * 获取版本号
     */
    fun appVersion():Int{
        val pm = ctx.packageManager
        return pm.getPackageInfo(ctx.packageName,0).versionCode
    }

    /**
     * 获取缓存目录,有sd时用外部缓存，没有时用内部缓存,返回目录的File对象
     */
    fun getCacheDir(): File {
        val file: File
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            file = ctx.externalCacheDir
        }else{
            file = ctx.cacheDir
        }
        return file
    }

    fun md5Encode(src: String): String {
        val sb = StringBuilder()
        try {
            val digest = MessageDigest.getInstance("MD5")
            val buf = digest.digest(src.toByteArray())
            //针对每一个字节16进制化
            for (b in buf) {
                val tmp = Integer.toHexString(b.toInt() and 0xff) //为了保证byte正确的变成int类型
                if (tmp.length != 2) {
                    sb.append("0")
                }
                sb.append(tmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sb.toString()
    }

    fun checkNetState():Boolean{
        val info = ctx.connectivityManager.activeNetworkInfo
        if (info?.state == NetworkInfo.State.CONNECTED){
            return true
        }
        return false
    }
}