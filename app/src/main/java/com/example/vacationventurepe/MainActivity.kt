package com.example.vacationventurepe

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.services.core.ServiceSettings
import com.example.vacationventurepe.application.RoboApplication
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.basetools.BaseActivity
import com.example.vacationventurepe.basetools.PB
import com.example.vacationventurepe.basetools.RoboActivity.roboStartActivityWithFinishChoose
import com.example.vacationventurepe.fragments.FragmentViewPagerAdapter
import com.example.vacationventurepe.fragments.mainactivity.MainMineFragment
import com.example.vacationventurepe.fragments.mainactivity.MainReportFragment
import com.example.vacationventurepe.network.fetchers.ServerFetcher
import com.example.vacationventurepe.viewmodels.GlobalViewModel
import com.example.vacationventurepe.viewmodels.mainactivity.MainMineViewModel
import com.example.vacationventurepe.viewmodels.mainactivity.RequestInfoWriteFragmentViewModel
import com.lljjcoder.Interface.OnCityItemClickListener
import com.lljjcoder.bean.CityBean
import com.lljjcoder.bean.DistrictBean
import com.lljjcoder.bean.ProvinceBean
import com.lljjcoder.citywheel.CityConfig
import com.lljjcoder.style.citypickerview.CityPickerView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    //DS:调试码
    private val TAG = this.javaClass.name

    private lateinit var fragmentViewPager2: ViewPager2
    private var currentPosition = 1

    private lateinit var globalViewModel: GlobalViewModel

    private lateinit var requestInfoFragmentViewModel: RequestInfoWriteFragmentViewModel
    private lateinit var mineFragmentViewModel: MainMineViewModel

    private val serverAPI = ServerFetcher()

    //DS:高德地图定位服务变量组
    private var locationClient: AMapLocationClient? = null
    private lateinit var locationOption: AMapLocationClientOption

    private val mainCityPickerView = CityPickerView().apply {
        setConfig(
            CityConfig.Builder()
                .title("选择目的地")
                .titleBackgroundColor("#FFFFFF")
                .confirTextColor("#696969")
                .cancelTextColor("#696969")
                .province("贵州省")
                .city("黔南布依族苗族自治州")
                .district("都匀市")
                .provinceCyclic(true)
                .cityCyclic(false)
                .districtCyclic(false)
                .visibleItemsCount(7)
                .build()
        )
    }

    private val buttonStatusList: MutableList<Boolean> = mutableListOf<Boolean>().apply {
        repeat(3) {
            add(false)
        }
    }

    private val buttonTouchSpaceList: MutableList<View> = mutableListOf()
    private val buttonImageSrcList: MutableList<ImageView> = mutableListOf()

    private lateinit var rootLayout: RelativeLayout
    private lateinit var centerBtnLayout: RelativeLayout

    private lateinit var historyTextView: TextView
    private lateinit var mineTextView: TextView

    private var buttonCenterOriginImageY = 0f
    private var buttonCenterAfterImageY = 0f
    private lateinit var buttonImageChangeColor: Pair<Int, Int>

    private var mainHistoryFragment =
        MainReportFragment(PB.QUERY_T_VACATION_HISTORY_ALL)//MainHistoryFragment(),被弃用
    private var mainReportFragment = MainReportFragment(PB.QUERY_T_VACATION_NOT_R)
    private var mainMineFragment = MainMineFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        backStatus = Base.ACTIVITY_BACK_TWICE

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        initData()

        synchronized(this) {
            //DS:高德地图服务授权以及初始化
            getAMapPermission()
            initAMapLocation()

            //DS:用户授权及定位
            requestPermissionAndLocation()
        }
    }

    override fun onDestroy() {
        locationClient?.onDestroy()
        super.onDestroy()
    }

    private fun initData() {

        //DS:全局ViewModel观察总线
        globalViewModel =
            (application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]

        //DS:弹出信息Fragment的ViewModel观察总线
        requestInfoFragmentViewModel =
            ViewModelProvider(this)[RequestInfoWriteFragmentViewModel::class.java]


        //DS:MineFragment的ViewModel观察总线
        mineFragmentViewModel =
            ViewModelProvider(this)[MainMineViewModel::class.java]
        mineFragmentViewModel.showDialogLiveData.observe(this) {
            when (it) {
                Base.DIALOG_TYPE_PSW -> {
                    showInfoAlertDialog(Base.DIALOG_TYPE_PSW, "修改密码", "")
                }

                Base.DIALOG_TYPE_EXIT_LOGIN -> {
                    showInfoAlertDialog(Base.DIALOG_TYPE_EXIT_LOGIN, "退出登录", "确定退出登录吗?")
                }
            }
        }
        mineFragmentViewModel.showChangePasswordLiveData.observe(this) {
            if (it) {
                showInfoAlertDialog(
                    Base.DIALOG_TYPE_PSW,
                    getString(R.string.info_change_password),
                    ""
                )
            }
        }

        //DS:为定位服务声明的通信LiveData
        mineFragmentViewModel.refreshLocationLiveData.observe(this) {
            if (it) {
                Toast.makeText(this@MainActivity, "定位刷新!", Toast.LENGTH_SHORT).show()
                locationClient?.startLocation()
            }
        }

        requestInfoFragmentViewModel.backEventLiveData.observe(this) {
            Log.d("MainActivity", "Observed!!")
            if (it) {
                showInfoAlertDialog(
                    Base.DIALOG_TYPE_NORMAL,
                    "!信息填写中!",
                    "退出将不保存该页填写草稿\n是否退出?"
                )
            } else {
                when (currentPosition) {
                    0 -> mainHistoryFragment.callBackWithParam(null)
                    1 -> mainReportFragment.callBackWithParam(null)
                }
            }
        }

        requestInfoFragmentViewModel.locationChooseLiveData.observe(this) {
            if (it) {
                mainCityPickerView.showCityPicker()
            }
        }

        requestInfoFragmentViewModel.submitStatusliveData.observe(this) {
            when (it) {
                PB.S_CONTINUE -> {
                    Toast.makeText(
                        this,
                        getString(R.string.infos_submit_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                PB.S_OVERDUE -> {
                    Toast.makeText(
                        this,
                        getString(R.string.overdue_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    runOnUiThread { exitLogin() }
                }

                Base.NETWORK_ON_FAILURE.toString() -> {
                    Toast.makeText(
                        this,
                        getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    runOnUiThread { exitLogin() }

                }
            }
        }

        buttonImageChangeColor = Pair(
            this.getColor(R.color.activity_un_choose),
            this.getColor(R.color.activity_choose)
        )

        //DEBUG
        Log.d("MainActivity", "Un_choose Color:${buttonImageChangeColor.first}")
        Log.d("MainActivity", "choose color:${buttonImageChangeColor.second}")

        buttonTouchSpaceList.apply {
            add(findViewById(R.id.main_activity_history_btn))
            add(findViewById(R.id.main_activity_report_btn))
            add(findViewById(R.id.main_activity_mine_btn))
        }


        buttonImageSrcList.apply {
            add(findViewById(R.id.main_activity_history_img))
            add(findViewById(R.id.main_activity_report_img))
            add(findViewById(R.id.main_activity_mine_img))
        }

        rootLayout = findViewById(R.id.main_container)
        centerBtnLayout = findViewById(R.id.main_activity_report_btn)

        historyTextView = findViewById(R.id.main_activity_history_text)
        mineTextView = findViewById(R.id.main_activity_mine_text)

        //默认中心图标与中心Fragment为起始页，并记住Y轴的值，初始化Y轴图标未使用的值
        buttonStatusList[1] = true
        buttonCenterOriginImageY = buttonImageSrcList[1].y
        buttonCenterAfterImageY = buttonCenterOriginImageY + 85

        fragmentViewPager2 = findViewById(R.id.main_activity_view_pager_2)
        fragmentViewPager2.adapter = FragmentViewPagerAdapter(
            this,
            listOf(
                mainHistoryFragment,
                mainReportFragment,
                mainMineFragment
            )
        )
        //设置默认页面为中心页
        fragmentViewPager2.setCurrentItem(1, false)

        fragmentViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                doChangeAction(position)
            }
        })

        //设置通过按钮切换界面
        buttonTouchSpaceList[0].setOnClickListener {
            fragmentViewPager2.setCurrentItem(0, true)
        }
        buttonTouchSpaceList[1].setOnClickListener {
            fragmentViewPager2.setCurrentItem(1, true)
        }
        buttonTouchSpaceList[2].setOnClickListener {
            fragmentViewPager2.setCurrentItem(2, true)
        }

        mainCityPickerView.setOnCityItemClickListener(object : OnCityItemClickListener() {
            override fun onSelected(
                province: ProvinceBean?,
                city: CityBean?,
                district: DistrictBean?
            ) {
                requestInfoFragmentViewModel.locationBackParamsLiveData.value =
                    "${province?.name.toString()}-${city?.name.toString()}-${district?.name.toString()}"
            }
        })
        mainCityPickerView.init(this)

    }

    //DS:高德地图服务客户端初始化
    private fun initAMapLocation() {
        try {
            locationClient = AMapLocationClient(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        locationClient?.apply {
            setLocationListener { p0 ->
                p0?.let {
                    if (it.errorCode == 0) {
                        if (it.address.isNullOrEmpty()) {
                            globalViewModel.postLocation("超出定位范围")
                            onDestroy()
                        } else {
                            globalViewModel.postLocation(it.address)
                            locationClient?.stopLocation()
                        }
                    } else {
                        Log.e(
                            TAG, "Location Service Error:\n" +
                                    "ErrorCode:${it.errorCode}\n" +
                                    "ErrorInfo:${it.errorInfo}"
                        )
                        Toast.makeText(this@MainActivity, it.errorInfo, Toast.LENGTH_LONG).show()
                        onDestroy()
                    }
                }
            }
            locationOption = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocationLatest = true
                isNeedAddress = true
                httpTimeOut = 20000
                isLocationCacheEnable = false
            }
            this.setLocationOption(locationOption)
        }
    }

    //DS:高德地图服务获取声明
    private fun getAMapPermission() {
        //DS:同意高德地图一系列服务条款与隐私政策,并插入APIKEY
        AMapLocationClient.setApiKey(Base.AMAP_KEY)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        ServiceSettings.updatePrivacyShow(this, true, true)
        ServiceSettings.updatePrivacyAgree(this, true)
    }


    //DS:MainActivity提示框方法
    /**
     * @param showType 提示类型
     * @param dialogTitle 提示标题
     * @param dialogContent 提示内容
     * Base.DIALOG_TYPE_NORMAL 普通提示框,标题必要,内容必要
     * Base.DIALOG_TYPE_EXIT_LOGIN 退出提示框,标题必要,内容必要
     * Base.DIALOG_TYPE_PSW 修改密码提示框,标题必要,内容非必要
     * **/
    private fun showInfoAlertDialog(showType: Int, dialogTitle: String, dialogContent: String) {
        //不采用系统自带AlertDialog
//        AlertDialog.Builder(this).apply {
//            setTitle("信息填写中")
//            setMessage("退出将不保存该页填写草稿\n是否退出?")
//            setPositiveButton("是")
//            { _, _ -> mainReportFragment.callBackWithParam(null) }
//            setNegativeButton("否", null)
//            show()
//        }
        when (showType) {
            Base.DIALOG_TYPE_NORMAL -> {
                val builder = AlertDialog.Builder(this)

                var alertTitleTextView: TextView
                var alertMessageTextView: TextView
                var alertPBtn: Button
                val alertNBtn: Button

                val alterLayoutView =
                    LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.alert_dialog_layout, null)
                        .apply {
                            alertTitleTextView = findViewById(R.id.alert_dialog_title_text_view)
                            alertMessageTextView =
                                findViewById(R.id.alert_dialog_message_text_view)
                            alertPBtn = findViewById(R.id.alert_dialog_positive_btn)
                            alertNBtn = findViewById(R.id.alert_dialog_negative_btn)
                        }

                val dialog = builder.setView(alterLayoutView).create().apply { show() }

                alertTitleTextView.text = dialogTitle
                alertMessageTextView.text = dialogContent
                alertPBtn.apply {
                    text = "是"
                    setOnClickListener {
                        dialog.dismiss()
                        when (currentPosition) {
                            0 -> mainHistoryFragment.callBackWithParam(null)
                            1 -> mainReportFragment.callBackWithParam(null)
                        }

                    }
                }
                alertNBtn.apply {
                    text = "否"
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }

                dialog.window?.setGravity(Gravity.CENTER)
            }

            Base.DIALOG_TYPE_EXIT_LOGIN -> {
                val builder = AlertDialog.Builder(this)

                var alertTitleTextView: TextView
                var alertMessageTextView: TextView
                var alertPBtn: Button
                val alertNBtn: Button

                val alterLayoutView =
                    LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.alert_dialog_layout, null)
                        .apply {
                            alertTitleTextView = findViewById(R.id.alert_dialog_title_text_view)
                            alertMessageTextView =
                                findViewById(R.id.alert_dialog_message_text_view)
                            alertPBtn = findViewById(R.id.alert_dialog_positive_btn)
                            alertNBtn = findViewById(R.id.alert_dialog_negative_btn)
                        }

                val dialog = builder.setView(alterLayoutView).create().apply { show() }

                alertTitleTextView.text = dialogTitle
                alertMessageTextView.text = dialogContent
                alertPBtn.apply {
                    text = "是"
                    setOnClickListener {
                        dialog.dismiss()
                        runOnUiThread { exitLogin() }
                    }
                }
                alertNBtn.apply {
                    text = "否"
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }

                dialog.window?.setGravity(Gravity.CENTER)
            }

            Base.DIALOG_TYPE_PSW -> {
                val builder = AlertDialog.Builder(this)

                val titleTextView: TextView

                val alertTipsTextView: TextView

                val originPswEditText: EditText
                val newPswEditText: EditText
                val newPswAgainEditText: EditText

                val negativeBtn: Button
                val positiveBtn: Button

                val alterLayoutView =
                    LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.alert_dialog_pws_layout, null)
                        .apply {
                            titleTextView = findViewById(R.id.alert_dialog_psw_title_text_view)
                            alertTipsTextView = findViewById(R.id.alert_dialog_psw_tips_text_view)
                            originPswEditText = findViewById(R.id.alert_dialog_origin_psw_edit_text)
                            newPswEditText = findViewById(R.id.alert_dialog_new_psw_edit_text)
                            newPswAgainEditText =
                                findViewById(R.id.alert_dialog_new_psw_again_edit_text)
                            negativeBtn = findViewById(R.id.alert_dialog_psw_negative_btn)
                            positiveBtn = findViewById(R.id.alert_dialog_psw_positive_btn)
                        }

                val dialog = builder.setView(alterLayoutView).create().apply { show() }
                titleTextView.text = dialogTitle

                negativeBtn.setOnClickListener {
                    dialog.dismiss()
                }

                positiveBtn.setOnClickListener {
                    val originPassword = originPswEditText.text.toString()
                    val newPassword = newPswEditText.text.toString()
                    val newPasswordAgain = newPswAgainEditText.text.toString()

                    if (newPassword.isEmpty() || newPasswordAgain.isEmpty() || originPassword.isEmpty()) {
                        alertTipsTextView.text = getString(R.string.info_password_not_empty)
                    } else if (newPassword != newPasswordAgain) {
                        alertTipsTextView.text = getString(R.string.info_new_password_gap_tips)

                    } else {
                        globalViewModel.sessionIDLiveData.value?.let { session ->
                            globalViewModel.studentEntityLiveData.value?.let { student ->
                                serverAPI.passwordChange(
                                    session,
                                    student.studentCode,
                                    originPassword,
                                    newPassword
                                ).observe(this@MainActivity) {
                                    when (it) {
                                        PB.S_CONTINUE -> {
                                            Toast.makeText(
                                                this,
                                                getString(R.string.change_password_sf),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            runOnUiThread { exitLogin() }
                                        }

                                        PB.RS_P_E_OP -> {
                                            alertTipsTextView.text =
                                                getString(R.string.change_password_error_op)
                                        }

                                        PB.RS_P_E_E -> {
                                            Toast.makeText(
                                                this,
                                                getString(R.string.account_changed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            runOnUiThread { exitLogin() }
                                        }

                                        PB.RS_P_E_S -> {
                                            Toast.makeText(
                                                this,
                                                getString(R.string.overdue_error),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            runOnUiThread { exitLogin() }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    //DS:使用EasyPermissions动态获取定位
    @AfterPermissionGranted(Base.AMAP_REQUEST_PERMISSION)
    private fun requestPermissionAndLocation() {
        val needPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        if (EasyPermissions.hasPermissions(this, *needPermissions)) {
            Log.d(TAG, "定位开始!")
            locationClient?.startLocation()
        } else {
            Log.d(TAG, "定位权限不足!")
            EasyPermissions.requestPermissions(
                this,
                "定位需要以下权限:",
                Base.AMAP_REQUEST_PERMISSION,
                *needPermissions
            )
        }
    }

    //DS:使用EasyPermissions的后续处理操作

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionsGranted(p0: Int, p1: MutableList<String>) {
    }

    override fun onPermissionsDenied(p0: Int, p1: MutableList<String>) {
        Toast.makeText(this, "未授权定位!", Toast.LENGTH_SHORT).show()
    }


    /**
     * 1.先对具有active状态的按钮归位,并设置状态为dead
     * 2.设置现在需要active的按钮的图片资源或者动画，并设置状态为active
     * */
    private fun doChangeAction(position: Int) {
        runOnUiThread {
            for (i in 0 until buttonStatusList.size) {
                if (i != 1 && buttonStatusList[i]) {
                    if (i == 0) {
                        Base.setTextViewColor(historyTextView, buttonImageChangeColor.first)
                    } else if (i == 2) {
                        Base.setTextViewColor(mineTextView, buttonImageChangeColor.first)
                    }

                    Base.changeNavWithAnimeOrImage(
                        buttonImageSrcList[i],
                        Base.CHANGE_SRC,
                        buttonImageChangeColor.first, null, null
                    )
                    buttonStatusList[i] = false
                } else if (i == 1 && buttonStatusList[i]) {
                    val animePair = centerNavAnime(Base.CENTER_ANIME_REDUCE)

                    Base.changeNavWithAnimeOrImage(
                        buttonImageSrcList[i],
                        Base.CHANGE_ANIME,
                        null, animePair.first, animePair.second
                    )
                    buttonStatusList[i] = false
                }
            }


            if (position != 1) {

                if (position == 0) {
                    Base.setTextViewColor(historyTextView, buttonImageChangeColor.second)
                } else if (position == 2) {
                    Base.setTextViewColor(mineTextView, buttonImageChangeColor.second)
                }


                Base.changeNavWithAnimeOrImage(
                    buttonImageSrcList[position],
                    Base.CHANGE_SRC,
                    buttonImageChangeColor.second, null, null
                )
            } else {
                val animePair = centerNavAnime(Base.CENTER_ANIME_MAGNIFY)
                Base.changeNavWithAnimeOrImage(
                    buttonImageSrcList[position],
                    Base.CHANGE_ANIME,
                    null, animePair.first, animePair.second
                )
            }
        }

        buttonStatusList[position] = true
    }

    /***
     *
     * @param status 表明了中间图标需要的缩放状态动画集,以下:
     * 放大动画集:Base.CENTER_ANIME_MAGNIFY
     * 缩小动画集:Base.CENTER_ANIME_REDUCE
     */
    private fun centerNavAnime(status: Int): Pair<ValueAnimator, ObjectAnimator> {

        val valueAnimator: ValueAnimator
        val objectAnimator: ObjectAnimator

        val density = resources.displayMetrics.density
        var startSize = 85 * density
        var endSize = 45 * density

        when (status) {
            Base.CENTER_ANIME_MAGNIFY -> {
                startSize = endSize.apply {
                    endSize = startSize
                }
            }
        }

        valueAnimator = ValueAnimator.ofFloat(startSize, endSize).apply {
            duration = 500

            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val layoutParams = buttonImageSrcList[1].layoutParams

                layoutParams.width = animatedValue.toInt()
                layoutParams.height = animatedValue.toInt()

                buttonImageSrcList[1].layoutParams = layoutParams
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val layoutParams = buttonImageSrcList[1].layoutParams
                    layoutParams.width = endSize.toInt()
                    layoutParams.height = endSize.toInt()
                    buttonImageSrcList[1].layoutParams = layoutParams
                }
            })

        }

        objectAnimator = if (startSize > endSize) {

            ObjectAnimator.ofFloat(
                buttonImageSrcList[1],
                "translationY",
                buttonCenterAfterImageY
            )

        } else {
            ObjectAnimator.ofFloat(
                buttonImageSrcList[1],
                "translationY",
                buttonCenterOriginImageY
            )
        }
        return Pair(valueAnimator, objectAnimator)


    }


    private fun exitLogin() {
        this@MainActivity.roboStartActivityWithFinishChoose<LoginActivity>(
            listOf(
                Pair(
                    "animationType",
                    Base.ANIMATION_FADE
                )
            ), null, Base.START_TYPE_FINISH
        )
    }

    //DS:方法被弃用
    private fun doExitLoginAnimation() {

        fragmentViewPager2.visibility = View.GONE

        val logoImage = buttonImageSrcList[1]

        val imageRect = Rect()
        logoImage.getGlobalVisibleRect(imageRect)

        centerBtnLayout.removeView(logoImage)

        val layoutParams = RelativeLayout.LayoutParams(logoImage.width, logoImage.height)
        layoutParams.leftMargin = imageRect.left
        layoutParams.topMargin = imageRect.top

        rootLayout.addView(logoImage, layoutParams)


        val logoTranY = logoImage.y

        val scaleXAnime = ObjectAnimator.ofFloat(logoImage, "scaleX", 1f, 2f)
        val scaleYAnime = ObjectAnimator.ofFloat(logoImage, "ScaleY", 1f, 2f)

        val tranYAnime = ObjectAnimator.ofFloat(logoImage, "translationY", 0f, logoTranY * -19f)

        val animatorSet = AnimatorSet()

        animatorSet.play(scaleXAnime).with(scaleYAnime).with(tranYAnime)
        animatorSet.duration = 800

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                runOnUiThread {
                    val shareOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        logoImage,
                        "LogoTrans"
                    )

                    this@MainActivity.roboStartActivityWithFinishChoose<LoginActivity>(
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