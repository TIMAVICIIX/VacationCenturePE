package com.example.vacationventurepe.interfaces

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

interface RootGsonAnalyser {

    fun getRootKeyAndValue(targetString: String): Pair<String, String> {

        val jsonObject = JsonParser.parseString(targetString).asJsonObject
        var result: Pair<String, String> = Pair("", "")

        for (entry: Map.Entry<String, JsonElement> in jsonObject.entrySet()) {
            result = Pair(entry.key, entry.value.toString())
        }

        return result
    }

    fun extractSessionId(header: String):String?  {
        return header.split(';').find { it.trim().startsWith("Set-Cookie: JSESSIONID=") }
            ?.substringAfter("Set-Cookie: JSESSIONID=")
    }


}