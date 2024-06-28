package com.example.vacationventurepe.test

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class CredentialsManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("userPreds", Context.MODE_PRIVATE)

    fun saveCredentials(username: String, password: String) {
        val editor = preferences.edit()
        editor.putString("Username", username)
        editor.putString("Password", password)
        editor.apply()
    }

    fun getCredentials(): Pair<String?,String?> {
        val savedUsername = preferences.getString("Username", null)
        val savedPassword = preferences.getString("Password", null)

        return Pair(savedUsername,savedPassword)
    }

    //密码散列算法，与SQL存储方式不契合，被弃用
//    private fun hashPassword(password: String): String {
//        val digest = MessageDigest.getInstance("SHA-256")
//        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
//        return hash.joinToString("") { "%02x".format(it) }
//    }
}