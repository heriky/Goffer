package com.hk.goffer.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.AppConfig
import com.hk.goffer.R
import com.hk.goffer.models.Offer
import com.hk.goffer.ui.activity.SchoolsActivity
import com.hk.goffer.utils.CommonUtil
import com.hk.goffer.utils.OfferCache
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import java.net.URL
import java.net.URLEncoder

/**
 * Created by hk on 2016/8/21.
 */
class SchoolFragment : BaseFragment() {

    companion object{
        var fab: FloatingActionButton? = null
    }

    val CACHE_NAME = "offers_school"

    var header: TextView? = null
    var lvSchool: ListView? = null
    var swipe: SwipeRefreshLayout? = null

    var llHeader: LinearLayout? = null
    var emptyTip: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_schools, null, false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        header = view?.find<TextView>(R.id.tv_header_school)
        lvSchool = view?.find<ListView>(R.id.lv_offer_school)
        swipe = view?.find<SwipeRefreshLayout>(R.id.refresh_school)
        fab = view?.find<FloatingActionButton>(R.id.fab_school_picker)
        llHeader = view?.find<LinearLayout>(R.id.ll_school_header)
        emptyTip = view?.find(R.id.school_empty_tips)

        fab?.onClick {
            startActivityForResult(intentFor<SchoolsActivity>(), 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == SchoolsActivity.RESULT_CODE && data != null) {
            toast("返回的学校id为:${data.getStringExtra("school_id")}")

            // 拉取数据填充列表
            val _id = data.getStringExtra("school_id")
            val name = data.getStringExtra("school_name")
            header?.text = name

            // 按照学校的_id拉取单个学校的数据，并进行显示
            val requestUrl = AppConfig.schoolDetailUrl + "/${URLEncoder.encode(_id, "UTF-8")}"
            obtainOffers(requestUrl,lvSchool,CACHE_NAME)
        }
    }

    /**
     * 通过检测网络状态，以最合适的方式进行数据的拉取
     * 断网状态下会读取缓存，联网状态下会从服务器拉取数据
     *
     * requestUrl: String  请求数据的url
     * lvData: ListView?    展示数据的ListView
     * cacheName: String   当前数据缓存的键值
     */
    private fun obtainOffers(requestUrl: String, lvData: ListView?, cacheName:String) {

        // 1. 断网状态下，取出缓存，或者拉取远程数据
        if (!CommonUtil.checkNetState()) {
            doAsync {
                var offers = OfferCache.getFromMCache(cacheName)
                if (offers == null || offers.size == 0) {
                    offers = OfferCache.readFromDCache(cacheName)
                    OfferCache.addToMCache(cacheName,offers) // 从硬盘读出后优先放入内存缓存中
                }
                val popularList = offers?.toMutableList()

                uiThread {
                    if (popularList != null && popularList.size != 0) {
                        lvData?.adapter = PopularAdapter(ctx, popularList)
                        toast("当前网络未连通，仅显示缓存数据！")
                    }
                }
            }
            return  // 如果没有网络则直接返回，不执行下面的
        }

        // 2.否则，如果有网络连接，则进行网络获取，获取之后进行缓存
        swipe?.isRefreshing = true
        doAsync {
            val jsonStr = URL(requestUrl).readText()
            val dataList = Gson().fromJson<MutableList<Offer>>(jsonStr, object : TypeToken<MutableList<Offer>>() {}.type)

            ctx.runOnUiThread {
                llHeader?.visibility = View.VISIBLE
                emptyTip?.visibility = View.GONE

                lvData?.adapter = PopularAdapter(ctx, dataList)  // 分页加载的时候这里是adapter.add方法
                swipe?.isRefreshing = false

                // 缓存的写入，仍然要在子线程中
                doAsync {
                    // 将网络数据进行缓存,按从网络获取的数据列表进行缓存！
                    OfferCache.addToMCache(CACHE_NAME, dataList)
                    OfferCache.writeToDCache(CACHE_NAME, dataList)
                }
            }
        }
    }
}