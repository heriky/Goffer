package com.hk.goffer.db

import android.content.ContentResolver
import android.net.Uri
import com.hk.goffer.App
import org.jetbrains.anko.db.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hk on 2016/8/25.
 */

val db = FavDatabaseOpenHelper.instance
val resolver: ContentResolver? = App.instance.contentResolver  // 通用触发器
val uriPrefix = "content://com.hk.goffer"


class FavCRUD() {
    companion object {
        val instance by lazy {
            FavCRUD()
        }
    }

    fun add(fav: FavData, cb: () -> Unit) {
        doAsync {
            val result = db.use {  // lambda的返回值就是result的值
                insert(FavsTable.TABLE_NAME,
                        FavsTable.ID to fav._id,
                        FavsTable.ALBUM_ID to fav.album_id,
                        FavsTable.PV to fav.pv,
                        FavsTable.URL to fav.detailUrl,
                        FavsTable.ADDRESS to fav.addr,
                        FavsTable.ENTERPRISE to fav.enterprise,
                        FavsTable.DATETIME to fav.datetime,
                        FavsTable.SCHOOL to fav.school)
            }
            // result 有值吗？？？
            uiThread {
                cb()  //  回调
            }
        }
    }

    fun addSync(fav: FavData) {
        val result = db.use {  // lambda的返回值就是result的值
            insert(FavsTable.TABLE_NAME,
                    FavsTable.ID to fav._id,
                    FavsTable.ALBUM_ID to fav.album_id,
                    FavsTable.PV to fav.pv,
                    FavsTable.URL to fav.detailUrl,
                    FavsTable.ADDRESS to fav.addr,
                    FavsTable.ENTERPRISE to fav.enterprise,
                    FavsTable.DATETIME to fav.datetime,
                    FavsTable.SCHOOL to fav.school)
        }
    }

    fun del(id: String, cb: () -> Unit) {
        doAsync {
            val rs = db.use {
                delete(FavsTable.TABLE_NAME, "_id={id}",
                        "id" to id)
            }
            uiThread {
                cb()
            }
        }
    }

    fun delByAlbumSync(albumId: Long) {
        db.use {
            val rs = db.use {
                delete(FavsTable.TABLE_NAME, "${FavsTable.ALBUM_ID}={albumId}", "albumId" to albumId)
            }
        }
    }


