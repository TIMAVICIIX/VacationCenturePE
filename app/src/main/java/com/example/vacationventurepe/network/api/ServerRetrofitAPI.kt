package com.example.vacationventurepe.network.api

import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.basetools.PB
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ServerRetrofitAPI {

    @POST(Base.BASE_HTTP_NAMESPACE)
    fun serverParentConnection(): Call<String>

    @FormUrlEncoded
    @POST(Base.BASE_HTTP_NAMESPACE)
    fun login(
        @Field(PB.A_ACTION) type: String,
        @Field(PB.G_LOGIN_ACCOUNT) account: String,
        @Field(PB.G_LOGIN_PASSWORD) password: String
    ): Call<String>

    @GET("android/{fileName}")
    fun fetchHTML(@Path("fileName") fileName: String): Call<String>

    @FormUrlEncoded
    @POST(Base.BASE_HTTP_NAMESPACE)
    fun resetPassword(
        @Field(PB.S_SESSION) sessionID: String,
        @Field(PB.A_ACTION) action: String,
        @Field(PB.G_LOGIN_ACCOUNT) studentID: String,
        @Field(PB.G_LOGIN_PASSWORD) originPsw: String,
        @Field(PB.RS_NEW_PASSWORD) newPsw: String
    ): Call<String>

    //BS:登录后操作
    @FormUrlEncoded
    @POST(Base.BASE_HTTP_NAMESPACE)
    fun queryVacationList(
        @Field(PB.S_SESSION) sessionID: String,
        @Field(PB.A_ACTION) action: String,
        @Field(PB.QUERY_TYPE) queryType: String,
        @Field(PB.G_LOGIN_ACCOUNT) studentID: String,
        @Field(PB.G_STUDENT_CLASS_CODE) studentCode: String
    ): Call<String>

    @FormUrlEncoded
    @POST(Base.BASE_HTTP_NAMESPACE)
    fun recordOperation(
        @Field(PB.S_SESSION) sessionID:String,
        @Field(PB.A_ACTION) action: String,
        @Field(PB.RECORD_TYPE) recordType:String,
        @Field(PB.RECORD_JSON) recordJSON:String
    ):Call<String>

}