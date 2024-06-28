package com.example.vacationventurepe.fragments.mainactivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.vacationventurepe.R
import com.example.vacationventurepe.application.RoboApplication
import com.example.vacationventurepe.basetools.PB
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.entity.Venture
import com.example.vacationventurepe.entity.VentureRecord
import com.example.vacationventurepe.fragments.BaseFragment
import com.example.vacationventurepe.fragments.infos.InfoWriteFragment
import com.example.vacationventurepe.interfaces.MessageSender
import com.example.vacationventurepe.interfaces.OnItemClickListener
import com.example.vacationventurepe.network.fetchers.ServerFetcher
import com.example.vacationventurepe.recycleview.report.ReportAdapter
import com.example.vacationventurepe.viewmodels.GlobalViewModel
import com.example.vacationventurepe.viewmodels.mainactivity.RequestInfoWriteFragmentViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class MainReportFragment(private val fragmentType: String) : BaseFragment(), MessageSender {

    private val TAG = javaClass.name

    private lateinit var requestInfoWriteFragmentViewModel: RequestInfoWriteFragmentViewModel
    private lateinit var globalViewModel: GlobalViewModel

    private val serverAPI = ServerFetcher()

    private lateinit var itemRecycleView: RecyclerView
    private lateinit var itemRecycleViewLayoutManager: LayoutManager

    private lateinit var smartRefreshLayout: SmartRefreshLayout

    private var childInfoFragmentY = 0
    private lateinit var infoFrameLayout: View
    private var infoFragment:InfoWriteFragment = InfoWriteFragment(0,0,"")
    private var recycleViewLock = false

    private lateinit var emptyTipsTextView: TextView
    private lateinit var bottomTipsTextView: TextView

    //DS:全局数据变量缓存
    private lateinit var sessionId: String
    private lateinit var studentEntity: Student

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val reportView = inflater.inflate(R.layout.activity_main_fragment_report, container, false)



        smartRefreshLayout = reportView.findViewById(R.id.report_fragment_smart_refresh_layout)
        smartRefreshLayout.setRefreshHeader(
            BezierRadarHeader(context).setEnableHorizontalDrag(true)
        )
        smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            fetchVacationList()
            globalViewModel.vacationLiveData.observe(viewLifecycleOwner) {
                refreshLayout.finishRefresh()
            }
        }

        emptyTipsTextView = reportView.findViewById(R.id.mine_fragment_empty_tips_text_view)
        bottomTipsTextView = reportView.findViewById(R.id.mine_fragment_bottom_tips_text_view)

        itemRecycleView = reportView.findViewById(R.id.report_fragment_recycleView)
        itemRecycleView.setHasFixedSize(true)

        itemRecycleViewLayoutManager = LinearLayoutManager(context)
        itemRecycleView.layoutManager = itemRecycleViewLayoutManager

        //Start to init Fragment case
        infoFrameLayout = reportView.findViewById(R.id.infos_fragment)

        itemRecycleView.adapter = setReCycleViewAdapter(null)