    fun queryById(id: String, cb: (FavData?) -> Unit) {
        doAsync {
            val rs: FavData? = db.use {
                select(FavsTable.TABLE_NAME)
                        .where("_id={id}", "id" to id)
                        .parseOpt(classParser<FavData>())
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun queryByIdSync(id: String): FavData? {
        val rs: FavData? = db.use {
            select(FavsTable.TABLE_NAME)
                    .where("_id={id}", "id" to id)
                    .parseOpt(classParser<FavData>())
        }
        return rs
    }

    fun queryByAlbum(albumId: Long, cb: (List<FavData>) -> Unit) {
        doAsync {
            val rs = db.use {
                select(FavsTable.TABLE_NAME)
                        .where("${FavsTable.ALBUM_ID}={albumId}", "albumId" to albumId)
                        .parseList(classParser<FavData>())
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun queryByAlbumSync(albumId: Long): List<FavData> {
        return db.use {
            val rs = select(FavsTable.TABLE_NAME)
                    .where("${FavsTable.ALBUM_ID}={albumId}", "albumId" to albumId)
                    .parseList(classParser<FavData>())
            print(rs[0].addr)
            rs
        }
    }

}

class AlbumCRDU() {
    val ACTION_UPDATE = "$uriPrefix/album/insert"

    companion object {
        val instance by lazy {
            AlbumCRDU()
        }
    }

    fun add(name: String, cb: () -> Unit) {
        doAsync {
            val rs = db.use {
                insert(AlbumTable.TABLE_NAME,
                        AlbumTable.NAME to name,
                        AlbumTable.CREATE_TIME to SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date()),
                        AlbumTable.SIZE to 0)
            }
            uiThread {
                cb()
            }
        }
    }

    fun addUnique(name: String, cb: (rs: Int) -> Unit) {
        doAsync {

            val rs = db.use {
                val isExist = select(AlbumTable.TABLE_NAME)
                        .where("${AlbumTable.NAME}={name}", "name" to name)
                        .parseOpt(classParser<AlbumData>())
                if (isExist != null) {  // 专辑名字必须是唯一的，如果有重名，则返回-1
                    -1
                } else {
                    insert(AlbumTable.TABLE_NAME,
                            AlbumTable.NAME to name,
                            AlbumTable.CREATE_TIME to SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date()),
                            AlbumTable.SIZE to 0).toInt()
                }
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun addUniqueSync(name: String): Int {
        val rs = db.use {
            val isExist = select(AlbumTable.TABLE_NAME)
                    .where("${AlbumTable.NAME}={name}", "name" to name)
                    .parseOpt(classParser<AlbumData>())
            if (isExist != null) {  // 专辑名字必须是唯一的，如果有重名，则返回-1
                -1
            } else {
                insert(AlbumTable.TABLE_NAME,
                        AlbumTable.NAME to name,
                        AlbumTable.CREATE_TIME to SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date()),
                        AlbumTable.SIZE to 0).toInt()
            }
        }
        return rs
    }

    /**
     * 删除专辑。 这个有级联的关系，删除专辑的同时也要将album中相关的fav删除掉
     */
    fun delById(albumId: Long, cb: () -> Unit) {
        doAsync {
            val rs = db.use {
                transaction {
                    delete(AlbumTable.TABLE_NAME, "${AlbumTable.ID}={id}", "id" to albumId)
                    FavCRUD.instance.delByAlbumSync(albumId)
                }
            }
            uiThread {
                cb()
            }
        }
    }

    /**
     * 这个方法非常重要，用于将条目加入到相应专辑中，要将条目数据插入到FAV表中，还要更新ALBUM表中的album中的size字段，
     * 并且两个操作是一个整体，这里需要爱一个事务来处理.
     *
     * 返回值为当前条目所已加入的专辑name
     */
    fun putOfferIntoAlbumSync(fav: FavData): String {
        return db.use {
            // 1.一个条目对应一个album，一个album对应多个条目，这里用的是一对多的关系
            // 2. 开启事务来做这件事情
            val existFav = FavCRUD.instance.queryByIdSync(fav._id)
            if (existFav == null) {
                transaction {
                    FavCRUD.instance.addSync(fav)
                    execSQL("update ${AlbumTable.TABLE_NAME} set ${AlbumTable.SIZE}=${AlbumTable.SIZE}+1 where ${AlbumTable.ID}='${fav.album_id}'")

                    resolver?.notifyChange(Uri.parse(ACTION_UPDATE), null) // 通知专辑，有变化
                }
                ""
            } else {
                queryByIdSync(existFav.album_id.toInt())?.name!!
            }
        }
    }

    fun queryById(id: Int, cb: (AlbumData?) -> Unit) {
        doAsync {
            val rs: AlbumData? = db.use {
                select(AlbumTable.TABLE_NAME)
                        .where("_id={id}", "id" to id)
                        .parseOpt(classParser<AlbumData>())
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun queryByIdSync(id: Int): AlbumData? {
        val rs: AlbumData? = db.use {
            select(AlbumTable.TABLE_NAME)
                    .where("_id={id}", "id" to id)
                    .parseOpt(classParser<AlbumData>())
        }
        return rs
    }

    fun queryByName(name: String, cb: (AlbumData?) -> Unit) {
        doAsync {
            val rs: AlbumData? = db.use {
                select(AlbumTable.TABLE_NAME)
                        .where("${AlbumTable.NAME}={name}", "name" to name)
                        .parseOpt(classParser<AlbumData>())
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun queryAll(cb: (List<AlbumData>) -> Unit) {
        doAsync {
            val rs: List<AlbumData> = db.use {
                select(AlbumTable.TABLE_NAME)
                        .parseList(classParser<AlbumData>())
            }
            uiThread {
                cb(rs)
            }
        }
    }

    fun queryAllSync(): List<AlbumData> {
        val rs: List<AlbumData> = db.use {
            select(AlbumTable.TABLE_NAME)
                    .parseList(classParser<AlbumData>())
        }
        return rs
    }

}

object HistoryCRUD {
    val ACTION_ADD = "$uriPrefix/history/change"

    fun addSync(history: HistoryData) {
        val result = db.use {  // lambda的返回值就是result的值
            val isExist = select(HistoryTable.TABLE_NAME)
                    .where("${HistoryTable.OFFER_ID}='${history._id}'").parseOpt(classParser<HistoryData>())
            if (isExist != null) {
                updateSync(history._id, System.currentTimeMillis())
            } else {
                transaction {
                    insert(HistoryTable.TABLE_NAME, // 主键id是自增的,便于删除最后一行
                            HistoryTable.RECENT_BROWSE to history.recent_browse,
                            HistoryTable.OFFER_ID to history._id,
                            HistoryTable.ENTERPRISE to history.enterprise,
                            HistoryTable.ADDRESS to history.addr,
                            HistoryTable.DATETIME to history.datetime,
                            HistoryTable.URL to history.detailUrl
                    )

                    val count: Int = select(HistoryTable.TABLE_NAME, "count(*)").parseSingle(IntParser)

                    if (count > 100) {
                        delFirstRowSync()
                    }
                }
            }

            resolver?.notifyChange(Uri.parse(ACTION_ADD), null)
        }
    }

    /**
     * 删除id值最小的一个，即最先加入的，这个遵循先进先出的原则，历史记录超过10个则进行出队操作
     */
    fun delFirstRowSync() {
        db.use {
            execSQL("delete from ${HistoryTable.TABLE_NAME} where id=(select min(id) from ${HistoryTable.TABLE_NAME})")
        }
    }

    fun delByIdSync(id: Long) {
        db.use {
            delete(HistoryTable.TABLE_NAME, "id='{id}'", "id" to id)
        }
    }
    fun delById(id:Long, cb: ()->Unit){
        doAsync {
            db.use {
                delete(HistoryTable.TABLE_NAME, "id='{id}'", "id" to id)
            }
            uiThread {
                cb()
            }
        }
    }

    fun removeAllSync() {
        db.use {
            execSQL("delete from ${HistoryTable.TABLE_NAME}")
        }
    }
    fun removeAll(cb: () -> Unit){
        doAsync {
            db.use {
                execSQL("delete from ${HistoryTable.TABLE_NAME}")
            }
            uiThread {
                cb()
            }
        }
    }

    /**
     * 主要是对浏览时间进行更新,在插入时，如果已经存在，则调用此更新
     */
    fun updateSync(offer_id: String, newTime: Long) {
        db.use {
            update(HistoryTable.TABLE_NAME, HistoryTable.RECENT_BROWSE to newTime)
                    .where("${HistoryTable.OFFER_ID}={id}", "id" to offer_id)
                    .exec()
        }
    }

    /**
     * 查询所有
     */
    fun queryAllSync(): List<HistoryData> {
        return db.use {
            select(HistoryTable.TABLE_NAME)
                    .parseList(classParser<HistoryData>())
        }
    }

    fun queryAll(cb: (List<HistoryData>) -> Unit) {
        doAsync {
            val rs = db.use {
                select(HistoryTable.TABLE_NAME)
                        .parseList(classParser<HistoryData>())
            }

            uiThread {
                cb(rs)
            }
        }
    }
}
