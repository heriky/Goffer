package com.hk.goffer.ui.activity

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import com.hk.goffer.R
import com.hk.goffer.ui.fragment.SearchHistoryFragment
import com.hk.goffer.ui.fragment.SearchResultFragment
import com.hk.goffer.utils.AnimUtils
import com.hk.goffer.utils.HistoryCache
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.withArguments

class SearchActivity : AppCompatActivity() {

    companion object{
        val CACHE_NAME = "search_history"  // 缓存搜索记录
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputMethodManager.showSoftInputFromInputMethod(edit_keywords.windowToken, 0)

        toolbar_search.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.left_activity_enter, R.anim.right_activity_exit)
        }

        // 初始添加搜索历史页面
        supportFragmentManager.beginTransaction()
                .replace(R.id.search_content,SearchHistoryFragment(),"searchHistory")
                .commit()

        // 设置关键字搜索(点击搜索按钮)
        btn_search.onClick {
            // 缓存搜索记录
            val keyword = edit_keywords.text.toString().trim()
            var keywordList:MutableList<String>? = null
            if (HistoryCache.getFromMCache(CACHE_NAME) == null && HistoryCache.readFromDCache(CACHE_NAME) == null){
                keywordList= mutableListOf(keyword)
            }else{
                keywordList = HistoryCache.getFromMCache(CACHE_NAME) ?: HistoryCache.readFromDCache(CACHE_NAME)
                keywordList?.add(0,keyword) // 往头部插入
            }
            HistoryCache.addToMCache(CACHE_NAME, keywordList!!)  // 将新的关键字加入缓存
            HistoryCache.writeToDCache(CACHE_NAME, keywordList)

            goSearch()
        }

        // 设置关键字搜索(点击键盘上是搜索按键)
        edit_keywords.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH){
                goSearch()
                true
            }else{
                false
            }

        }
    }

    /**
     * 产生搜索结果
     */
    fun goSearch(){
        val keyword = edit_keywords.text.toString().trim()
        if (TextUtils.isEmpty(keyword)){
            AnimUtils.shake(edit_keywords)

            edit_keywords.error = "请首先输入关键字"
        }else{
            supportFragmentManager.beginTransaction()
                    .addToBackStack("search_result")
                    .replace(R.id.search_content, SearchResultFragment()
                            .withArguments("keyword" to keyword)
                            ,"search_result")
                    .commit()
        }
    }
}
