package com.example.vacationventurepe.interfaces

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation


interface CollAnimationInterface {

    fun setCollapseAnimaGone(targetLayout: View) {
        targetLayout.visibility = View.GONE
    }

    fun setExpandAnimaAppear(view: View) {
        view.visibility = View.VISIBLE
    }

}