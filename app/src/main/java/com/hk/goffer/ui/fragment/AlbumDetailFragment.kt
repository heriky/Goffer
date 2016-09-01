package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hk.goffer.R
import com.hk.goffer.db.FavCRUD
import com.hk.goffer.db.FavData
import com.hk.goffer.ui.activity.OfferDetailActivity
import com.hk.goffer.utils.GraphicUtils
import org.jetbrains.anko.find
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.onItemClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import java.util.*

/**
 * Created by hk on 2016/8/28.
 */

class AlbumDetailFragment : BaseFragment() {
    var adapter: ArrayAdapter<FavData>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater?.inflate(R.layout.fragment_album_detail, null, false)!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //0. toolbar 设置
        val toolbar = view?.find<Toolbar>(R.id.toolbar_album_detail)
        toolbar?.setNavigationOnClickListener {
            fragmentManager.popBackStack()
        }

        // 1.获取传递的数据
        val albumId = arguments["album_id"] as Long
        val albumName = arguments["album_name"] as String
        val albumSize = arguments["album_size"] as Long
        val albumTime = arguments["create_time"] as String

        //2. 获取相应的控件
        val albumFace = view?.find<ImageView>(R.id.album_detail_face)
        val albumTitle = view?.find<TextView>(R.id.album_preview_title)
        val albumCount = view?.find<TextView>(R.id.album_preview_count)
        val albumCreate = view?.find<TextView>(R.id.album_preview_time)

        val lvDetail = view?.find<ListView>(R.id.lv_schedule_detail)

        albumFace?.imageBitmap = GraphicUtils.drawRect(albumName.substring(0, 2), 100, 34)
        albumTitle?.text = albumName
        albumCount?.text = "包含$albumSize 个记录"
        albumCreate?.text = "创建于$albumTime"

        // 3.按albumId查询所有fav，分类列出
        FavCRUD.instance.queryByAlbum(albumId) {
            favs ->
            val sortedFavs = favs.sortedBy { it.datetime }
            val type2Pos: MutableMap<String, Int> = HashMap()  //  保存“日期-起始位置”键值对
            var pos = 0
            sortedFavs.forEach {
                val datetime = it.datetime
                if (!type2Pos.containsKey(datetime)) {
                    type2Pos[datetime] = pos
                }
                pos++
            }

            lvDetail?.adapter = AlbumDetailAdapter(favs, type2Pos)
            lvDetail?.onItemClick { adapterView, view, i, l ->
                val fav = lvDetail.getItemAtPosition(i) as FavData
                ctx.startActivity<OfferDetailActivity>("detailUrl" to fav.detailUrl)
            }
        }

    }

    inner class AlbumDetailAdapter(val favs: List<FavData>, val type2Pos: MutableMap<String, Int>) : ArrayAdapter<FavData>(ctx, -1, favs) {

        val titlePos = type2Pos.values
        val inflater: LayoutInflater? = LayoutInflater.from(ctx)

        override fun getCount(): Int {
            return favs.size  // 包含标题
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View?
            val holder: AlbumDetailHolder

            if (convertView == null) {
                view = inflater?.inflate(R.layout.list_album_detail_item, null, false)
                holder = AlbumDetailHolder(view)
                view?.tag = holder
            } else {
                holder = convertView.tag as AlbumDetailHolder
                view = convertView
            }

            if (position in titlePos) {
                // 将标题和分割线都显示出来
                holder.llHeader?.visibility = View.VISIBLE
            }
            holder.setupHolderByFav(favs[position])
            return view!!
        }
    }


}

class AlbumDetailHolder {
    var tvEnterprise: TextView? = null
    var tvDatetime: TextView? = null
    var tvAddr: TextView? = null
    var favHeader: TextView? = null
    var llHeader: LinearLayout? = null

    constructor(view: View?) {
        tvEnterprise = view?.find(R.id.fav_enterprise)
        tvDatetime = view?.find(R.id.fav_datetime)
        tvAddr = view?.find(R.id.fav_addr)
        favHeader = view?.find(R.id.tv_fav_header)
        llHeader = view?.find(R.id.ll_fav_header)
    }

    fun setupHolderByFav(fav: FavData) {
        tvEnterprise?.text = fav.enterprise
        tvDatetime?.text = fav.datetime
        tvAddr?.text = fav.addr
        if (llHeader?.visibility == View.VISIBLE) {
            favHeader?.text = fav.datetime
//            favHeaderIcon?.imageBitmap = GraphicUtils.drawCircle("☆", 24, 24)
        }
    }
}

