package com.hk.goffer.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hk.goffer.R
import com.jakewharton.disklrucache.DiskLruCache
import kotlinx.android.synthetic.main.activity_offer_detail.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class OfferDetailActivity : BaseActivity() {

    lateinit  var detailUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_detail)


        // 获取url
        detailUrl = intent.getStringExtra("detailUrl")

        // 设置actionbar
        toolbar_offer_detail.title = "详情"
        toolbar_offer_detail.navigationIconResource = R.drawable.ic_chevron_left_white_36dp

        setSupportActionBar(toolbar_offer_detail)
        toolbar_offer_detail.setNavigationOnClickListener {
            finish()
        }

        // 设置加载条
        swipe_offer_detail.isRefreshing = true
        swipe_offer_detail.setProgressViewOffset(false, 0, dip(25))

        // 设置fab
        fab_add_schedule.onClick { v->
            Snackbar.make(fab_add_schedule.parent as View,"加入计划中吗？",Snackbar.LENGTH_LONG)
                    .setAction("确定"){v->
                        toast("确定按下")
                    }.setActionTextColor(Color.WHITE).show()

        }

        // 设置webView
        setupWebView(web_offer_detail)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_offer_detail,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.action_browser -> browse(detailUrl, true)
            R.id.action_share -> share("这个招聘会不错，一起去吧，详情在这里：$detailUrl","招聘会信息")
        }
        return super.onOptionsItemSelected(item)
    }


    fun setupWebView(wv: WebView){
        val settings = wv.settings
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.setSupportZoom(true)
        settings.textZoom = 100
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        wv.setWebViewClient(object: WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                swipe_offer_detail.isRefreshing = false
                swipe_offer_detail.isEnabled = false
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        })
        wv.loadUrl(detailUrl, mutableMapOf("User-Agent" to "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1"))

    }
}


