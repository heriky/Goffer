package com.hk.goffer.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hk.goffer.AppConfig
import com.hk.goffer.MainActivity
import com.hk.goffer.R
import com.hk.goffer.models.School
import com.hk.goffer.utils.GraphicUtils
import kotlinx.android.synthetic.main.activity_schools.*
import org.jetbrains.anko.*
import java.net.URL

class SchoolsActivity : AppCompatActivity() {
    companion object{
        val RESULT_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//        val slide = Slide()
//        slide.interpolator = DecelerateInterpolator()
//        slide.duration = 1000
//        window.enterTransition = slide

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schools)

        // 获取学校列表
        doAsync {
            val jsonStr = URL(AppConfig.schoolsUrl).readText(Charsets.UTF_8)
            val schools = Gson().fromJson<MutableList<School>>(jsonStr, object : TypeToken<MutableList<School>>() {}.type)

            runOnUiThread {
                gv_schools.adapter = SchoolGvAdapter(schools)
            }
        }
    }

    inner class SchoolGvAdapter(val schools: MutableList<School>) : ArrayAdapter<School>(ctx, 0, schools) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val btn: Button

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.grid_school_item,null,false)
                btn = view.find(R.id.btn_school)
                view.tag = btn
            }else{
                view = convertView
                btn = convertView.tag as Button
            }
            btn.backgroundDrawable = GraphicUtils.getCircleDrawable(ctx,"",40,0)
            btn.text = schools[position].name
            btn.onClick {
                setResult(RESULT_CODE,intentFor<MainActivity>("school_id" to schools[position]._id,"school_name" to schools[position].name))
                finish()
            }


            return view
        }
    }
}
