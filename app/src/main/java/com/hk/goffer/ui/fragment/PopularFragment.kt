package com.hk.goffer.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.AppConfig
import com.hk.goffer.R
import com.hk.goffer.db.AlbumCRDU
import com.hk.goffer.db.FavData
import com.hk.goffer.db.HistoryCRUD
import com.hk.goffer.db.HistoryData
import com.hk.goffer.models.Offer
import com.hk.goffer.ui.activity.OfferDetailActivity
import com.hk.goffer.utils.CommonUtil
import com.hk.goffer.utils.OfferCache
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by hk on 2016/8/21.
 *
 * 此文件中包含三种
 * 1. PopularFragment本身的设置
 * 2. 相对应的adapter
 * 3. adapter相对应的ViewHolder
 */


/**
 * 关于缓存的一些代码
 */
//var diskCache: DiskLruCache by Delegates.notNull()
//var mCache: LruCache<String, List<Offer>> by Delegates.notNull()  // 顶级
//
//
//fun getFromMCache(key: String): List<Offer>? {
//
//    return mCache[key]
//}
//
//fun addToMCache(key: String, value: List<Offer>?) {
//    if (mCache[key] == null) {
//        mCache.put(key, value)
//    }
//}
//
//fun writeToDCache(key: String, obj: List<Offer>?) {
//    // 降低对象按JSON字符串进行存储
//    val editor = diskCache.edit(key)
//    val jsonObj = Gson().toJson(obj)
//    editor.newOutputStream(0).write(jsonObj.toByteArray(Charsets.UTF_8))
//    editor.commit() // 除了commit，还必须将diskCache进行flush，当然为了防止频繁flush，将flush放在onPause中进行
//}
//
//fun readFromDCache(key: String): List<Offer>? {
//    val snap = diskCache.get(key)
//    val inputStream = snap.getInputStream(0)
//    val buf = ByteArray(1024)
//    var len = inputStream.read(buf)
//    val rs = StringBuffer()
//    while (len > 0) {
//        rs.append(String(buf, 0, len))
//        len = inputStream.read(buf)
//    }
//    val jsonStr = rs.toString()
//    return Gson().fromJson(jsonStr, object : TypeToken<List<Offer>>() {}.type)
//}

/**
 * 主Fragment代码
 */
class PopularFragment : BaseFragment() {

    val CACHE_NAME = "offers_popular" // 数据缓存的“键值”

    override fun onPause() {
        super.onPause()
        OfferCache.diskCache.flush()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_popular, null, false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipe = view?.find<SwipeRefreshLayout>(R.id.refresh_popular)
        val lvPopular = view?.find<ListView>(R.id.list_popular)

        // 设置下拉刷新条
        swipe?.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)
        swipe?.setProgressBackgroundColorSchemeColor(Color.CYAN)
        swipe?.setProgressViewOffset(true, 0, dip(25))  // 设置了便宜量才会响应一开始加载就出现
        swipe?.onRefresh {
            toast("开始刷新了")
        }

        obtainData(lvPopular,swipe)

    }

    /**
     * 从缓存或者网络中，获取数据
     */
    fun obtainData(lvPopular: ListView?,swipe: SwipeRefreshLayout?){
        // 检测网络状态，取出缓存，或者拉取远程数据
        if (!CommonUtil.checkNetState()) {
            doAsync {
                var offers = OfferCache.getFromMCache(CACHE_NAME)
                if (offers == null || offers.size == 0) {
                    offers = OfferCache.readFromDCache(CACHE_NAME)
                    OfferCache.addToMCache("offers",offers) // 从硬盘读出后优先放入内存缓存中
                }
                val popularList = offers?.toMutableList()

                uiThread {
                    if (popularList != null && popularList.size != 0) {
                        lvPopular?.adapter = PopularAdapter(ctx, popularList)
                        toast("当前网络未连通，仅显示缓存数据！")
                    }
                }
            }
            return  // 如果没有网络则直接返回，不执行下面的
        }

        swipe?.isRefreshing = true
        doAsync() {
            val jsonStr = URL(AppConfig.popularUrl).readText()
            val type = object : TypeToken<MutableList<Offer>>() {}.type
            val popularList = Gson().fromJson<MutableList<Offer>>(jsonStr, type)  //注意这里fromJson也提供了泛型

            ctx.runOnUiThread {
                lvPopular?.addFooterView(UI {  // 1. 添加底部加载按钮 2.在KitKat版本之前，添加Header和FooterView的时候必须在adapter设置之前
                    button("加载更多") {
                        backgroundColor = R.attr.selectableItemBackground
                        onClick {
                            v ->
                            toast("点击事件响应")
                        }
                    }
                }.view)
                lvPopular?.adapter = PopularAdapter(ctx, popularList)
                swipe?.isRefreshing = false

                // 缓存的写入，仍然要在子线程中
                doAsync {
                    // 将网络数据进行缓存,按从网络获取的数据列表进行缓存！
                    OfferCache.addToMCache(CACHE_NAME, popularList)
                    OfferCache.writeToDCache(CACHE_NAME, popularList)
                }
            }
        }
    }


}

