package com.example.connector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartServiceOnBoot : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(i)
        }
    }
}