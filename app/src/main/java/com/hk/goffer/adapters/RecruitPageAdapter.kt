package com.hk.goffer.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View

/**
 * Created by hk on 2016/8/21.
 */
class RecruitPageAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    lateinit  var titles: Array<String>
    lateinit  var fragments: MutableList<Fragment>
    constructor(fm: FragmentManager?,titles:Array<String>,fragments:MutableList<Fragment>):this(fm){
        this.titles = titles
        this.fragments = fragments
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }


}