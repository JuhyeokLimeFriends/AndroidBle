package com.example.bletestactivity

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.example.bletestactivity.CHARACTERISTIC_UUID
import com.example.bletestactivity.CONFIG_UUID
import com.example.bletestactivity.SCAN_PERIOD
import com.example.bletestactivity.BluetoothUtils.findBLECharacteristics
import com.example.bletestactivity.BluetoothUtils.findCharacteristic

private val TAG = "gattClienCallback"

class DeviceControlActivity(
    private val context: Context?,
    private var bluetoothGatt: BluetoothGatt?
) {
    private var device: BluetoothDevice? = null

    // @@@@@@@@@@@@@@@@@@@@@ 데이터 전송 @@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(
                        TAG, "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    disconnectGattServer()
                }
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Connected to GATT_SUCCESS.")
                    broadcastUpdate("Connected " + device?.name)
                    // @@@@@@@@@@@@@@@@@@@@@ 데이터 전송 @@@@@@@@@@@@@@@@@@@@@@@
                    val cmd_characteristic =
                        findCharacteristic(gatt!!, CHARACTERISTIC_UUID)
                    // disconnect if the characteristic is not found
                    if (cmd_characteristic == null) {
                        Log.e(TAG, "Unable to find cmd characteristic")
                        disconnectGattServer()
                        return
                    }

                    var cmdBytes = byteArrayOf(0x02, 0xFA.toByte(), 0x02, 0xA2.toByte(), 0x09)
                    cmd_characteristic.value = cmdBytes
                    cmd_characteristic.writeType =
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

                    val success = gatt!!.writeCharacteristic(cmd_characteristic)
                    // check the result
                    if (success) {
                        Log.d(TAG, "Success to write command")
                    } else {
                        Log.d(TAG, "Failed to write command : " + cmd_characteristic.uuid)
                        disconnectGattServer()
                    }
                    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                }
                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                    broadcastUpdate("Fail Connect " + device?.name)
                }
            }
        }

        private fun broadcastUpdate(str: String) {
            val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                }
            }
            mHandler.obtainMessage().sendToTarget()
        }

        private fun disconnectGattServer() {
            Log.d(TAG, "Closing Gatt connection")
            // disconnect and close the gatt
            if (bluetoothGatt != null) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }

    fun connectGatt(device: BluetoothDevice): BluetoothGatt? {
        this.device = device

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(
                context, false, gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        }
        return bluetoothGatt
    }
}