package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.text.Html
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
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Created by hk on 2016/8/31.
 */

class SearchResultFragment : BaseFragment(){

    var tvKeyword: TextView? = null
    var keyword: String =""
    var emptyTip: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_search_result,null,false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvKeyword = view?.find<TextView>(R.id.tv_search_keyword)
        emptyTip = view?.find<LinearLayout>(R.id.search_result_empty)
        val lvResult = view?.find<ListView>(R.id.lv_search_result)

        keyword = arguments["keyword"].toString()


        // 从网络加载数据
        val requestUrl = AppConfig.keywordUrl +"?q=${URLEncoder.encode(keyword,"UTF-8")}"
        obtainOffers(requestUrl,lvResult)
    }

    /**
     * 从网络获取数据
     */
    private fun obtainOffers(requestUrl: String, lvData: ListView?) {
        doAsync {
            val jsonStr = URL(requestUrl).readText()
            var dataList = Gson().fromJson<MutableList<Offer>>(jsonStr, object : TypeToken<MutableList<Offer>>() {}.type)
            if (dataList == null){
                dataList = ArrayList(0) // 没有数据，就是空的
            }
            ctx.runOnUiThread {
                lvData?.adapter = PopularAdapter(ctx, dataList)  // 分页加载的时候这里是adapter.add方法
                tvKeyword?.text = Html.fromHtml("关于<font color='#ff0000'>$keyword</font>共有${dataList.size}条记录")

                if (dataList.size == 0 ){
                    lvData?.visibility = View.GONE
                    emptyTip?.visibility = View.VISIBLE
                }
            }
        }
    }
}