package com.example.vacationventurepe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.example.vacationventurepe.application.RoboApplication
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.basetools.BaseActivity
import com.example.vacationventurepe.basetools.RoboActivity.roboStartActivityWithFinishChoose
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.network.fetchers.ServerFetcher
import com.example.vacationventurepe.test.CredentialsManager
import com.example.vacationventurepe.viewmodels.GlobalViewModel
import com.example.vacationventurepe.basetools.PB

class CoverActivity : BaseActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var credentialsManager: CredentialsManager

    private lateinit var versionTextView:TextView

    private lateinit var globalViewModel: GlobalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_cover)

        logoImage = findViewById(R.id.cover_logo)

        versionTextView = findViewById(R.id.cover_activity_version_text_view)
        versionTextView.text = packageManager.getPackageInfo(this.packageName,0).versionName

        credentialsManager = CredentialsManager(this)

        globalViewModel = (application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]


        tryLogin()

        //测试用例DEBUG
//        val animationTask = object : TimerTask() {
//            override fun run() {
//                runOnUiThread {
//                    setLoginAnime()
////                    if (Random.nextBoolean())
////                        setMainAnime()
////                    else
////                        setLoginAnime()
//                }
//
//            }
//        }
//        Timer().schedule(animationTask, 1800)

    }

    private fun tryLogin() {

        var account: String
        var password: String

        credentialsManager.getCredentials().apply {
            if (first != null && second != null) {
                account = first.toString()
                password = second.toString()

                if (account == "" && password == "") {
                    runOnUiThread { setLoginAnime() }
                    return
                }

            } else {
                runOnUiThread { setLoginAnime() }
                return
            }
        }

        ServerFetcher().loginService(account, password) {
            globalViewModel.postSessionID(it)
        }.observe(this) {

            when (it) {
                Base.NETWORK_ON_FAILURE.toString() -> {
                    Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT)
                        .show()
                    runOnUiThread { setLoginAnime() }
                }

                PB.L_E_C_PASSWORD -> {
                    Toast.makeText(this, getString(R.string.password_changed), Toast.LENGTH_SHORT)
                        .show()
                    runOnUiThread { setLoginAnime() }
                }

                PB.L_E_C_ACCOUNT -> {
                    Toast.makeText(this, getString(R.string.account_changed), Toast.LENGTH_SHORT)
                        .show()
                    credentialsManager.saveCredentials("", "")
                    runOnUiThread { setLoginAnime() }
                }

                is Student -> {
                    globalViewModel.studentEntityLiveData.postValue(it)
                    runOnUiThread {
                        setMainAnime()
                    }
                }

                else -> {
                    Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT)
                        .show()
                    runOnUiThread { setLoginAnime() }
                }
            }

        }

    }

    private fun setLoginAnime() {

        val logoTranY = logoImage.y

        val scaleXAnime = ObjectAnimator.ofFloat(logoImage, "scaleX", 1f, 0.74f)
        val scaleYAnime = ObjectAnimator.ofFloat(logoImage, "scaleY", 1f, 0.74f)

        val tranYAnime = ObjectAnimator.ofFloat(logoImage, "translationY", 0f, -logoTranY * 0.85f)

        val animatorSet = AnimatorSet()

        animatorSet.play(scaleYAnime).with(scaleXAnime).with(tranYAnime)
        animatorSet.duration = 500

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                runOnUiThread {
                    val shareOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@CoverActivity,
                        logoImage,
                        "LogoTrans"
                    )

                    this@CoverActivity.roboStartActivityWithFinishChoose<LoginActivity>(
                        listOf(
                            Pair(
                                "animationType",
                                Base.ANIMATION_FADE
                            )
                        ), shareOption.toBundle(), Base.START_TYPE_FINISH
                    )
                }
            }
        })

        animatorSet.start()


    }

    private fun setMainAnime() {

        val logoTranY = logoImage.y

        val scaleXAnime = ObjectAnimator.ofFloat(logoImage, "scaleX", 1f, 0.85f)
        val scaleYAnime = ObjectAnimator.ofFloat(logoImage, "ScaleY", 1f, 0.85f)

        val tranYAnime = ObjectAnimator.ofFloat(logoImage, "translationY", 0f, logoTranY * 0.95f)

        val animatorSet = AnimatorSet()

        animatorSet.play(scaleXAnime).with(scaleYAnime).with(tranYAnime)
        animatorSet.duration = 800

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                runOnUiThread {
                    val shareOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@CoverActivity,
                        logoImage,
                        "LogoTrans"
                    )

                    this@CoverActivity.roboStartActivityWithFinishChoose<MainActivity>(
                        listOf(
                            Pair(
                                "animationType",
                                Base.ANIMATION_FADE
                            )
                        ), shareOption.toBundle(), Base.START_TYPE_FINISH
                    )
                }
            }
        })

        animatorSet.start()
    }

}