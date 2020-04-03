package com.cyan_namid09.testbluetooth_revenge

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log

private const val TAG = "ADVERTISE CALLBACK"

class MyAdvertiseCallback(): AdvertiseCallback() {
    override fun onStartFailure(errorCode: Int) {
        Log.e(TAG, "Error Code: $errorCode")
        when (errorCode) {
            ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> Log.e(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS")
            ADVERTISE_FAILED_ALREADY_STARTED -> Log.e(TAG, "ADVERTISE_FAILED_ALREADY_STARTED")
            ADVERTISE_FAILED_DATA_TOO_LARGE -> Log.e(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE")
            ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> Log.e(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED")
            ADVERTISE_FAILED_INTERNAL_ERROR -> Log.e(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR")
        }
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        Log.d(TAG, "Start Success")
    }
}