//        setRecycleAlphaListener(itemRecycleView)

        return reportView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestInfoWriteFragmentViewModel =
            ViewModelProvider(requireActivity())[RequestInfoWriteFragmentViewModel::class.java]
        requestInfoWriteFragmentViewModel.submitStatusliveData.observe(viewLifecycleOwner) {
            if (it == PB.S_CONTINUE) {
                this.callBackWithParam(null)
                fetchVacationList()
            }
        }

        globalViewModel =
            (requireActivity().application as RoboApplication).viewModelProvider[GlobalViewModel::class.java]
        if (fragmentType == PB.QUERY_T_VACATION_NOT_R) {
            globalViewModel.vacationLiveData.observe(viewLifecycleOwner) {
                itemRecycleView.adapter = setReCycleViewAdapter(it)
            }
        } else if (fragmentType == PB.QUERY_T_VACATION_HISTORY_ALL) {
            globalViewModel.historyAndRecordLiveData.observe(viewLifecycleOwner) {
                itemRecycleView.adapter = setReCycleViewAdapter(it.first)
            }
        }
        globalViewModel.studentEntityLiveData.observe(viewLifecycleOwner) {
            studentEntity = it
        }
        globalViewModel.sessionIDLiveData.observe(viewLifecycleOwner) {
            sessionId = it
            //DS:开始假期请求
            Log.d(TAG, "开始请求假期列表!")
            fetchVacationList()
        }
    }

    override fun callBackWithParam(param: Any?) {
        childFragmentManager.beginTransaction().apply {
            remove(infoFragment)
            commit()
        }
//        infoFrameLayout.visibility = View.INVISIBLE
        recycleViewLock = false
    }

    private fun fetchVacationList() {

        //DEBUG
//        Log.d(
//            TAG, "开始请求:\n" +
//                    "Session:${globalViewModel.sessionIDLiveData.value}\n" +
//                    "请求类型:${fetchType}\n" +
//                    "学生号:${globalViewModel.studentEntityLiveData.value?.studentCode}\n" +
//                    "班级号:${globalViewModel.studentEntityLiveData.value?.classCode}\n"
//        )

        serverAPI.fetchVacationList(
            globalViewModel.sessionIDLiveData.value!!,
            fragmentType,
            globalViewModel.studentEntityLiveData.value?.studentCode!!,
            globalViewModel.studentEntityLiveData.value?.classCode!!,
            {
                val gson = Gson()
                if (fragmentType == PB.QUERY_T_VACATION_NOT_R) {
                    try {
                        synchronized(this@MainReportFragment) {
                            val ventureList: MutableList<Venture> =
                                gson.fromJson(
                                    it,
                                    object : TypeToken<MutableList<Venture>>() {}.type
                                )
                            ventureList.removeAt(ventureList.size - 1)

                            setBottomState(ventureList)

                            globalViewModel.postVacationList(ventureList)
                        }
                        //DEBUG
                        //Log.d(TAG, "接受假期列表\n转换中,源JSON:${response.body()}")
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (fragmentType == PB.QUERY_T_VACATION_HISTORY_ALL) {

                    synchronized(this@MainReportFragment) {
                        val historyVacationString = it.split(PB.QUERY_T_VACATION_HISTORY_BLENDER)[0]
                        val historyRecordString = it.split(PB.QUERY_T_VACATION_HISTORY_BLENDER)[1]

                        val historyVentureList: MutableList<Venture> =
                            gson.fromJson(
                                historyVacationString,
                                object : TypeToken<MutableList<Venture>>() {}.type
                            )
                        historyVentureList.removeAt(historyVentureList.size - 1)

                        val historyRecordList: MutableList<VentureRecord> =
                            gson.fromJson(
                                historyRecordString,
                                object : TypeToken<MutableList<VentureRecord>>() {}.type
                            )
                        historyRecordList.removeAt(historyRecordList.size - 1)

                        globalViewModel.postHistoryVacationAndRecordList(
                            historyVentureList,
                            historyRecordList
                        )
                        setBottomState(historyVentureList)
                    }

                }
            },
            {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }, {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.unknown_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun setReCycleViewAdapter(vacationList: List<Venture>?): ReportAdapter {

        val paramList = if (vacationList.isNullOrEmpty()) {
            listOf()
        } else {
            vacationList
        }

        return ReportAdapter(
            object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    if (!recycleViewLock) {

                        val location = IntArray(2)
                        view.getLocationOnScreen(location)
                        childInfoFragmentY = location[1]
                        //                        infoFrameLayout.visibility = View.VISIBLE
                        infoFragment = InfoWriteFragment(
                            childInfoFragmentY,
                             position,
                            this@MainReportFragment.fragmentType
                        )
                        recycleViewLock = true

                        childFragmentManager.beginTransaction().apply {
                            add(R.id.infos_fragment, infoFragment)
                            commit()
                        }
                    }
                }
            }, requireContext(), paramList
        )

    }

    private fun setBottomState(targetList: List<*>) {
        if (targetList.isEmpty()) {
            bottomTipsTextView.visibility = View.GONE
            emptyTipsTextView.visibility = View.VISIBLE
        } else {
            bottomTipsTextView.visibility = View.VISIBLE
            emptyTipsTextView.visibility = View.GONE
        }
    }

    fun setRecycleAlphaListener(targetRecycleView: RecyclerView) {

        targetRecycleView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager: LinearLayoutManager =
                    recyclerView.layoutManager as LinearLayoutManager

                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                for (i in firstVisiblePosition..lastVisiblePosition) {

                    val visibleViewHolder = recyclerView.findViewHolderForAdapterPosition(i)

                    visibleViewHolder?.let {

                        val minAlpha = 0.3f
                        val maxAlpha = 1f

                        val nowAlpha =
                            maxAlpha - (i / (recyclerView.adapter!!.itemCount).toFloat()) * (maxAlpha - minAlpha)

                        it.itemView.alpha = nowAlpha
                    }

                }

            }
        })

    }

}