package com.example.bletestactivity

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bletestactivity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1

    // Bluetooth Device Scan을 위한 Permission 확인을 위한 변수 선언
    private val REQUEST_ALL_PERMISSION = 2
    private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    // Bluetooth Device Scan을 위한 변수 선언
    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 2000
    private val handler = Handler()

    // RecyclerView를 통해 스캔한 기기 리스트 보여주기 위한 변수 선언
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var binding: ActivityMainBinding? = null

    class RecyclerViewAdapter(private val myDataset: ArrayList<BluetoothDevice>) :
        RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
        class MyViewHolder(val linearView: LinearLayout) : RecyclerView.ViewHolder(linearView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            // create a new view
            val linearView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false) as LinearLayout

            return MyViewHolder(linearView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemName: TextView = holder.linearView.findViewById(R.id.item_name)
            val itemAddress: TextView = holder.linearView.findViewById(R.id.item_address)
            itemName.text = myDataset[position].name
            itemAddress.text = myDataset[position].address
        }

        override fun getItemCount(): Int = myDataset.size
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Scan Button Visibility
        // Init BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            // Device doesn't support Bluetooth
            binding?.bleOnOffBtn?.isChecked = bluetoothAdapter?.isEnabled == false
        }
        binding?.bleOnOffBtn?.setOnClickListener {
            bluetoothOnOff()
            binding?.scanBtn?.visibility = if (binding?.scanBtn?.visibility == View.VISIBLE) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }

        // bluetooth Scan Button Click Event
        binding?.scanBtn?.setOnClickListener { v: View? ->
            if (!hasPermission(this, PERMISSIONS))
                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
            scanDevice(true)
        }

        // RecyclerView
        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter = RecyclerViewAdapter(devicesArr)
        binding?.recyclerView?.layoutManager =viewManager
        binding?.recyclerView?.adapter = recyclerViewAdapter

    }

    fun bluetoothOnOff() {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("BluetoothAdapter", "Device doesn't support Bluetooth")
        } else {
            if (bluetoothAdapter?.isEnabled == false) {// 블루투스 꺼져 있으면 => 활성화
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            } else {
                bluetoothAdapter?.disable()
            }
        }
    }

    // App에서 Permission이 제대로 등록되었는지 검사하는 함수
    private fun hasPermission(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                )
                    return false
            }
        }
        return true
    }

    // Permission 확인
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show()
                else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permission Must Be Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Bluetooth Device Scan 하기
    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let {
                // results is not null
                for (result in it) {
                    if (!devicesArr.contains(result.device) && result.device.name != null) devicesArr.add(
                        result.device
                    )
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null
                if (!devicesArr.contains(it.device) && it.device.name != null) devicesArr.add(it.device)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * scanDevice라는 함수를 통해서 매개변수 state가 true이면 handler를 이용해 Bluetooth Scan을 SCAN_PERIOD 동안 실행하고 false이면, Scanning을 멈춘다.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevice(state: Boolean) = if (state) {
        handler.postDelayed({
            scanning = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD.toLong())
        scanning = true
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)

    } else {
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
    }
}

