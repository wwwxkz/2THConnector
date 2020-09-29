package com.example.connector

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class Connector {
    fun post(json: String, position: ArrayList<String>){
        val url = "http://192.168.1.12/2THPlatform/api/v1/report/send/?company=2th&password=123&user=connector&mac="+position[2]+"&lat="+position[0]+"&lon="+position[1]+"&tel"+position[3]
        val headerHttp = MediaType.parse("application/json; charset=utf-8")
        val client = OkHttpClient()
        var body = RequestBody.create(headerHttp, json)
        var request = Request.Builder().url(url).post(body).build()
        val response = client.newCall(request).execute()
    }
}