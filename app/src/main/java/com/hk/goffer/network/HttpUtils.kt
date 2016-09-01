package com.hk.goffer.network

import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.App
import com.hk.goffer.models.Offer

/**
 * Created by hk on 2016/9/1.
 */
object HttpUtils {

    val mVolley: RequestQueue = Volley.newRequestQueue(App.instance.applicationContext)
    /**
     * 按照url，以get方式进行请求，获取的是offer的列表
     */
    fun getOffers(url: String, cbSuccess: (MutableList<Offer>) -> Unit, cbError: (VolleyError) -> Unit) {

        val request = StringRequest(Request.Method.GET, url, Response.Listener<kotlin.String> {
            val type = object : TypeToken<MutableList<Offer>>() {}.type
            val dataList = Gson().fromJson<MutableList<Offer>>(it, type)  //注意这里fromJson也提供了泛型
            cbSuccess(dataList)
        }, Response.ErrorListener {
            cbError(it)
        })
        request.retryPolicy = DefaultRetryPolicy(5000,3,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        mVolley.add(request)
    }
}