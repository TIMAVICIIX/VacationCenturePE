package com.example.vacationventurepe.network.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

@Deprecated("This Interface Deprecated by AMapAPI")
interface RegeoRetrofitAPI {

    @Headers("User-Agent: Apifox/1.0.0 (https://apifox.com)")
    @GET("v3/geocode/regeo")
    fun getLocation(
        @Query("output") outPutType:String,
        @Query("location") locationValue:String,
        @Query("key") userKey:String,
        @Query("radius") radius:String,
    ): Call<ResponseBody>

}