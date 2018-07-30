package chendroid.com.myapplication.view.recyclerview

import java.text.FieldPosition

/**
 *  Created by @author chen.zhao, on @date 2018/6/29  下午2:49
 *  多布局条目类型
 */
interface MultipleType<in T> {
    fun getLayoutId(item: T, position: Int): Int
}