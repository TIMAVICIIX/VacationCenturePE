package com.example.vacationventurepe.network.fetchers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vacationventurepe.basetools.Base
import com.example.vacationventurepe.basetools.Base.Companion.toStringOrBlank
import com.example.vacationventurepe.entity.Student
import com.example.vacationventurepe.network.api.ServerRetrofitAPI
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.example.vacationventurepe.basetools.PB
import com.example.vacationventurepe.interfaces.RootGsonAnalyser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.CountDownLatch

private const val TAG = "ServerFetcher"

class ServerFetcher : ViewModel(), RootGsonAnalyser {

    private val serverRetrofitAPI: ServerRetrofitAPI
    private var sessionID: String? = null

    init {
        val okhttpClientWithInterceptor = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Base.BASE_HTTP_LINK)
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okhttpClientWithInterceptor)
            .build()

        serverRetrofitAPI = retrofit.create(ServerRetrofitAPI::class.java)
    }

    fun fetchContentsTest(): LiveData<String> {

        val responseLiveData: MutableLiveData<String> = MutableLiveData()
        val serverRequest: Call<String> = serverRetrofitAPI.serverParentConnection()

        serverRequest.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed to get info's", t)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "Get Successful")
                responseLiveData.value = response.body()
            }
        })

        return responseLiveData
    }

    fun loginService(
        account: String,
        password: String,
        setSession: ((sessionID: String) -> Unit)
    ): MutableLiveData<Any> {

        val loginErrorResponseLiveData: MutableLiveData<String> = MutableLiveData()
        val loginSuccessResponseLiveData: MutableLiveData<Student> = MutableLiveData()

        val resultResponseLiveData: MediatorLiveData<Any> = MediatorLiveData()

        serverRetrofitAPI
            .login(PB.A_LOGIN_TEST, account, password)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {
                        //DEBUG
                        //Log.d("Response Body", response.body().toString())

                        val testStatus = if (response.body().toString() != "") {
                            JsonParser.parseString(response.body()).asJsonObject.get(PB.R_STATUS).asString
                        } else {
                            ""
                        }
                        if (testStatus != PB.S_CONTINUE) {
                            loginErrorResponseLiveData.postValue(testStatus)
                        } else {
                            serverRetrofitAPI
                                .login(PB.A_LOGIN, account, password)
                                .enqueue(object : Callback<String> {
                                    override fun onResponse(
                                        call: Call<String>,
                                        response: Response<String>
                                    ) {
                                        if (response.isSuccessful) {
                                            loginSuccessResponseLiveData
                                                .postValue(
                                                    Gson().fromJson(
                                                        response.body(),
                                                        Student::class.java
                                                    )
                                                )


                                            //DS:注册会话ID
                                            sessionID =
                                                extractSessionId(response.headers().toString())
                                            sessionID?.let { setSession(sessionID!!) }

                                            Log.d(
                                                this@ServerFetcher.javaClass.name,
                                                "Header:${response.headers()}"
                                            )
                                            Log.d(
                                                this@ServerFetcher.javaClass.name,
                                                "Session ID:${sessionID}"
                                            )

                                        } else {
                                            loginErrorResponseLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        loginErrorResponseLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                                    }
                                })
                        }
                    } else {
                        loginErrorResponseLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                    }

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    loginErrorResponseLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                }

            })

        resultResponseLiveData.addSource(loginSuccessResponseLiveData) {
            resultResponseLiveData.value = it
        }
        resultResponseLiveData.addSource(loginErrorResponseLiveData) {
            resultResponseLiveData.value = it
        }
        return resultResponseLiveData

    }

    /**
     * @return第一项为隐私政策，第二项为服务条款
     * **/
    fun fetchHTML(): MutableLiveData<List<Pair<String, String>>> {

        val htmlURLs = listOf(Base.NETWORK_PRIVACY_POLICY, Base.NETWORK_TERMS_OF_SERVICE)

        val resultMutableList: MutableList<Pair<String, String>> = mutableListOf()
        val resultMutableLiveData: MutableLiveData<List<Pair<String, String>>> = MutableLiveData()

        val latch = CountDownLatch(htmlURLs.size)

        for (htmlURL in htmlURLs) {

            serverRetrofitAPI.fetchHTML(htmlURL).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        response.body()?.let {

                            val htmlUri = call.request().url.toString()
                            val htmlTitle = Jsoup.parse(it).title()

//                            Log.d(this.javaClass.name, "URI:${htmlUri};Title:${htmlTitle}")

                            resultMutableList.add(Pair(htmlUri, htmlTitle))
                        }
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    latch.countDown()
                }
            })
        }

        viewModelScope.launch(Dispatchers.IO) {
            latch.await()
            withContext(Dispatchers.Main) {
                resultMutableLiveData.value = resultMutableList.toList()
            }
        }
        return resultMutableLiveData

    }

    fun passwordChange(
        sessionID: String,
        studentCode: String,
        originPassword: String,
        newPassword: String
    ): MutableLiveData<String> {

        val responseLiveData: MutableLiveData<String> = MutableLiveData()

        serverRetrofitAPI
            .resetPassword(sessionID, PB.A_RESET_PSW, studentCode, originPassword, newPassword)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    responseLiveData.postValue(
                        if (response.body().toString() != "") {
                            JsonParser.parseString(response.body()).asJsonObject.get(PB.R_STATUS).asString
                        } else {
                            ""
                        }
                    )
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    responseLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                }
            })

        return responseLiveData
    }

    fun fetchVacationList(
        sessionID: String,
        fetchType: String,
        studentCode: String,
        classCode: String,
        responseResolver: (responseBody: String) -> Unit,
        failureResolver: () -> Unit,
        otherResolver: () -> Unit
    ) {

        serverRetrofitAPI
            .queryVacationList(sessionID, PB.A_QUERY_VACATION, fetchType, studentCode, classCode)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val gson = Gson()
                        val responseBody = response.body().toStringOrBlank()

                        responseResolver(responseBody)

                    } else {
                        otherResolver()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    failureResolver()
                }
            })
    }

    fun recordOperation(
        sessionID: String,
        recordType: String,
        recordJSON: String
    ): MutableLiveData<String> {

        val resultMutableLiveData: MutableLiveData<String> = MutableLiveData()

        serverRetrofitAPI
            .recordOperation(sessionID, PB.A_RECORD_OPERATION, recordType, recordJSON)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val status =
                            JsonParser.parseString(response.body()).asJsonObject.get(PB.R_STATUS).asString
                        resultMutableLiveData.postValue(status)
                    } else {
                        resultMutableLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    resultMutableLiveData.postValue(Base.NETWORK_ON_FAILURE.toString())
                }
            })

        return resultMutableLiveData

    }

}