package com.example.vacationventurepe.interfaces

import android.view.View

interface FloatFragmentAnimation {

    suspend fun enterFragmentAnimation(view: View, startY: Int) {

        view.visibility = View.INVISIBLE

        view.apply {
            pivotY = startY.toFloat()
            pivotX = width / 2.0f

            scaleY = 0f
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start()
        }

    }

    suspend fun exitFragmentAnimation(view: View, startY: Int) {

        view.apply {
            pivotY = startY.toFloat()
            pivotX = width / 2.0f

                animate()
                    .scaleX(0f)
                    .alpha(0f)
                    .setDuration(300)
                    .start()
        }

    }

}