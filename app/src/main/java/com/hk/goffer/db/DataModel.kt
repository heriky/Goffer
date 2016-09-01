package com.hk.goffer.db

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hk on 2016/8/25.
 */

class FavData(var map: MutableMap<String, Any?>) {
    var _id: String by map
    var enterprise: String by map
    var datetime: String by map
    var addr: String by map
    var detailUrl: String by map
    var album_id: Long by map   // 不能是Int，因为classParser不支持int值
    var school: String by map
    var pv: Long by map

    constructor():this(HashMap()){}

    constructor(_id: String, enterprise: String, datetime: String,
                addr: String,school: String, detailUrl: String,
                pv: Long, album_id: Long) : this(HashMap()) {
        this._id = _id
        this.enterprise = enterprise
        this.datetime = datetime
        this.addr = addr
        this.detailUrl = detailUrl
        this.album_id = album_id
        this.school = school
        this.pv = pv
    }
}

/**
 *  val TABLE_NAME = "albums"
val ID = "_id"
val NAME = "name"
val CREATE_TIME = "create_time"
 */
data class AlbumData(var map: MutableMap<String, Any?>){
    var _id: Long by map
    var name: String  by map
    var create_time: String by map
    var album_size : Long by map
    constructor():this(HashMap()){}
    constructor(_id: Long, name: String, create_time:String,album_size: Long):this(HashMap()){
        this._id = _id
        this.name = name
        this.create_time = create_time
        this.album_size = album_size
    }
    constructor(name:String):this(0,name,SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(Date()),0){

    }

}

data class HistoryData(var id: Long, var recent_browse:Long, var _id: String,var enterprise: String, var datetime:String,var addr:String,var detailUrl: String)

