package com.example.vacationventurepe.fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vacationventurepe.basetools.BaseActivity
import com.example.vacationventurepe.fragments.mainactivity.MainHistoryFragment
import com.example.vacationventurepe.fragments.mainactivity.MainMineFragment
import com.example.vacationventurepe.fragments.mainactivity.MainReportFragment

class FragmentViewPagerAdapter(activity: BaseActivity,val fragmentList:List<BaseFragment>) : FragmentStateAdapter(activity) {



    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

}