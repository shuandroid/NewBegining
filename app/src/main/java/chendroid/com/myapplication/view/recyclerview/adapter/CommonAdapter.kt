package chendroid.com.myapplication.view.recyclerview.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import chendroid.com.myapplication.view.recyclerview.MultipleType
import chendroid.com.myapplication.view.recyclerview.ViewHolder

/**
 *  Created by @author chen.zhao, on @date 2018/6/29  下午2:53
 */
abstract class CommonAdapter<T>(var mContext: Context, var mData: ArrayList<T>, //条目布局
                                private var mLayoutId: Int) : RecyclerView.Adapter<ViewHolder>() {

    protected var mInflater: LayoutInflater? = null
    protected var mTypeSupport: MultipleType<T>? = null

    // 使用接口回调点击事件
    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null

    init {
        mInflater = LayoutInflater.from(mContext)
    }

    constructor(context: Context, data: ArrayList<T>, typeSupport: MultipleType<T>) :
            this(context, data, -1) {
        this.mTypeSupport = typeSupport
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (mTypeSupport != null) {
            //需要多布局
            mLayoutId = viewType
        }
        //创建view
        val view = mInflater?.inflate(mLayoutId, parent, false)
        return ViewHolder(view!!)
    }

    override fun getItemViewType(position: Int): Int {
        //多布局问题
        return mTypeSupport?.getLayoutId(mData[position], position)
                ?: super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        bindData(holder, mData[position], position)

        mItemClickListener?.let {
            holder.itemView.setOnClickListener {
                mItemClickListener!!.onItemClick(mData[position], position)
            }
        }

        mItemLongClickListener?.let {
            holder.itemView.setOnLongClickListener {
                mItemLongClickListener!!.onItemLongClick(mData[position], position)
            }
        }


    }


    /**
     * 将必要参数传递出去
     *
     * @param holder
     * @param data
     * @param position
     */
    protected abstract fun bindData(holder: ViewHolder, data: T, position: Int)

    override fun getItemCount(): Int {

        return mData.size
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.mItemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener
    }


}