// 适配器
class PopularAdapter() : BaseAdapter() {
    lateinit private var mList: MutableList<Offer>
    private var inflater: LayoutInflater? = null
    private var ctx: Context by Delegates.notNull()

    constructor(ctx: Context, list: MutableList<Offer>) : this() {
        mList = list
        inflater = LayoutInflater.from(ctx)
        this.ctx = ctx
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: PopularViewHolder
        if (convertView == null) {
            view = inflater?.inflate(R.layout.card_offer, null, false)!!
            holder = PopularViewHolder(view, ctx)
            view.tag = holder
        } else {
            holder = convertView.tag as PopularViewHolder
            view = convertView
        }

        holder.setHolderByOffer(mList[position])
        return view
    }

    override fun getItem(position: Int): Any {
        return mList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return if (mList.isEmpty()) 0 else mList.size
    }
}

// ViewHolder
class PopularViewHolder() {
    lateinit var enterprise: TextView
    lateinit var datetime: TextView
    lateinit var loc: TextView
    lateinit var pv: TextView
    lateinit var btnDetail: Button
    lateinit var btnOrder: Button
    lateinit var btnShare: Button

    lateinit var ctx: Context

    constructor(view: View, ctx: Context) : this() {
        enterprise = view.find<TextView>(R.id.tv_offer_title)
        datetime = view.find<TextView>(R.id.tv_offer_datetime)
        loc = view.find<TextView>(R.id.tv_offer_loc)
        btnDetail = view.find<Button>(R.id.btn_detail)
        btnOrder = view.find<Button>(R.id.btn_collect)
        btnShare = view.find<Button>(R.id.btn_share)
        pv = view.find<TextView>(R.id.tv_offer_pv)
        this.ctx = ctx
    }

    fun setHolderByOffer(offer: Offer) {
        enterprise.text = offer.enterprise
        datetime.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(offer.datetime)
        loc.text = offer.addr
        pv.text = offer.pv.toString()

        btnDetail.onClick {
            v ->
            // 加入数据库，以后浏览历史从数据库中取出来
            HistoryCRUD.addSync(HistoryData(
                    -999, System.currentTimeMillis(), offer._id,
                    offer.enterprise, SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(offer.datetime),
                    offer.addr, offer.detailUrl)
            )

            ctx.startActivity<OfferDetailActivity>("detailUrl" to offer.detailUrl)

            // 后台更新 pv值
            doAsync {
                val requestUrl = AppConfig.pvUrl+"${offer._id}"
                URL(requestUrl).readText(Charsets.UTF_8) // 调用read后请求才会被发送

                ctx.runOnUiThread{
                    pv.text = (Integer.parseInt(offer.pv.toString()) +1 ).toString()
                }
            }

        }
        btnShare.onClick {
            v ->
            val content = """分享一个招聘信息：${enterprise.text}
时间:${datetime.text}
地点:${loc.text}
具体链接:${offer.detailUrl}
一起去吧！！"""
            ctx.share(content, "招聘会信息")
        }

        btnOrder.onClick {
            chooseAlbum(offer)
        }
    }

    // 添加条目到计划表中
    private fun chooseAlbum(offer: Offer) {
        var dropList: Spinner? = null
        AlbumCRDU.instance.queryAll {
            albums ->
            val albumNames = albums.map { it.name }
            val nameToId = mutableMapOf<String, Long>() // 生成name到id的映射，因为表中name是唯一的，id也是自增的
            albums.forEach {
                nameToId[it.name] = it._id
            }

            ctx.alert("点击下拉列表选择计划表", "计划表选择") {
                customView(
                        ctx.UI {
                            dropList = spinner {
                                adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, albumNames)
                            }
                        }.view
                )
                negativeButton("取消")
                positiveButton("确定", {
                    val albumName = dropList?.selectedItem
                    if (albumName == null) {
                        dismiss()
                        AlbumCRDU.instance.addUnique("默认计划表") {
                            albumId ->
                            if (albumId != -1) {
                                addFavIntoAlbum(albumId.toLong(), offer)
                            } else {
                                ctx.toast("默认计划表已经存在，请手动建立新的计划表")
                            }
                        }
                    } else {
                        val albumId = nameToId[albumName.toString()] // 获取外键
                        addFavIntoAlbum(albumId?.toLong()!!, offer)
                    }
                })
            }.show()
        }
    }

    fun addFavIntoAlbum(albumId: Long, offer: Offer) {
        val rs = AlbumCRDU.instance.putOfferIntoAlbumSync(FavData(offer._id, offer.enterprise,
                SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(offer.datetime), offer.addr, offer.school,
                offer.detailUrl, offer.pv.toLong(), albumId))

        if (rs.equals("")) {
            ctx.toast("成功加入")
        } else {
            ctx.longToast("该信息已存在于【$rs】 中,请勿将同一信息添加到多个计划表中")
        }
    }
}


