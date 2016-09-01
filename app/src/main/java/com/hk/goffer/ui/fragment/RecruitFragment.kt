package com.hk.goffer.ui.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hk.goffer.R
import com.hk.goffer.adapters.RecruitPageAdapter
import com.hk.goffer.ui.activity.SearchActivity
import com.hk.goffer.utils.AnimUtils
import kotlinx.android.synthetic.main.fragment_recruit.*
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by hk on 2016/8/21.
 */
class RecruitFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_recruit,null,false)
        return view!!  // 兼容带问号和不带问号的
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search_view.setOnClickListener {
            startActivity<SearchActivity>()
            act.overridePendingTransition(R.anim.right_activity_enter,R.anim.left_activity_exit)
        }

        tab_cat.tabMode = TabLayout.MODE_FIXED  // tab_cat 是联动的标题

        pager_recruit.adapter = RecruitPageAdapter(childFragmentManager, arrayOf("热门","时间","学校"),
                mutableListOf<Fragment>(PopularFragment(),TimeFragment(),SchoolFragment()))

        tab_cat.setupWithViewPager(pager_recruit)

        pager_recruit.currentItem = 0
        pager_recruit.offscreenPageLimit = 2

        // 添加FloatingActionButton的收缩动画
        pager_recruit.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when(position){
                    1-> {
                        AnimUtils.expand(TimeFragment.fab as View)
                        AnimUtils.shrink(SchoolFragment.fab as View)
                    }
                    2->{
                        AnimUtils.shrink(TimeFragment.fab as View)
                        AnimUtils.expand(SchoolFragment.fab as View)
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
    }
}


