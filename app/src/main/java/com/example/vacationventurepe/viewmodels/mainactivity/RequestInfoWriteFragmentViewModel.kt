package com.example.vacationventurepe.viewmodels.mainactivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RequestInfoWriteFragmentViewModel : ViewModel() {

    var backEventLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var locationChooseLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var locationBackParamsLiveData: MutableLiveData<String> = MutableLiveData()

    var submitStatusliveData:MutableLiveData<String> = MutableLiveData()

}