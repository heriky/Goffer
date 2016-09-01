package com.hk.goffer.utils

import android.util.LruCache
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.models.Offer
import com.jakewharton.disklrucache.DiskLruCache

/**
 * Created by hk on 2016/8/31.
 */

/**
 * 具有针对性的，对offerList列表提供内存和硬盘双缓存
 */
object OfferCache {
    val diskCache: DiskLruCache = DiskLruCache.open(CommonUtil.getCacheDir(), CommonUtil.appVersion(), 1, 10 * 1024 * 1024) //10M空间
    val mCache = object : LruCache<String, List<Offer>>((Runtime.getRuntime().maxMemory() / 8).toInt()) {
        override fun sizeOf(key: String?, value: List<Offer>): Int {  // 规定计量单位
            return value.size
        }
    }

    fun getFromMCache(key: String): List<Offer>? {

        return mCache[key]
    }

    fun addToMCache(key: String, value: List<Offer>?) {
        if (mCache[key] == null) {
            mCache.put(key, value)
        }
    }

    fun writeToDCache(key: String, obj: List<Offer>?) {
        // 降低对象按JSON字符串进行存储
        val editor = diskCache.edit(key)
        val jsonObj = Gson().toJson(obj)
        editor.newOutputStream(0).write(jsonObj.toByteArray(Charsets.UTF_8))
        editor.commit() // 除了commit，还必须将diskCache进行flush，当然为了防止频繁flush，将flush放在onPause中进行
    }

    fun readFromDCache(key: String): List<Offer>? {
        val snap = diskCache.get(key) ?: return null
        val inputStream = snap.getInputStream(0)
        val buf = ByteArray(1024)
        var len = inputStream.read(buf)
        val rs = StringBuffer()
        while (len > 0) {
            rs.append(String(buf, 0, len))
            len = inputStream.read(buf)
        }
        val jsonStr = rs.toString()
        return Gson().fromJson(jsonStr, object : TypeToken<List<Offer>>() {}.type)
    }
}

object HistoryCache{
    val diskCache: DiskLruCache? = DiskLruCache.open(CommonUtil.getCacheDir(),CommonUtil.appVersion(),1,1024) // 历史记录缓存，开辟1K空间
    val mCache = LruCache<String,MutableList<String>>(1024)

    fun addToMCache(key: String, value: MutableList<String>){
        if (mCache[key] == null) {
            mCache.put(key,value)
        }
    }

    fun getFromMCache(key: String): MutableList<String>?{
        return mCache[key]
    }

    fun writeToDCache(key:String, value: MutableList<String>){
        // 针对key打开一个流文件
        val editor = diskCache?.edit(key)
        val jsonStr = Gson().toJson(value)
        editor?.newOutputStream(0)?.write(jsonStr.toByteArray(Charsets.UTF_8))
        editor?.commit()
    }

    fun readFromDCache(key: String): MutableList<String>? {
        val snap = OfferCache.diskCache.get(key) ?: return null
        val inputStream = snap.getInputStream(0)
        val buf = ByteArray(1024)
        var len = inputStream.read(buf)
        val rs = StringBuffer()
        while (len > 0) {
            rs.append(String(buf, 0, len))
            len = inputStream.read(buf)
        }
        val jsonStr = rs.toString()
        return Gson().fromJson(jsonStr, object : TypeToken<List<String>>() {}.type)
    }
}