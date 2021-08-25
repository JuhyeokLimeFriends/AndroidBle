package com.example.bletestactivity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*


object BluetoothUtils {
    /**
     * Find characteristics of BLE
     * @param _gatt BluetoothGatt
     * @return list of found gatt characteristics
     */
    fun findBLECharacteristics(_gatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
        val matching_characteristics: MutableList<BluetoothGattCharacteristic> =
            ArrayList()
        val service_list = _gatt.services
        val service = findGattService(service_list) ?: return matching_characteristics
        val characteristicList =
            service.characteristics
        for (characteristic in characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matching_characteristics.add(characteristic)
            }
        }
        return matching_characteristics
    }

    /**
     * Find the given uuid characteristic
     * @param _gatt gatt instance
     * @param _uuid_string uuid to query as string
     * @return
     */
    fun findCharacteristic(
        _gatt: BluetoothGatt,
        _uuid_string: String
    ): BluetoothGattCharacteristic? {
        val service_list = _gatt.services
        val service = findGattService(service_list) ?: return null
        val characteristicList =
            service.characteristics
        for (characteristic in characteristicList) {
            if (matchCharacteristic(characteristic, _uuid_string)) {
                return characteristic
            }
        }
        return null
    }

    /**
     * Match the given characteristic and a uuid string
     * @param _characteristic one of found characteristic provided by the server
     * @param _uuid_string uuid as string to match
     * @return true if matched
     */
    private fun matchCharacteristic(
        _characteristic: BluetoothGattCharacteristic?,
        _uuid_string: String
    ): Boolean {
        if (_characteristic == null) {
            return false
        }
        val uuid = _characteristic.uuid
        return matchUUIDs(uuid.toString(), _uuid_string)
    }

    /**
     * Find Gatt service that matches with the server's service
     * @param _service_list list of services
     * @return matched service if found
     */
    private fun findGattService(_service_list: List<BluetoothGattService>): BluetoothGattService? {
        for (service in _service_list) {
            val service_uuid_string = service.uuid.toString()
            if (matchServiceUUIDString(service_uuid_string)) {
                return service
            }
        }
        return null
    }

    /**
     * Try to match the given uuid with the service uuid
     * @param _service_uuid_string service UUID as string
     * @return true if service uuid is matched
     */
    private fun matchServiceUUIDString(_service_uuid_string: String): Boolean {
        return matchUUIDs(_service_uuid_string, SERVICE_STRING)
    }

    /**
     * Check if there is any matching characteristic
     * @param _characteristic query characteristic
     */
    private fun isMatchingCharacteristic(_characteristic: BluetoothGattCharacteristic?): Boolean {
        if (_characteristic == null) {
            return false
        }
        val uuid = _characteristic.uuid
        return matchCharacteristicUUID(uuid.toString())
    }

    /**
     * Query the given uuid as string to the provided characteristics by the server
     * @param _characteristic_uuid_string query uuid as string
     * @return true if the matched is found
     */
    private fun matchCharacteristicUUID(_characteristic_uuid_string: String): Boolean {
        return matchUUIDs(_characteristic_uuid_string, CHARACTERISTIC_UUID)
    }

    /**
     * Try to match a uuid with the given set of uuid
     * @param _uuid_string uuid to query
     * @param _matches a set of uuid
     * @return true if matched
     */
    private fun matchUUIDs(
        _uuid_string: String,
        vararg _matches: String
    ): Boolean {
        for (match in _matches) {
            if (_uuid_string.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
