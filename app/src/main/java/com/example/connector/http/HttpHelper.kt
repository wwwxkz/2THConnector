package com.example.connector.http

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class HttpHelper {
    fun post(json: String){
        val url = "http://localhost/2THPlatform/api/v1/report/send/"
        val headerHttp = MediaType.parse("application/json; charset=utf-8")
        val client = OkHttpClient()
        var body = RequestBody.create(headerHttp, json)
        var request = Request.Builder().url(url).post(body).build()
        val response = client.newCall(request).execute()
    }
}