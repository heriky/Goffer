package com.hk.goffer

/**
 * Created by hk on 2016/8/30.
 */
object AppConfig {
    val host = "http://222.25.4.10:3000/api/v1"  // 主机名+ api主路由
    val popularUrl = "$host/offers/all"          // 热门信息查询
    val timeUrl = "$host/offers/time"           // 按时间查询
    var schoolsUrl = "$host/offers/schools"     // 查询所有学校
    var schoolDetailUrl = "$host/offers/school" // 按学校查询信息详情
    var keywordUrl = "$host/offers/"            // 按关键字查询
    var pvUrl = "$host/offer/"               // 更新PV
}