package com.example.orangetracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.orangetracker.R

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnBlock: Button
    private lateinit var btnApplyLimit: Button
    private lateinit var btnDecreaseLimit: Button
    private lateinit var btnIncreaseLimit: Button
    private lateinit var etLimitValue: EditText
    private lateinit var tvDeviceName: TextView
    private lateinit var tvMacAddress: TextView
    private lateinit var tvIpAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRxSpeed: TextView
    private lateinit var tvTxSpeed: TextView
    private lateinit var tvTotalRx: TextView
    private lateinit var tvTotalTx: TextView
    private lateinit var tvLastSeen: TextView
    private lateinit var tvDeviceVendor: TextView

    private var deviceId: String? = null
    private var deviceName: String? = null
    private var deviceMac: String? = null
    private var deviceIp: String? = null
    private var deviceStatus: String? = null
    private var currentLimit = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d("DeviceDetailActivity", "=== DeviceDetailActivity onCreate ===")

            // Проверяем полученные Intent данные
            val bundle = intent.extras
            if (bundle != null) {
                Log.d("DeviceDetailActivity", "Intent bundle keys: ${bundle.keySet()}")
                for (key in bundle.keySet()) {
                    Log.d("DeviceDetailActivity", "  $key: ${bundle.get(key)}")
                }
            } else {
                Log.e("DeviceDetailActivity", "Intent bundle is NULL!")
            }

            setContentView(R.layout.activity_device_detail)
            Log.d("DeviceDetailActivity", "Layout set successfully")

            initViews()
            getIntentData()
            setupClickListeners()
            loadDeviceData()

            Log.d("DeviceDetailActivity", "=== DeviceDetailActivity initialized ===")

        } catch (e: Exception) {
            Log.e("DeviceDetailActivity", "CRITICAL ERROR in onCreate", e)
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Закрываем activity при ошибке
        }
    }
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnBlock = findViewById(R.id.btnBlock)
        btnApplyLimit = findViewById(R.id.btnApplyLimit)
        btnDecreaseLimit = findViewById(R.id.btnDecreaseLimit)
        btnIncreaseLimit = findViewById(R.id.btnIncreaseLimit)
        etLimitValue = findViewById(R.id.etLimitValue)
        tvDeviceName = findViewById(R.id.tvDeviceName)
        tvMacAddress = findViewById(R.id.tvMacAddress)
        tvIpAddress = findViewById(R.id.tvIpAddress)
        tvStatus = findViewById(R.id.tvStatus)
        tvRxSpeed = findViewById(R.id.tvRxSpeed)
        tvTxSpeed = findViewById(R.id.tvTxSpeed)
        tvTotalRx = findViewById(R.id.tvTotalRx)
        tvTotalTx = findViewById(R.id.tvTotalTx)
        tvLastSeen = findViewById(R.id.tvLastSeen)
        tvDeviceVendor = findViewById(R.id.tvDeviceVendor)
    }

    private fun getIntentData() {
        deviceId = intent.getStringExtra("DEVICE_ID")
        deviceName = intent.getStringExtra("DEVICE_NAME")
        deviceMac = intent.getStringExtra("DEVICE_MAC")
        deviceIp = intent.getStringExtra("DEVICE_IP")
        deviceStatus = intent.getStringExtra("DEVICE_STATUS")
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnBlock.setOnClickListener {
            if (btnBlock.text == "ЗАБЛОКИРОВАТЬ") {
                showBlockConfirmation()
            } else {
                showUnblockConfirmation()
            }
        }

        btnDecreaseLimit.setOnClickListener {
            currentLimit = (etLimitValue.text.toString().toIntOrNull() ?: 2).coerceAtLeast(1) - 1
            if (currentLimit < 1) currentLimit = 1
            etLimitValue.setText(currentLimit.toString())
        }

        btnIncreaseLimit.setOnClickListener {
            currentLimit = (etLimitValue.text.toString().toIntOrNull() ?: 2) + 1
            if (currentLimit > 100) currentLimit = 100
            etLimitValue.setText(currentLimit.toString())
        }

        btnApplyLimit.setOnClickListener {
            val limit = etLimitValue.text.toString().toIntOrNull() ?: 2
            applySpeedLimit(limit)
        }
    }

    private fun loadDeviceData() {
        tvDeviceName.text = deviceName ?: "iPhone XR"
        tvMacAddress.text = deviceMac ?: "AA:BB:CC:DD:EE:FF"
        tvIpAddress.text = deviceIp ?: "192.168.2.5"
        tvDeviceVendor.text = "Apple Inc."
        tvLastSeen.text = "только что"

        updateStatusDisplay()

        tvRxSpeed.text = "1.2"
        tvTxSpeed.text = "0.3"
        tvTotalRx.text = "15.2"
        tvTotalTx.text = "4.1"
    }

    private fun updateStatusDisplay() {
        when (deviceStatus) {
            "ACTIVE" -> {
                tvStatus.text = "Активен"
                tvStatus.setTextColor(getColor(R.color.status_active))
                btnBlock.text = "ЗАБЛОКИРОВАТЬ"
                btnBlock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.status_blocked)))
            }
            "BLOCKED" -> {
                tvStatus.text = "Заблокирован"
                tvStatus.setTextColor(getColor(R.color.status_blocked))
                btnBlock.text = "РАЗБЛОКИРОВАТЬ"
                btnBlock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.status_active)))
            }
            "LIMITED" -> {
                tvStatus.text = "Лимит 2 Мбит/с"
                tvStatus.setTextColor(getColor(R.color.status_limited))
                btnBlock.text = "ЗАБЛОКИРОВАТЬ"
                btnBlock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.status_blocked)))
                etLimitValue.setText("2")
            }
            else -> {
                tvStatus.text = "Не в сети"
                tvStatus.setTextColor(getColor(R.color.status_offline))
            }
        }
    }

    private fun showBlockConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.block_title)
            .setMessage("Заблокировать ${deviceName ?: "устройство"}?")
            .setPositiveButton(R.string.block_device) { _, _ ->
                deviceStatus = "BLOCKED"
                updateStatusDisplay()
                Toast.makeText(this, "Устройство заблокировано", Toast.LENGTH_SHORT).show()

                // Отправляем результат обратно в DevicesActivity
                val intent = Intent().apply {
                    putExtra("DEVICE_ID", deviceId)
                    putExtra("DEVICE_STATUS", "BLOCKED")
                }
                setResult(RESULT_OK, intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showUnblockConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.unblock_title)
            .setMessage("Разблокировать ${deviceName ?: "устройство"}?")
            .setPositiveButton(R.string.unblock_device) { _, _ ->
                deviceStatus = "ACTIVE"
                updateStatusDisplay()
                Toast.makeText(this, "Устройство разблокировано", Toast.LENGTH_SHORT).show()

                val intent = Intent().apply {
                    putExtra("DEVICE_ID", deviceId)
                    putExtra("DEVICE_STATUS", "ACTIVE")
                }
                setResult(RESULT_OK, intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun applySpeedLimit(limit: Int) {
        deviceStatus = "LIMITED"
        tvStatus.text = "Лимит $limit Мбит/с"
        tvStatus.setTextColor(getColor(R.color.status_limited))
        Toast.makeText(this, "Скорость ограничена до $limit Мбит/с", Toast.LENGTH_SHORT).show()

        val intent = Intent().apply {
            putExtra("DEVICE_ID", deviceId)
            putExtra("DEVICE_STATUS", "LIMITED")
            putExtra("SPEED_LIMIT", limit)
        }
        setResult(RESULT_OK, intent)
    }
}