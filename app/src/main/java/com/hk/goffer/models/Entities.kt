package com.hk.goffer.models

import android.os.Parcel
import android.os.Parcelable

import java.util.*

/**
 * Created by hk on 2016/8/23.
 */

// GsonFormat用的类
data class Offer(
        var _id:String, var school:String, var enterprise: String,
        var datetime: Date, var addr: String, var detailUrl: String,
        var pv : Int) : Parcelable {
    constructor(source: Parcel): this(source.readString(), source.readString(), source.readString(), source.readSerializable() as Date, source.readString(), source.readString(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(_id)
        dest?.writeString(school)
        dest?.writeString(enterprise)
        dest?.writeSerializable(datetime)
        dest?.writeString(addr)
        dest?.writeString(detailUrl)
        dest?.writeInt(pv)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Offer> = object : Parcelable.Creator<Offer> {
            override fun createFromParcel(source: Parcel): Offer = Offer(source)
            override fun newArray(size: Int): Array<Offer?> = arrayOfNulls(size)
        }
    }
}

/**
 * "_id":"57bbc138d1a91087e827f44d","name":"西安交通大学","pic":"http://www.baidu.com",
 * "offers":["57bbba96d1a91087e827f444","57bbbaffd1a91087e827f445","57bbbb37d1a91087e827f446"]
 */
data class School(var _id:String, var name: String, var pic:String,var offers: List<String>)
