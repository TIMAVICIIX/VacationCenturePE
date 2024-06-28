package com.example.vacationventurepe.basetools

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vacationventurepe.BroadcastReceiverConsist
import com.example.vacationventurepe.R

open class BaseActivity : AppCompatActivity() {

    private var broadcastReceiverGroup: MutableList<BroadcastReceiverConsist> =
        mutableListOf()

    open var backStatus:Int = Base.ACTIVITY_BACK_ONCE
        set(value){
            field = if(value == Base.ACTIVITY_BACK_ONCE || value ==Base.ACTIVITY_BACK_TWICE)value else field
        }

    private var lastBackPressedTime:Long = 0

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setEnterAnimation()

        broadcastReceiverGroup.forEach {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(it.broadcastReceiver, it.intent,it.broadType)
            }else{
                registerReceiver(it.broadcastReceiver, it.intent)
            }
        }

        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastReceiverGroup.forEach {
            unregisterReceiver(it.broadcastReceiver)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(backStatus == Base.ACTIVITY_BACK_ONCE) {
                finishWithAnimation(this)
                finish()
            }else{

                if(lastBackPressedTime + 2000>System.currentTimeMillis()){
                    finishWithAnimation(this)
                    finish()
                }else{
                    Toast.makeText(this,getString(R.string.exit_twice),Toast.LENGTH_SHORT).apply {
                        show()
                    }
                }

                lastBackPressedTime = System.currentTimeMillis()

            }
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }

    }

    private fun setEnterAnimation() {

        val animationType = intent.extras?.getString("animationType")

        when (animationType) {

            Base.ANIMATION_FADE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }
            }

            Base.ANIMATION_SLIDE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                }
            }
        }

    }

    fun finishWithAnimation(localActivity: BaseActivity) {

        val animationType = localActivity.intent.extras?.getString("animationType")

        when (animationType) {

            Base.ANIMATION_FADE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }
            }

            Base.ANIMATION_SLIDE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                }
            }
        }

        localActivity.finish()

    }


    /**
     * 需要启动广播接收者时,必须在onCreate方法中的super.onCreate()方法之前启用
     * @param action 广播接受过滤器，以及过滤动作和接受动作
     * */
    fun addBroadcastReceiver(
        actionFilter: String,
        broadType: Int,
        action: (actionContent: Context?, actionIntent: Intent?, actionFilter: String) -> Unit
    ) {
        broadcastReceiverGroup.add(BroadcastReceiverConsist(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("BaseActivity","Received!")
                action(context, intent, actionFilter)
            }
        }, IntentFilter(actionFilter),broadType))

    }

}