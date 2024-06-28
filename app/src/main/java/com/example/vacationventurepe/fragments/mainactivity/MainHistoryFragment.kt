package com.example.vacationventurepe.fragments.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.vacationventurepe.R
import com.example.vacationventurepe.databinding.ActivityMainFragmentHistoryBinding
import com.example.vacationventurepe.fragments.BaseFragment
import com.example.vacationventurepe.viewmodels.mainactivity.MainHistoryViewModel


/**
 *
 * 已被弃用，用原有ReportFragment代替
 *
 * **/
@Deprecated("已被抽象为MainReportFragment")
class MainHistoryFragment : BaseFragment() {

    private var binding: ActivityMainFragmentHistoryBinding? = null

    private val bindGet get() = binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val historyView = inflater.inflate(R.layout.activity_main_fragment_history,container,false)

        return historyView

    }

}