package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import cn.aigestudio.datepicker.cons.DPMode
import cn.aigestudio.datepicker.views.DatePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.AppConfig
import com.hk.goffer.R
import com.hk.goffer.models.Offer
import com.hk.goffer.today
import com.hk.goffer.utils.CommonUtil
import com.hk.goffer.utils.OfferCache
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hk on 2016/8/21.
 */
class TimeFragment : BaseFragment(){

    companion object {
        var fab: FloatingActionButton? = null
    }
    val CACHE_NAME = "offers_datetime"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_datetime,null,false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultDate = Date()

        val timeHeader = view?.find<TextView>(R.id.tv_header_datetime)
        val swipe = view?.find<SwipeRefreshLayout>(R.id.refresh_datetime)
        val lvDatetime = view?.find<ListView>(R.id.lv_offer_datetime)
        fab = view?.find<FloatingActionButton>(R.id.fab_date_picker)

        timeHeader?.text = SimpleDateFormat("yyyy-MM-dd",Locale.CHINESE).format(defaultDate)
        fab?.onClick {
            // 展开日期控件,点击后重新加载新的数据
            // 如果是分页加载，这里必须使用adapter.add方法进行刷新
            showDatePicker(lvDatetime,swipe, timeHeader!!)
        }

        // 一开始拉取当天的数据，一旦date变化，再刷新数据
        obtainData(lvDatetime,swipe,ctx.today)
    }



    /**
     * 一开始获取最初的数据，默认以当天时间为参数进行查询
     */
    private fun obtainData(lvDatetime: ListView?,swipe: SwipeRefreshLayout?, date: String) {

        // 1. 断网状态下，取出缓存，或者拉取远程数据
        if (!CommonUtil.checkNetState()) {
            doAsync {
                var offers = OfferCache.getFromMCache(CACHE_NAME)
                if (offers == null || offers.size == 0) {
                    offers = OfferCache.readFromDCache(CACHE_NAME)
                    OfferCache.addToMCache(CACHE_NAME, offers) // 从硬盘读出后优先放入内存缓存中
                }
                val popularList = offers?.toMutableList()

                uiThread {
                    if (popularList != null && popularList.size != 0) {
                        lvDatetime?.adapter = PopularAdapter(ctx, popularList)
                        toast("当前网络未连通，仅显示缓存数据！")
                    }
                }
            }
            return  // 如果没有网络则直接返回，不执行下面的
        }

        swipe?.isRefreshing = true
        val requestUrl = AppConfig.timeUrl+"?t=${URLEncoder.encode(date,"UTF-8")}"
        doAsync {
            val jsonStr = URL(requestUrl).readText()
            val dataList = Gson().fromJson<MutableList<Offer>>(jsonStr,object : TypeToken<MutableList<Offer>>() {}.type)

            ctx.runOnUiThread {
                toast("时间线成功")
                lvDatetime?.adapter = PopularAdapter(ctx,dataList)  // 分页加载的时候这里是adapter.add方法
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

    /**
     * 展开日期控件
     */
    private fun showDatePicker(lvDatetime: ListView?,swipe: SwipeRefreshLayout?,timeHeader: TextView) {
        val dialog = AlertDialog.Builder(ctx).create();
        dialog.show()
        val picker = DatePicker(ctx)

        val cld = Calendar.getInstance()
        picker.setDate(cld.get(Calendar.YEAR), cld.get(Calendar.MONTH) + 1)

        picker.setMode(DPMode.SINGLE)
        picker.setOnDatePickedListener { date ->
            dialog.dismiss()
            // 开始刷新数据：缓存或者网络请求
            timeHeader.text = date
            obtainData(lvDatetime,swipe,date)
        }


        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.window.setContentView(picker, params)
        dialog.window.setGravity(Gravity.CENTER)
    }
}