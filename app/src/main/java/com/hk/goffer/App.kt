package com.hk.goffer

import android.app.Application
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by hk on 2016/8/25.
 */
class App : Application(){
    companion object{
        var instance: App by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
// 扩展一些常用的参数
val Context.today: String
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date())