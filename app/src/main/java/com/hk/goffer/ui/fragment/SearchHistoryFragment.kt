package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import com.hk.goffer.R
import com.hk.goffer.ui.activity.SearchActivity
import com.hk.goffer.utils.HistoryCache
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.withArguments

/**
 * Created by hk on 2016/8/30.
 */
class SearchHistoryFragment : BaseFragment(){

    var lvHistory: ListView? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_search_history,null,false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lvHistory = view?.find(R.id.lv_search_history)

        // 从缓存中读取搜索历史
        val cacheName = SearchActivity.CACHE_NAME
        val searchList:MutableList<String>? = HistoryCache.readFromDCache(cacheName) ?: HistoryCache.readFromDCache(cacheName)
        if (searchList == null || searchList.size == 0) return

        lvHistory?.adapter =object: ArrayAdapter<String>(ctx,R.layout.search_history_item,R.id.tv_search_history, searchList){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val mView =  super.getView(position, convertView, parent)
                val keyword = getItem(position)

                // 添加整体点击事件
                mView.onClick {
                    fragmentManager.beginTransaction()
                            .addToBackStack("search_result")
                            .replace(R.id.search_content,SearchResultFragment()
                                    .withArguments("keyword" to keyword)
                                    ,"search_result")
                            .commit()
                }

                // 添加局部点击事件
                val closeBtn = mView?.find<ImageButton>(R.id.btn_history_remove)
                closeBtn?.onClick {
                    // 同时删除内存中和硬盘中的缓存
                    searchList.remove(keyword)
                    HistoryCache.addToMCache(cacheName,searchList)
                    HistoryCache.writeToDCache(cacheName,searchList)
                    remove(keyword)
                }
                return mView
            }
        }
    }

}