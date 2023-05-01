package com.ntduc.baseproject.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import javax.inject.Inject

/**
 * Created by DucNT
 */

/*
* <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
*/
class Network @Inject constructor(val context: Context) : NetworkConnectivity {
    @SuppressLint("MissingPermission")
    override fun getNetworkInfo(): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    override fun isConnected(): Boolean {
        val info = getNetworkInfo()
        return info != null && info.isConnected
    }
}

interface NetworkConnectivity {
    fun getNetworkInfo(): NetworkInfo?
    fun isConnected(): Boolean
}