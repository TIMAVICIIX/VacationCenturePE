package com.example.vacationventurepe.fragments.infos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.vacationventurepe.R
import com.example.vacationventurepe.application.RoboApplication
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.basetools.PB
import com.example.vacationventurepe.entity.Venture
import com.example.vacationventurepe.entity.VentureRecord
import com.example.vacationventurepe.fragments.BaseFragment
import com.example.vacationventurepe.interfaces.CollAnimationInterface
import com.example.vacationventurepe.interfaces.FloatFragmentAnimation
import com.example.vacationventurepe.network.fetchers.ServerFetcher
import com.example.vacationventurepe.viewmodels.GlobalViewModel
import com.example.vacationventurepe.viewmodels.mainactivity.RequestInfoWriteFragmentViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoWriteFragment(
    private var startY: Int,
    private var targetVacationPosition: Int,
    private var fragmentType: String,
) : BaseFragment(), FloatFragmentAnimation, CollAnimationInterface {

    private val TAG = this.javaClass.name

    private val serverAPI = ServerFetcher()

    private lateinit var selfView: View
    private lateinit var selfViewModel: RequestInfoWriteFragmentViewModel
    private lateinit var globalViewModel: GlobalViewModel

    private lateinit var backBtn: Button

    private lateinit var infoTitleTextView: TextView
    private lateinit var infoPublishObjectTextView: TextView

    private lateinit var infoVacationCodeTextView: TextView
    private lateinit var infoVacationTypeTextView: TextView
    private lateinit var infoVacationDuringTextView: TextView
    private lateinit var infoVacationDesTextView: TextView

    private var baseInfoToggle: Boolean = true
    private lateinit var baseInfoLayout: LinearLayout
    private lateinit var baseInfoToggleBtn: ImageView

    private lateinit var liveRadioGroup: RadioGroup
    private lateinit var liveRadioBtn: RadioButton
    private lateinit var stayRadioButton: RadioButton

    private lateinit var liveTargetTextView: TextView
    private lateinit var liveTargetBtn: ImageView
    private lateinit var moreLocationEditText: EditText

    private lateinit var liveMethodEditText: EditText
    private lateinit var moreInformationEditText: EditText
    private lateinit var submitTips: TextView

    private lateinit var submitWriteBtn: Button

    private lateinit var submitDisableTips: TextView

    private var globalVacationEntity: Venture? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val infoView = inflater.inflate(R.layout.activity_main_infos_layout, container, false)

        initData(infoView)
        container?.bringToFront()

        //DS:从全局ViewModel中加载假期信息
        if (fragmentType == PB.QUERY_T_VACATION_NOT_R) {
            loadInfos()
        } else if (fragmentType == PB.QUERY_T_VACATION_HISTORY_ALL) {
            loadChangeOrOverdueItemStatus()
        }

        return infoView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selfView = view
        lifecycleScope.launch(Dispatchers.Main) {
            enterFragmentAnimation(view, startY)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDetach() {
        lifecycleScope.launch(Dispatchers.Main) {
            exitFragmentAnimation(selfView, startY)
        }
        super.onDetach()
    }

    private fun initData(context: View) {

        context.apply {

            selfViewModel =
                ViewModelProvider(requireActivity())[RequestInfoWriteFragmentViewModel::class.java]
            globalViewModel =
                (requireActivity().application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]

            backBtn = findViewById(R.id.info_layout_back_btn)
            backBtn.setOnClickListener {
                if (globalVacationEntity != null) {
                    selfViewModel.backEventLiveData.value =
                        (globalVacationEntity?.ventureState != Base.VACATION_STATE_OVERDUE && !checkIsNull())
                } else {
                    selfViewModel.backEventLiveData.value = !checkIsNull()
                }
            }
            infoTitleTextView = findViewById(R.id.info_layout_title_text_view)
            infoPublishObjectTextView = findViewById(R.id.info_layout_publish_target_text_view)

            infoVacationCodeTextView = findViewById(R.id.info_layout_vacation_code_text_view)
            infoVacationTypeTextView = findViewById(R.id.info_layout_vacation_type_text_view)
            infoVacationDuringTextView = findViewById(R.id.info_layout_vacation_during_text_view)
            infoVacationDesTextView = findViewById(R.id.info_layout_vacation_des_text_view)

            baseInfoLayout = findViewById(R.id.info_layout_base_info_layout)
            baseInfoToggleBtn = findViewById(R.id.info_layout_more_btn)
            baseInfoToggleBtn.setOnClickListener {
                if (baseInfoToggle) {
                    setCollapseAnimaGone(baseInfoLayout)
                    baseInfoToggle = false
                } else {
                    setExpandAnimaAppear(baseInfoLayout)
                    baseInfoToggle = true
                }
            }

            liveRadioGroup = findViewById(R.id.infos_live_status_raido_group)
            liveRadioGroup.check(R.id.infos_live_false_radio_btn)

            liveRadioBtn = findViewById(R.id.infos_live_false_radio_btn)
            liveRadioBtn.setOnClickListener {
                liveRadioGroup.isEnabled = true
                liveTargetTextView.isClickable = true
                liveTargetBtn.isEnabled = true
                liveMethodEditText.isEnabled = true
                moreLocationEditText.isEnabled = true
                moreInformationEditText.isEnabled = true

                liveTargetTextView.text = getString(R.string.example_location)
                liveMethodEditText.setText("")
                moreLocationEditText.setText("")
                moreInformationEditText.setText("")
            }
            stayRadioButton = findViewById(R.id.infos_live_true_radio_btn)
            stayRadioButton.setOnClickListener {
                liveRadioGroup.isEnabled = false
                liveTargetTextView.isClickable = false
                liveTargetBtn.isEnabled = false
                liveMethodEditText.isEnabled = false
                moreLocationEditText.isEnabled = false
                moreInformationEditText.isEnabled = false

                liveTargetTextView.text = getString(R.string.example_location)
                liveMethodEditText.setText(getString(R.string.live_true_text))
                moreLocationEditText.setText(getString(R.string.live_true_text))
                moreInformationEditText.setText(getString(R.string.live_true_text))
            }

            liveTargetTextView = findViewById(R.id.location_text_view)
            liveTargetTextView.setOnClickListener {
                selfViewModel.locationChooseLiveData.value = true
            }
            liveTargetBtn = findViewById(R.id.location_more_btn)
            liveTargetBtn.setOnClickListener {
                selfViewModel.locationChooseLiveData.value = true
            }

            moreLocationEditText = findViewById(R.id.location_more_edit_text)

            liveMethodEditText = findViewById(R.id.live_method_edit_text)
            moreInformationEditText = findViewById(R.id.other_infos_edit_text)

            submitWriteBtn = findViewById(R.id.infos_submit_btn)
            submitWriteBtn.setOnClickListener {
                submitOrChangeRecord()
            }

            submitTips = findViewById(R.id.infos_report_submit_tips_text_view)

            submitDisableTips = findViewById(R.id.infos_report_tips_text_view)

            selfViewModel.locationBackParamsLiveData.observe(viewLifecycleOwner) {
                liveTargetTextView.text = it
            }

        }

        //DS:初始隐藏详细信息
        setCollapseAnimaGone(baseInfoLayout)
        baseInfoToggle = false
    }

    private fun loadInfos() {

        val vacationEntity = globalViewModel.vacationLiveData.value?.get(targetVacationPosition)

        globalVacationEntity = vacationEntity

        setVacationContent()
    }

    private fun loadChangeOrOverdueItemStatus() {

        val historyVacationEntity =
            globalViewModel.historyAndRecordLiveData.value?.first?.get(targetVacationPosition)
        val recordVacationList = globalViewModel.historyAndRecordLiveData.value?.second

        globalVacationEntity = historyVacationEntity
        setVacationContent()

        historyVacationEntity?.let { venture ->

            var recordVacationEntity: VentureRecord? = null

            if (recordVacationList != null) {
                for (record in recordVacationList) {
                    if (record.ventureCode == venture.ventureCode) {
                        recordVacationEntity = record
                    }
                }
            }

            recordVacationEntity?.apply {

                try {
                    val liveDes = destination.split("##")

                    if(liveDes.size == 2){
                        liveTargetTextView.text=liveDes[0].replace(" ", "-")
                        moreLocationEditText.setText(liveDes[1])
                    }else{
                        liveTargetTextView.text= getString(R.string.example_location)
                        moreLocationEditText.setText(getString(R.string.live_true_text))
                    }
                }catch (e:Exception){
                    liveTargetTextView.text= getString(R.string.none)
                    moreLocationEditText.setText(getString(R.string.none))
                }

                try {

                    val moreDes = ventureDes.split("##")

                    liveMethodEditText.setText(
                        if (moreDes.size == 2) {
                            moreDes[1]
                        } else {
                            getString(R.string.live_true_text)
                        }
                    )

                    moreInformationEditText.setText(
                        if (moreDes.size == 2) {
                            moreDes[0]
                        } else {
                            getString(R.string.live_true_text)
                        }
                    )
                } catch (e: Exception) {
                    liveMethodEditText.setText(getString(R.string.none))
                    moreInformationEditText.setText(getString(R.string.none))
                }

                if (getString(R.string.live_false) in destination) {
                    liveRadioGroup.check(R.id.infos_live_true_radio_btn)
                    liveTargetTextView.isClickable = false
                    liveTargetBtn.isEnabled = false
                    liveMethodEditText.isEnabled = false
                    moreLocationEditText.isEnabled = false
                    moreInformationEditText.isEnabled = false
                } else {
                    liveRadioGroup.check(R.id.infos_live_false_radio_btn)
                }

            }

            if (recordVacationEntity == null) {
                moreLocationEditText.setText(getString(R.string.infos_none_record))
                liveMethodEditText.setText(getString(R.string.infos_none_record))
                moreInformationEditText.setText(getString(R.string.infos_none_record))
            }

            if (venture.ventureState == Base.VACATION_STATE_OVERDUE) {
                liveRadioGroup.isEnabled = false
                liveTargetTextView.isClickable = false
                liveTargetBtn.isEnabled = false
                liveMethodEditText.isEnabled = false
                moreLocationEditText.isEnabled = false
                moreInformationEditText.isEnabled = false

                liveRadioBtn.isClickable = false
                stayRadioButton.isClickable = false


                submitWriteBtn.visibility = View.GONE

                submitDisableTips.text = getString(R.string.infos_disable_tips)
            }


        }

    }

    private fun submitOrChangeRecord() {

        val studentEntity = globalViewModel.studentEntityLiveData.value

        studentEntity?.let { student ->
            globalVacationEntity?.let { vacation ->

                val destination =
                    if (liveRadioGroup.checkedRadioButtonId == R.id.infos_live_false_radio_btn) {
                        "${getString(R.string.live_true)}##${liveTargetTextView.text}##${moreLocationEditText.text}"
                    } else {
                        getString(R.string.live_false)
                    }

                val des =
                    if (liveRadioGroup.checkedRadioButtonId == R.id.infos_live_false_radio_btn) {
                        "${moreInformationEditText.text}##${liveMethodEditText.text}"
                    } else {
                        ""
                    }

                val ventureRecord = VentureRecord(
                    student.studentCode,
                    vacation.ventureCode,
                    vacation.ventureName,
                    student.studentName,
                    destination,
                    des
                )

                val recordType = if (fragmentType == PB.QUERY_T_VACATION_NOT_R) {
                    PB.RECORD_OPERATE_SAVE
                } else {
                    PB.RECORD_OPERATE_CHANGE
                }

                if (destination != getString(R.string.live_false)) {
                    if (moreInformationEditText.text.toString() == "" || moreLocationEditText.text.toString() == "") {
                        submitTips.visibility = View.VISIBLE
                        return
                    }
                }

                globalViewModel.sessionIDLiveData.value?.let {
                    serverAPI.recordOperation(
                        it,
                        recordType,
                        Gson().toJson(ventureRecord)
                    ).observe(viewLifecycleOwner) { status ->
                        selfViewModel.submitStatusliveData.postValue(status)
                    }
                }

            }
        }

    }

    private fun setVacationContent() {
        globalVacationEntity?.apply {

            infoTitleTextView.text = ventureName

            infoPublishObjectTextView.text =
                "${ventureBelongCollege}-${ventureBelongSpec}-${ventureBelongClass}"
            infoVacationCodeTextView.text = ventureCode
            infoVacationTypeTextView.text = ventureType
            infoVacationDesTextView.text = ventureDes
            infoVacationDuringTextView.text = "${ventureStartDate}-${ventureEndDate}"
        }
    }

    private fun checkIsNull(): Boolean {
        return moreLocationEditText.text.toString() == "" && liveMethodEditText.text.toString() == "" && moreInformationEditText.text.toString() == ""
    }

}