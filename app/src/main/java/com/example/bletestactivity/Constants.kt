package com.example.bletestactivity

import android.Manifest
import java.text.SimpleDateFormat
import java.util.*
const val DEVICE_NAME = "name"
const val DEVICE_ADDRESS = "address"
const val CYCLE_TYPE = "cycleType"
const val SCREEN_MESSAGE = "lcdMessage"
const val CYCLE_HOUR = "CycleHour"
const val CYCLE_MIN = "CycleMin"
const val CYCLE_NUMBER_PICKER_INDEX = "CycleNumberPickerIndex"
const val IS_ON_OFF_CHECKED= "isONOFFChecked"
const val DEVICE_ID = "device id"

val sdfTime = SimpleDateFormat("HH:mm")

// used to identify adding bluetooth names
const val REQUEST_ENABLE_BT = 3054

// used to request fine location permission
const val REQUEST_FINE_LOCATION = 3055

val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
)

// scan period in milliseconds
const val SCAN_PERIOD = 3000

//사용자 BLE UUID Service/Rx/Tx
const val SERVICE_STRING = "0000FFF0-0000-1000-8000-00805f9b34fb"
//테스트 디바이스
//const val SERVICE_DEVICE_ADDRESS = "E4:FB:59:C1:CA:24"
//상용화 디바이스 
//const val SERVICE_DEVICE_ADDRESS = "FF:E4:53:94:0C:EB"
const val SERVICE_DEVICE_ADDRESS = "F3:4D:85:21:84:73"
val SERVICE_UUID = UUID.fromString(SERVICE_STRING);
const val CHARACTERISTIC_UUID = "0000FFF1-0000-1000-8000-00805f9b34fb"


//BluetoothGattDescriptor 고정
const val CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb"

const val ACTION_GATT_CONNECTED = "com.limefriends.lny.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.limefriends.lny.ACTION_GATT_DISCONNECTED"
const val ACTION_STATUS_MSG = "com.limefriends.lny.ACTION_STATUS_MSG"
const val ACTION_READ_DATA= "com.limefriends.lny.ACTION_READ_DATA"
const val EXTRA_DATA = "com.limefriends.lny.EXTRA_DATA"
const val MSG_DATA = "com.limefriends.lny.MSG_DATA"

val numberPickerValues  = arrayOf("10", "20", "30" ,"60")