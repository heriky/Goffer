package com.hk.goffer.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.hk.goffer.App
import org.jetbrains.anko.db.*

/**
 * Created by hk on 2016/8/25.
 */

// 定义表字段和结构, 匿名对象不能单独使用，单例可以单独使用，以下定义的是单例
object FavsTable{
    val TABLE_NAME = "favs"
    val ID = "_id"
    val SCHOOL = "school"
    val ENTERPRISE = "enterprise"
    val DATETIME = "datetime"
    val ADDRESS = "addr"
    val URL = "detailUrl"
    val PV = "pv"
    val ALBUM_ID = "album_id"
}

object AlbumTable{
    val TABLE_NAME = "albums"
    val ID = "_id"
    val NAME = "name"
    val CREATE_TIME = "create_time"
    val SIZE = "album_size"
}

object HistoryTable {
    val TABLE_NAME = "browsers"
    val ID = "id"
    val OFFER_ID = "_id"
    val RECENT_BROWSE = "recent_browse" // 最近浏览时间,存储为时间戳，便于比较
    val ENTERPRISE = "enterprise"
    val DATETIME = "datetime"
    val ADDRESS = "addr"
    val URL = "detailUrl"
}


// 定义数据库操作类
class FavDatabaseOpenHelper() : ManagedSQLiteOpenHelper(App.instance, FavDatabaseOpenHelper.DB_NAME,
        null, FavDatabaseOpenHelper.DB_VERSION) {
    companion object{
        val DB_NAME = "favs.db"
        val DB_VERSION = 1
        val instance = FavDatabaseOpenHelper() // 注意，经常使用这种方式防止new的方式生成对象
    }
    override fun onCreate(db: SQLiteDatabase?) {
        // 加入计划的表
        db?.createTable(FavsTable.TABLE_NAME, true,
                FavsTable.ID to TEXT + PRIMARY_KEY,
                FavsTable.ENTERPRISE to TEXT,
                FavsTable.DATETIME to TEXT,
                FavsTable.ADDRESS to TEXT,
                FavsTable.SCHOOL to TEXT,
                FavsTable.URL to TEXT,
                FavsTable.PV to INTEGER,
                FavsTable.ALBUM_ID to INTEGER)

        // 专辑列表
        db?.createTable(AlbumTable.TABLE_NAME,true,
                AlbumTable.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                AlbumTable.NAME to TEXT + NOT_NULL + UNIQUE,
                AlbumTable.CREATE_TIME to TEXT ,
                AlbumTable.SIZE to INTEGER + DEFAULT("0")
        )

        // 浏览历史
        db?.createTable(HistoryTable.TABLE_NAME,true,
                HistoryTable.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT, // 抛弃_id,而使用自增的id 作为主键
                HistoryTable.RECENT_BROWSE to INTEGER,  // 最近浏览时间,使用时间戳进行存储
                HistoryTable.OFFER_ID to TEXT,
                HistoryTable.ENTERPRISE to TEXT,
                HistoryTable.DATETIME to TEXT,
                HistoryTable.ADDRESS to TEXT,
                HistoryTable.URL to TEXT
        )
        // 默认会有一个表
        db?.execSQL("insert into ${AlbumTable.TABLE_NAME} (${AlbumTable.NAME},${AlbumTable.CREATE_TIME},${AlbumTable.SIZE})" +
                "values (?,?,?)", arrayOf("默认计划表","2016",'0'))
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable(AlbumTable.TABLE_NAME)
        db?.dropTable(FavsTable.TABLE_NAME)
        onCreate(db)

    }
}

// 向Context中注入database对象，扩展属性
val Context.database : FavDatabaseOpenHelper
    get()= FavDatabaseOpenHelper.instance



