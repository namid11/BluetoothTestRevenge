package com.cyan_namid09.testbluetooth_revenge

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import com.cyan_namid09.testbluetooth_revenge.databinding.ActivityMainBinding
import java.util.*

private const val TAG = "[MAIN ACTIVITY]"
private const val REQUEST_ENABLE_BT = 1

val UUID_SERVICE: UUID = UUID.fromString("4627f78e-7410-11ea-bc55-0242ac130003")
val UUID_CHARACTERISTIC: UUID = UUID.fromString("b20a1840-676b-41ff-8947-7543108499d5")

class MainActivity : AppCompatActivity() {

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled
    private var bluetoothDevice: BluetoothDevice? = null
    private var gattServer: BluetoothGattServer? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        gattServer = bluetoothManager.openGattServer(this, gattServerCallback)
        val service = BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            UUID_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        ).apply {
            setValue("HELLO BLUETOOTH")
        }
        val descriptor = BluetoothGattDescriptor(UUID_CHARACTERISTIC, BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ)
        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)

        binding.connectButton.setOnClickListener {
            val bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            bluetoothLeAdvertiser?.startAdvertising(
                AdvertiseSettings.Builder().build(),
                AdvertiseData.Builder().apply {
                    addServiceUuid(ParcelUuid(UUID_SERVICE))
                }.build(),
                object : AdvertiseCallback() {
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
            )
        }

        binding.extraButton.setOnClickListener {
            if (bluetoothDevice != null && gattServer != null) {
                gattServer!!.cancelConnection(bluetoothDevice)
            } else {
                Log.e(TAG, "exist null data")
            }
        }

        binding.sendButton.setOnClickListener {
            if (gattServer != null && bluetoothDevice != null) {
                characteristic.setValue("HELLO BLUETOOTH")
                if (gattServer!!.notifyCharacteristicChanged(bluetoothDevice!!, characteristic, false))
                    Log.d(TAG, "notification succeeded")
                else
                    Log.e(TAG, "notification failed")
            } else {
                Log.e(TAG, "exist null data")
            }
        }

    }


    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "STATE CONNECTED")
                    bluetoothDevice = device
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "STATE DISCONNECTED")
                    bluetoothDevice = null
                }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            Log.d(TAG, "Characteristic Read Request")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS , offset, characteristic?.value ?: "WWW".toByteArray())
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            Log.d(TAG, "Descriptor Read Request")
            gattServer?.sendResponse(device, requestId, BluetoothProfile.STATE_CONNECTED, offset, null)
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Characteristic Write Request")
            gattServer?.sendResponse(device, requestId, BluetoothProfile.STATE_CONNECTED, offset, value)
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Descriptor Write Request")
            gattServer?.sendResponse(device, requestId, BluetoothProfile.STATE_CONNECTED, offset, value)
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "sent operation succeeded")
                }
                else -> {
                    Log.d(TAG, "sent operation failed")
                }
            }
        }
    }
}
