package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.hk.goffer.R
import com.hk.goffer.db.HistoryCRUD
import com.hk.goffer.db.HistoryData
import com.hk.goffer.ui.activity.OfferDetailActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by hk on 2016/8/29.
 */

class BrowseHistoryFragment: BaseFragment(){

    lateinit  var adapter: BrowseAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_browse_history, null, false)
        return view!!
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //0. toolbar 设置返回按钮
        val toolbar = view?.find<Toolbar>(R.id.toolbar_browse_history)
        toolbar?.setNavigationOnClickListener {
            fragmentManager.popBackStack()
        }

        //2. 右侧清除按钮设置事件
        val clearBtn = view?.find<ImageButton>(R.id.clear_browse_history)
        clearBtn?.onClick {
            alert ("是否移除所有项？","删除提示"){
                negativeButton("否")
                positiveButton("是"){
                    //HistoryCRUD.removeAllSync() // 数据库中清除所有 同步方法
                    HistoryCRUD.removeAll {
                        adapter.clear()  // 列表显示中清除所有
                    }
                }
            }.show()

        }

        // 3.设置数据列表
        val lvDetail = view?.find<ListView>(R.id.lv_browse_history)
        //val browseData = HistoryCRUD.queryAllSync() 同步方法

        HistoryCRUD.queryAll {
            browseData->
            adapter = BrowseAdapter(browseData.sortedBy { it.recent_browse }.reversed())
            lvDetail?.adapter = adapter
            setupListItem(lvDetail)
        }
    }

    /**
     * 设置listview的单击和长按事件
     */
    private fun  setupListItem(lvDetail: ListView?) {
        lvDetail?.onItemLongClick { adapterView, view, i, l ->
            alert("是否删除该项","删除提示") {
                negativeButton("否")
                positiveButton("是"){
                    val historyData = lvDetail.getItemAtPosition(i) as HistoryData

                    //HistoryCRUD.delByIdSync(historyData.id) // 从数据库中删除同步方法
                    HistoryCRUD.delById(historyData.id){
                        adapter.remove(historyData) // 从列表中删除
                    }
                }
            }.show()
            true
        }

        lvDetail?.onItemClick { adapterView, view, i, l ->
            val history = lvDetail.getItemAtPosition(i) as HistoryData
            ctx.startActivity<OfferDetailActivity>("detailUrl" to history.detailUrl)
        }
    }

    inner class BrowseAdapter(var browseData: List<HistoryData>): ArrayAdapter<HistoryData>(ctx,0,browseData){
        val inflater: LayoutInflater = LayoutInflater.from(ctx)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view:View
            val holder: BrowseHistoryViewHolder

            if (convertView == null){
                view = inflater.inflate(R.layout.list_browse_history_item,null,false)
                holder = BrowseHistoryViewHolder(view)
                view.tag = holder
            }else{
                view = convertView
                holder = convertView.tag as BrowseHistoryViewHolder
            }

            holder.setupHolderByData(browseData[position])
            return view
        }
    }
}

class BrowseHistoryViewHolder{
    var enterprise:TextView by Delegates.notNull()
    var datetime: TextView by Delegates.notNull()
    var addr: TextView by Delegates.notNull()
    var access: TextView by Delegates.notNull()
    constructor(view:View){
        enterprise = view.find(R.id.browse_enterprise)
        datetime = view.find(R.id.browse_datetime)
        addr = view.find(R.id.browse_addr)
        access = view.find(R.id.tv_access_time)
    }

    fun setupHolderByData(data: HistoryData){
        enterprise.text = data.enterprise
        datetime.text = data.datetime
        addr.text = data.addr
        access.text = SimpleDateFormat("MM-dd HH:mm:ss",Locale.CHINESE).format(Date(data.recent_browse))
    }
}