package com.hk.goffer.ui.fragment

import android.app.FragmentTransaction
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hk.goffer.R
import com.hk.goffer.db.AlbumCRDU
import com.hk.goffer.db.AlbumData
import com.hk.goffer.db.HistoryCRUD
import com.hk.goffer.utils.GraphicUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.*
import kotlin.properties.Delegates

/**
 * Created by hk on 2016/8/21.
 */
class ScheduleFragment : BaseFragment() {

    private val TAG = "ScheduleFragment"
    private var albumAdapter: ArrayAdapter<AlbumData>? = null
    private var resolver: ContentResolver? = null
    private var albumObserver: ContentObserver? = null
    private var historyObserver: ContentObserver? = null
    private var browseHistory: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolver = ctx.contentResolver
        albumObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                toast("数据库发生了变化")
                val albums = AlbumCRDU.instance.queryAllSync()
                albumAdapter?.clear()
                albumAdapter?.addAll(albums)  // 刷新数据
            }
        }

        historyObserver = object: ContentObserver(Handler()){
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // 从新查询，以设置浏览数目
                HistoryCRUD.queryAll {
                    browseHistory?.text = "${it.size}"
                }
            }
        }


        resolver?.registerContentObserver(Uri.parse(AlbumCRDU.instance.ACTION_UPDATE), true, albumObserver)
        resolver?.registerContentObserver(Uri.parse(HistoryCRUD.ACTION_ADD), true, historyObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        resolver?.unregisterContentObserver(albumObserver)
        resolver?.unregisterContentObserver(historyObserver)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_schedule, null, false)
        return view!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置浏览记录
        val browserHistory = view?.find<LinearLayout>(R.id.ll_browse_history)
        val shareHistory = view?.find<LinearLayout>(R.id.ll_share_history)
        setupHistory(browserHistory, shareHistory)

        // 查询数据库，开始填充Album数据
        val lvSchedule = view?.find<ListView>(R.id.lv_schedule)
        //AlbumCRDU.instance.queryAllSync()
        AlbumCRDU.instance.queryAll {
            albums ->
            albumAdapter = AlbumsAdapter(albums)
            lvSchedule?.adapter = albumAdapter
            addAlbumItemListener(lvSchedule)
        }

        // 设置加号监听
        val btnAdd = view?.find<FloatingActionButton>(R.id.fab_add_album)
        addFabClickListener(btnAdd)
    }


    // 设置浏览历史和分享历史点击事件, 最多显示最近100条记录
    fun setupHistory(bHistory: LinearLayout?, sHistory: LinearLayout?) {
        browseHistory = bHistory?.find<TextView>(R.id.browse_history_count)
        HistoryCRUD.queryAll {
            browseHistory?.text = "${it.size}"
        }
        bHistory?.onClick {
            fragmentManager
                    .beginTransaction()
                    .hide(this@ScheduleFragment)
                    .addToBackStack(TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .add(R.id.activity_main_container, BrowseHistoryFragment())
                    .commit()
        }

        sHistory?.onClick {
            toast("分享历史")
        }
    }

    // 针对Album列表的一系列设置，主要是添加事件响应
    fun addAlbumItemListener(lvSchedule: ListView?) {
        lvSchedule?.onItemLongClick { adapterView, view, i, l ->
            val albumObj = lvSchedule.getItemAtPosition(i) as AlbumData
            val albumId = albumObj._id

            alert("=_= 此计划表创建于${albumObj.create_time},请问你是要删除它吗?", "删除提示") {
                positiveButton("删除它", {
                    AlbumCRDU.instance.delById(albumId) {
                        albumAdapter?.remove(albumObj)
                    }
                })
                negativeButton("点错了", { dismiss() })
                cancellable(false)
            }.show()

            true
        }

        lvSchedule?.onItemClick {
            adapterView, view, i, l ->
            val obj = lvSchedule.getItemAtPosition(i) as AlbumData
            if (obj.album_size == 0L) {
                toast("该计划表无任何内容")
            } else {
                // 开启详情！！！新开一个fragment,进行替换，要加上动画和回复效果
                fragmentManager
                        .beginTransaction()
                        .hide(this@ScheduleFragment)
                        .addToBackStack(TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .add(R.id.activity_main_container, AlbumDetailFragment()
                                .withArguments("album_id" to obj._id,
                                        "album_name" to obj.name,
                                        "album_size" to obj.album_size,
                                        "create_time" to obj.create_time), "AlumDetailFragment")
                        .commit()
            }
        }
    }

    fun addFabClickListener(btnAdd: ImageButton?) {
        btnAdd?.onClick {
            // 自定义消息弹出框, 考虑做成单例的形式
            var inputEditText: EditText? = null
            val newView = UI {
                verticalLayout {
                    inputEditText = editText {
                        hint = "输入合理的名称,不超过10个字"
                        hintTextColor = 0xcccccc.opaque
                        padding = dip(3)
                        lparams {
                            setMargins(dip(16), dip(8), dip(16), dip(16))
                        }
                        addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                val length: Int = s?.length!!
                                if (length > 10) {
                                    this@editText.error = "请不要输入超过10个字符"
                                    val sliced = this@editText.text?.toString()?.substring(0, 10)
                                    this@editText.editableText.clear()
                                    this@editText.editableText.append(sliced)
                                }
                            }

                        })
                    }
                }
            }.view

            // 显示弹出框
            alert("输入计划表名称", "创建新的计划表") {
                customView(newView)
                negativeButton("取消")
                positiveButton("确认", {
                    val finalName = inputEditText?.editableText.toString()
                    AlbumCRDU.instance.addUnique(finalName, {
                        rs ->
                        if (rs == -1) {
                            toast("该名称已存在！")
                        } else {
                            albumAdapter?.add(AlbumData(finalName)) //直接用假数据，省的再查一次数据库
                        }
                    })
                })
            }.show()
        }
    }

    // 内部类制作adapter
    inner class AlbumsAdapter(val albums: List<AlbumData>) : ArrayAdapter<AlbumData>(ctx, -1, albums) {
        val inflater = LayoutInflater.from(ctx)!!

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: AlbumViewHolder
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_album_item, null, false)
                holder = AlbumViewHolder(view)
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as AlbumViewHolder
            }
            holder.setHolderByAlbum(albums[position])

            return view
        }
    }
}

// Album的ViewHolder
class AlbumViewHolder() {
    var face: ImageView by Delegates.notNull()
    var albumName: TextView by Delegates.notNull()
    var albumSize: TextView by Delegates.notNull()

    constructor(view: View) : this() {
        face = view.find(R.id.album_face)
        albumName = view.find(R.id.album_name)
        albumSize = view.find(R.id.album_size)
    }

    fun setHolderByAlbum(album: AlbumData) {
        face.imageBitmap = GraphicUtils.drawCircle(album.name.substring(0, 2), 48, 24)
        albumName.text = album.name
        albumSize.text = album.album_size.toString()
    }
}
