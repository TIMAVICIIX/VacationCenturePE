package com.example.vacationventurepe.basetools

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import android.widget.TextView

class Base {

    companion object {

        const val ANIMATION_FADE = "fade"
        const val ANIMATION_SLIDE = "slide"

        const val START_TYPE_FINISH = 0
        const val START_TYPE_STAYME = 1

        const val ACTIVITY_BACK_ONCE = 2
        const val ACTIVITY_BACK_TWICE = 3

        const val CHANGE_SRC = 4
        const val CHANGE_ANIME = 5

        const val CENTER_ANIME_REDUCE = 6
        const val CENTER_ANIME_MAGNIFY = 7

        //NETWORK OPTIONS
        const val NETWORK_ON_FAILURE = 8
        const val NETWORK_ON_SUCCESS = 12
        const val BASE_HTTP_LINK = "http://192.168.78.69:8080/StudentVentures_Hub/"
        const val BASE_HTTP_NAMESPACE = "mobile_student-controller"
        const val NETWORK_PRIVACY_POLICY = "privacy_policy.html"
        const val NETWORK_TERMS_OF_SERVICE = "terms_of_service.html"

        //MAIN ACTIVITY DIALOG OPTION
        const val DIALOG_TYPE_NORMAL = 9
        const val DIALOG_TYPE_PSW = 10
        const val DIALOG_TYPE_EXIT_LOGIN = 11

        //AMap权限组标识
        const val AMAP_REQUEST_PERMISSION = 721

        //AMapKey
        const val AMAP_KEY = "606a47fa8a5709b12ab4cdc22d532b46"

        //Vacation State
        const val VACATION_STATE_CHANGING = "可修改"
        const val VACATION_STATE_REPORTING = "填写中"
        const val VACATION_STATE_OVERDUE = "已过期"

        fun String?.toStringOrBlank(): String {
            return this?.toString() ?: " "
        }

        /**
         * @param imageViewTarget 更改的目标图片布局
         * @param changeType 更改的类型
         * @param changeObjectAnimator 如果通过动画方式更改图标，图标的动画集
         * */
        fun changeNavWithAnimeOrImage(
            imageViewTarget: ImageView,
            changeType: Int,
            changeColor: Int?,
            changeValueAnimator: ValueAnimator?,
            changeObjectAnimator: ObjectAnimator?
        ) {

            when (changeType) {

                CHANGE_SRC -> {
                    imageViewTarget.setColorFilter(changeColor!!)
                }

                CHANGE_ANIME -> {
                    changeValueAnimator?.start()
                    changeObjectAnimator?.start()
                }

            }

        }


        fun setTextViewColor(targetTextView: TextView, color: Int) {

            val colorSpan = ForegroundColorSpan(color)

            val stringBuilder = SpannableStringBuilder(targetTextView.text)

            stringBuilder.setSpan(
                colorSpan,
                0,
                stringBuilder.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )

            targetTextView.text = stringBuilder
        }


    }

}