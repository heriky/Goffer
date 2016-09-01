package com.hk.goffer

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hk.goffer.ui.activity.BaseActivity
import com.hk.goffer.ui.cview.FragmentTabHost
import com.hk.goffer.ui.fragment.ConfigFragment
import com.hk.goffer.ui.fragment.RecruitFragment
import com.hk.goffer.ui.fragment.ScheduleFragment
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class MainActivity : BaseActivity() {

    private val CONTENT_MAP = mapOf(
            Pair("招聘会",R.drawable.selector_tab_recruit) to RecruitFragment::class.java,
            Pair("计划",R.drawable.selector_tab_schedule) to  ScheduleFragment::class.java,
            Pair("设置",R.drawable.selector_tab_settings) to ConfigFragment::class.java
    )

    private var timeRecord = 0L  // 记录点击返回键的时间

    lateinit private var tabHost : FragmentTabHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabHost = find<FragmentTabHost>(android.R.id.tabhost)

        if (savedInstanceState == null) {
            // 1. 设置底部菜单栏
            setTabHost()
        }
    }

    /**
     * 用于设置底部菜单栏
     */
    private fun setTabHost() {
        tabHost.setup(ctx,supportFragmentManager,R.id.activity_main_container)

        for((info, content) in CONTENT_MAP){

            tabHost.addTab(tabHost.newTabSpec(info.first).setIndicator(genTabIndicatorView(info.first,info.second)),
                    content, null)
        }
        tabHost.currentTab = 0
        tabHost.tabWidget.setDividerDrawable(android.R.color.transparent)
        tabHost.setOnTabChangedListener {
            if (supportFragmentManager.backStackEntryCount != 0){
                supportFragmentManager.popBackStack() // 栈中如果有 ，就移除，这个设置是确保专辑详情状态下切换底部tab
                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("计划")).commit()
            }

        }
    }

    /**
     * 生成TabWidget布局
     */
    private fun genTabIndicatorView(title:String, iconResource:Int): View {
        val indicatorView = layoutInflater.inflate(R.layout.tab_indicator,null,false)

        val tabIcon = indicatorView.find<ImageView>(R.id.iv_tab_icon)
        val tabTitle = indicatorView.find<TextView>(R.id.tv_tab_title)
        //tabIcon.imageResource = iconResource  使用selector来制作切换效果
        tabIcon.backgroundResource = iconResource
        tabTitle.text = title

        return indicatorView
    }

    override fun onBackPressed() {
        if (SystemClock.currentThreadTimeMillis() - timeRecord < 2000){
            super.onBackPressed()
        }else{
            toast("再按一次退出程序")
        }
        timeRecord = SystemClock.currentThreadTimeMillis()
    }
}


