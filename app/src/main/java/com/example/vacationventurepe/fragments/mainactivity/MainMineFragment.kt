package com.example.vacationventurepe.fragments.mainactivity


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.vacationventurepe.R
import com.example.vacationventurepe.application.RoboApplication
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.fragments.BaseFragment
import com.example.vacationventurepe.viewmodels.GlobalViewModel
import com.example.vacationventurepe.viewmodels.mainactivity.MainMineViewModel

class MainMineFragment : BaseFragment() {

    private lateinit var globalViewModel: GlobalViewModel
    private lateinit var selfViewModel: MainMineViewModel

    private lateinit var locationTextView: TextView
    private lateinit var locationRefreshBtn: LinearLayout

    private lateinit var userIcon: ImageView

    private lateinit var userNameTextView: TextView

    private lateinit var userAccountTextView: TextView
    private lateinit var userBelongTextView: TextView
    private lateinit var userReportTextView: TextView
    private lateinit var userUnReportTextView: TextView

    private lateinit var changePswBtn: Button
    private lateinit var logoutBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mineView = inflater.inflate(R.layout.activity_main_fragment_mine, container, false)
        return mineView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initData(content: View) {

        content.apply {

            locationTextView = findViewById(R.id.main_activity_user_location_textview)
            locationRefreshBtn = findViewById(R.id.main_activity_mine_location_refresh)
            locationRefreshBtn.setOnClickListener {
                locationTextView.text = getString(R.string.loading)
                selfViewModel.refreshLocationLiveData.postValue(true)
            }

            userIcon = findViewById(R.id.user_icon)

            userAccountTextView = findViewById(R.id.main_activity_user_account_textview)
            userNameTextView = findViewById(R.id.main_activity_user_name_textview)
            userBelongTextView = findViewById(R.id.main_activity_user_belong_textview)
            userReportTextView = findViewById(R.id.main_activity_user_report_textview)
            userUnReportTextView = findViewById(R.id.main_activity_user_unreport_textview)

            changePswBtn = findViewById(R.id.main_activity_change_psw_btn)
            changePswBtn.setOnClickListener {
                selfViewModel.showChangePasswordLiveData.postValue(true)
            }



            logoutBtn = findViewById(R.id.main_activity_logout_btn)
            logoutBtn.setOnClickListener {
                selfViewModel.showDialogLiveData.postValue(Base.DIALOG_TYPE_EXIT_LOGIN)
            }


            globalViewModel =
                (requireActivity().application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]
            globalViewModel.studentEntityLiveData.observe(viewLifecycleOwner) {
                Log.d(this.javaClass.name, "Observed!")
                refreshStudentInfo(it)
            }
            globalViewModel.locationLiveData.observe(viewLifecycleOwner) {
                locationTextView.text = it
            }

            selfViewModel = ViewModelProvider(requireActivity())[MainMineViewModel::class.java]
        }

    }

    private fun refreshStudentInfo(student: Student) {

        val iconLink = Uri.parse("${Base.BASE_HTTP_LINK}/User's_Avatar/${student.studentCode}.png")
        Glide.with(this)
            .load(iconLink)
            .into(userIcon)

        student.apply {

            userNameTextView.text = studentName
            userAccountTextView.text = studentCode
            userBelongTextView.text = "${collegeName}-${className}"

            userReportTextView.text = "入学时间:${enrollTime}"
            userUnReportTextView.text = "电话号码:${telephone}"


        }

    }

}