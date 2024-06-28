package com.example.vacationventurepe.network.fetchers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.network.api.RegeoRetrofitAPI
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

@Deprecated("This Class Deprecated by AMap API")
class LocationFetcher {

    private val regeoRetrofitAPI: RegeoRetrofitAPI

    private val TAG = this.javaClass.name

    init {

        val okHttpClient = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://restapi.amap.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        regeoRetrofitAPI = retrofit.create(RegeoRetrofitAPI::class.java)
    }

    fun convertLocation(userKey: String, location: String): LiveData<String> {

        val resultLiveData: MutableLiveData<String> = MutableLiveData()

        regeoRetrofitAPI.getLocation(
            "JSON",
            location,
            userKey,
            "200"
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    //DEBUG
                    Log.d(TAG, response.body().toString())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                resultLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
            }
        })

        return resultLiveData

    }

}