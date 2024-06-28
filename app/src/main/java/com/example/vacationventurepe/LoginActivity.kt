package com.example.vacationventurepe


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
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

class LoginActivity : BaseActivity() {

    private lateinit var globalViewModel: GlobalViewModel

    private lateinit var loginTipTextView: TextView

    private lateinit var userAccount: EditText
    private lateinit var userPassword: EditText

    private lateinit var userAccountEntry: String
    private lateinit var userPasswordEntry: String

    private lateinit var policyTextView: TextView
    private var policyUri: String = ""
    private lateinit var serviceTextView: TextView
    private var serviceUri: String = ""

    private lateinit var loginBtn: Button

    private val disappearViews: MutableList<View> = mutableListOf()

    private lateinit var logoImage: RelativeLayout
    private lateinit var logoImageReal: ImageView

    private lateinit var versionTextView: TextView

    private lateinit var credentialsManager: CredentialsManager

    private val loginServerAPI = ServerFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        backStatus = Base.ACTIVITY_BACK_TWICE

        credentialsManager = CredentialsManager(this)
        //测试用例DEBUG
//        credentialsManager.saveCredentials("21083053026", "112233")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login_view)

        initData()

        setClickListener()

        fetchHTML()

    }

    private fun initData() {

        globalViewModel =
            (application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]

        loginTipTextView = findViewById(R.id.login_view_tips_text_view)

        userAccount = findViewById(R.id.editText_account_input)
        userPassword = findViewById(R.id.editText_password_input)

        policyTextView = findViewById(R.id.login_layout_privacy_policy_text_view)
        serviceTextView = findViewById(R.id.login_layout_terms_of_service_text_view)

        loginBtn = findViewById(R.id.login_btn)

        logoImage = findViewById(R.id.login_view_logo)
        logoImageReal = findViewById(R.id.login_view_logo_image)

        versionTextView = findViewById(R.id.login_layout_version_text_view)
        versionTextView.text = packageManager.getPackageInfo(this.packageName,0).versionName

        disappearViews.apply {
            add(findViewById(R.id.login_view_disappear_01))
            add(findViewById(R.id.login_view_disappear_02))
            add(findViewById(R.id.login_view_disappear_03))
            add(findViewById(R.id.login_view_disappear_04))
        }
    }

    private fun setClickListener() {
        loginBtn.setOnClickListener {
            userAccountEntry = userAccount.text.toString()
            userPasswordEntry = userPassword.text.toString()

            if (userPasswordEntry.isEmpty() || userAccountEntry.isEmpty()) {
                Toast.makeText(this, getString(R.string.login_remind), Toast.LENGTH_SHORT).show()
            } else {

                loginServerAPI.loginService(userAccountEntry, userPasswordEntry){
                    globalViewModel.postSessionID(it)
                }.observe(this) {
                    when (it) {
                        Base.NETWORK_ON_FAILURE.toString() -> Toast.makeText(
                            this,
                            getString(R.string.network_error),
                            Toast.LENGTH_SHORT
                        ).show()

                        PB.L_E_C_ACCOUNT -> {
                            loginTipTextView.text = getString(R.string.account_error)
                        }

                        PB.L_E_C_PASSWORD -> {
                            loginTipTextView.text = getString(R.string.password_error)
                        }

                        PB.L_E_INSIDE ->{
                            loginTipTextView.text = getString(R.string.inside_error)
                        }

                        is Student -> {

                            globalViewModel.studentEntityLiveData.postValue(it)

                            credentialsManager.saveCredentials(userAccountEntry, userPasswordEntry)
                            doDisappearViews()
                            moveLogo()
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                getString(R.string.network_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

//            测试用例DEBUG
//            if (credentialsManager.validateCredentials(userAccountEntry, userPasswordEntry)) {
//                doDisappearViews()
//                moveLogo()
//            } else {
//                Toast.makeText(this, "用户名与密码不正确,请重试!", Toast.LENGTH_SHORT).show()
//            }


        }

        policyTextView.setOnClickListener {
            if (policyUri.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUri))
                startActivity(intent)
            }
        }
        serviceTextView.setOnClickListener {
            if (serviceUri.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serviceUri))
                startActivity(intent)
            }
        }
    }

    private fun fetchHTML() {

        loginServerAPI.fetchHTML().observe(this) {
//            Log.d(this.javaClass.name, it.toString())
            if (it.isNotEmpty()) {
                policyUri = it[0].first
                policyTextView.text = it[0].second

                serviceUri = it[1].first
                serviceTextView.text = it[1].second
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun doDisappearViews() {

        disappearViews.forEach {
            it.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    it.visibility = View.INVISIBLE
                }
            })
        }

    }

    private fun moveLogo() {

        val logoTranY = logoImage.y + 250

        Log.d("LoginActivity", "logoTranY:$logoTranY")

        val scaleXAnime = ObjectAnimator.ofFloat(logoImage, "scaleX", 1f, 1.2f)
        val scaleYAnime = ObjectAnimator.ofFloat(logoImage, "ScaleY", 1f, 1.2f)

        val tranYAnime = ObjectAnimator.ofFloat(logoImage, "translationY", 0f, logoTranY * 3.8f)

        val animatorSet = AnimatorSet()

        animatorSet.play(scaleXAnime).with(scaleYAnime).with(tranYAnime)
        animatorSet.duration = 500

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                runOnUiThread {
                    val shareOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@LoginActivity,
                        logoImageReal,
                        "LogoTrans"
                    )

                    this@LoginActivity.roboStartActivityWithFinishChoose<MainActivity>(
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