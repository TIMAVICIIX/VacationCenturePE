package com.example.vacationventurepe.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.entity.Venture
import com.example.vacationventurepe.entity.VentureRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class GlobalViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = javaClass.name

    val studentEntityLiveData: MutableLiveData<Student> = MutableLiveData()

    val sessionIDLiveData: LiveData<String> get() = sessionIDMutableLiveData
    private val sessionIDMutableLiveData: MutableLiveData<String> = MutableLiveData()

    val locationLiveData: LiveData<String> get() = locationMutableLiveData
    private val locationMutableLiveData: MutableLiveData<String> = MutableLiveData()

    val vacationLiveData: LiveData<List<Venture>> get() = vacationMutableLiveData
    private val vacationMutableLiveData: MutableLiveData<List<Venture>> = MutableLiveData()

    //DS:被解构LiveData替代
//    val historyVacationLiveData: LiveData<List<Venture>> get() = historyMutableLiveData
//    private val historyMutableLiveData: MutableLiveData<List<Venture>> = MutableLiveData()

    val historyAndRecordLiveData: LiveData<Pair<List<Venture>, List<VentureRecord>>> get() = historyAndRecordMutableLiveData
    private val historyAndRecordMutableLiveData: MutableLiveData<Pair<List<Venture>, List<VentureRecord>>> =
        MutableLiveData()

    fun postSessionID(id: String) {
//        DEBUG
        if (id != sessionIDMutableLiveData.value) {
            Log.d(TAG, "Global注册会话ID中!")
            sessionIDMutableLiveData.postValue(id)
        }
    }

    fun postLocation(address: String) {
        if (address != locationMutableLiveData.value) {
            locationMutableLiveData.postValue(address)
        }
    }


    fun postVacationList(vacationList: List<Venture>) {

        Log.d(TAG, "假期列表刷新:\n")
        vacationList.forEach {
            Log.d(TAG, "假期号:${it.ventureCode}\n")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val resultVentures =
                vacationList.sortedByDescending {
                    LocalDate.parse(
                        it.ventureStartDate,
                        dateFormatter
                    )
                }

            vacationMutableLiveData.postValue(resultVentures)
        } else {
            vacationMutableLiveData.postValue(vacationList)
        }


    }

    fun postHistoryVacationAndRecordList(
        vacationList: List<Venture>,
        recordList: List<VentureRecord>
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val resultVacations =
                vacationList.sortedByDescending {
                    LocalDate.parse(
                        it.ventureStartDate,
                        dateFormatter
                    )
                }

            historyAndRecordMutableLiveData.postValue(Pair(resultVacations, recordList))
        } else {
            historyAndRecordMutableLiveData.postValue(Pair(vacationList, recordList))
        }


    }
}