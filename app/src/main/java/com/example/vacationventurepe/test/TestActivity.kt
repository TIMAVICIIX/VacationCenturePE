package com.example.vacationventurepe.test

import android.os.Bundle
import com.example.vacationventurepe.R
import com.example.vacationventurepe.basetools.BaseActivity
import com.example.vacationventurepe.network.fetchers.ServerFetcher

class TestActivity:BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)

        val serverLiveData = ServerFetcher().fetchContentsTest()
        serverLiveData.observe(this){

        }
    }

}