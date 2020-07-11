package com.cyan_namid09.testbluetooth_revenge

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cyan_namid09.testbluetooth_revenge.databinding.ActivityMainBinding
import java.util.*


private const val TAG = "[MAIN ACTIVITY]"
private const val REQUEST_ENABLE_BT = 1

private val UUID_SERVICE: UUID = UUID.fromString("4627f78e-7410-11ea-bc55-0242ac130003")
private val UUID_CHARACTERISTIC: UUID = UUID.fromString("b20a1840-676b-41ff-8947-7543108499d5")
private val UUID_CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

class MainActivity : AppCompatActivity() {

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled
    private var bluetoothDevice: BluetoothDevice? = null
    private val gattServer: BluetoothGattServer by lazy {
        bluetoothManager.openGattServer(applicationContext, gattServerCallback)
    }

    private lateinit var binding: ActivityMainBinding

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // アプリがBluetoothを利用可能かつ使用許可を得ていることを確認
        // 使用許可がない場合は、許可ダイアログを表示
        bluetoothAdapter.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

//        gattServer = bluetoothManager.openGattServer(applicationContext, gattServerCallback)
        val service = BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            UUID_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val descriptor = BluetoothGattDescriptor(UUID_CCCD,
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ)
        descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)
        gattServer.addService(service)

        binding.advertiseButton.setOnClickListener {
            bluetoothLeAdvertiser.startAdvertising(
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
                        Log.d(TAG, "Start Advertise")
                    }
                }
            )
        }

        binding.notifyButton.setOnClickListener {
            if (bluetoothDevice != null) {
                characteristic.setValue("count: ${++count}")
                if (gattServer.notifyCharacteristicChanged(bluetoothDevice!!, characteristic, false))
                    Log.d(TAG, "notification succeeded")
                else
                    Log.e(TAG, "notification failed")
            } else {
                Log.e(TAG, "exist null data")
            }
        }

        binding.disconnectButton.setOnClickListener {
            if (bluetoothDevice != null) {
                gattServer.cancelConnection(bluetoothDevice)
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
            count += 1
            characteristic?.setValue(count.toString())
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS , offset, characteristic?.value ?: "NONE".toByteArray())
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            Log.d(TAG, "Descriptor Read Request")
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "TEST".toByteArray())
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Characteristic Write Request")
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Descriptor Write Request")
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
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

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "succeeded to add service")
                }
                else -> {
                    Log.e(TAG, "failed to add service")
                }
            }
        }
    }
}
