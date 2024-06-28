package com.example.vacationventurepe.viewmodels.mainactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.viewmodels.BaseViewModel

class MainMineViewModel : ViewModel() {

    var showDialogLiveData:MutableLiveData<Int> = MutableLiveData()

    var refreshLocationLiveData:MutableLiveData<Boolean> = MutableLiveData()

    var showChangePasswordLiveData:MutableLiveData<Boolean> = MutableLiveData()

}