package com.example.orangetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.orangetracker.adapter.DeviceAdapter
import com.example.orangetracker.model.Device
import com.example.orangetracker.model.DeviceStatus

class DevicesActivity : AppCompatActivity() {

    // View элементы
    private lateinit var btnBack: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var listViewDevices: ListView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnScanAgain: Button

    // Фильтры
    private lateinit var chipAll: Button
    private lateinit var chipActive: Button
    private lateinit var chipBlocked: Button
    private lateinit var chipLimited: Button

    // Статистика
    private lateinit var tvTotalCount: TextView
    private lateinit var tvActiveCount: TextView
    private lateinit var tvBlockedCount: TextView

    // Адаптер и данные
    private var deviceAdapter: DeviceAdapter? = null
    private val devices = mutableListOf<Device>() // ОРИГИНАЛЬНЫЙ список всех устройств
    private var currentFilter: String = "all"
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private val REQUEST_CODE_DEVICE_DETAIL = 1001
    private val TAG = "DevicesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        setContentView(R.layout.activity_devices)

        initViews()
        setupClickListeners()
        setupSearch()
        loadDemoDevices()
        setupDeviceList()
        updateStatistics()
        applyFilter() // Применяем фильтр после загрузки

        Log.d(TAG, "onCreate completed")
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)
        etSearch = findViewById(R.id.etSearch)
        listViewDevices = findViewById(R.id.listViewDevices)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnScanAgain = findViewById(R.id.btnScanAgain)

        chipAll = findViewById(R.id.chipAll)
        chipActive = findViewById(R.id.chipActive)
        chipBlocked = findViewById(R.id.chipBlocked)
        chipLimited = findViewById(R.id.chipLimited)

        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvActiveCount = findViewById(R.id.tvActiveCount)
        tvBlockedCount = findViewById(R.id.tvBlockedCount)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnRefresh.setOnClickListener {
            refreshDeviceList()
        }

        btnScanAgain.setOnClickListener {
            refreshDeviceList()
        }

        chipAll.setOnClickListener {
            updateFilterStyles(chipAll)
            currentFilter = "all"
            applyFilter()
            updateStatistics()
        }

        chipActive.setOnClickListener {
            updateFilterStyles(chipActive)
            currentFilter = "active"
            applyFilter()
            updateStatistics()
        }

        chipBlocked.setOnClickListener {
            updateFilterStyles(chipBlocked)
            currentFilter = "blocked"
            applyFilter()
            updateStatistics()
        }

        chipLimited.setOnClickListener {
            updateFilterStyles(chipLimited)
            currentFilter = "limited"
            applyFilter()
            updateStatistics()
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    applyFilter()
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupDeviceList() {
        Log.d(TAG, "setupDeviceList started")

        deviceAdapter = DeviceAdapter(
            context = this,
            devices = devices, // Передаем ссылку на оригинальный список
            onDeviceClickListener = { device ->
                try {
                    Log.d(TAG, "Clicked on device: ${device.name} (Status: ${device.status})")
                    val intent = Intent(this, DeviceDetailActivity::class.java).apply {
                        putExtra("DEVICE_ID", device.id)
                        putExtra("DEVICE_NAME", device.name)
                        putExtra("DEVICE_MAC", device.mac)
                        putExtra("DEVICE_IP", device.ip)
                        putExtra("DEVICE_STATUS", device.status.name)
                    }
                    startActivityForResult(intent, REQUEST_CODE_DEVICE_DETAIL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error clicking device", e)
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onDeviceBlockListener = { device ->
                blockDevice(device)
            },
            onDeviceUnblockListener = { device ->
                unblockDevice(device)
            },
            onDeviceLimitListener = { device, limit ->
                limitDevice(device, limit)
            }
        )

        listViewDevices.adapter = deviceAdapter
        Log.d(TAG, "setupDeviceList completed")
    }

    private fun updateFilterStyles(selectedChip: Button) {
        val chips = listOf(chipAll, chipActive, chipBlocked, chipLimited)
        chips.forEach { chip ->
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
        }

        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedChip.setTextColor(resources.getColor(android.R.color.white, theme))
    }

    private fun loadDemoDevices() {
        Log.d(TAG, "loadDemoDevices started")

        devices.clear()
        devices.addAll(
            listOf(
                Device(
                    id = "1",
                    name = "iPhone XR",
                    mac = "AA:BB:CC:DD:EE:01",
                    ip = "192.168.2.5",
                    vendor = "Apple",
                    status = DeviceStatus.ACTIVE,
                    currentRxMbps = 1.2,
                    currentTxMbps = 0.3,
                    totalRxGb = 15.2,
                    totalTxGb = 4.1
                ),
                Device(
                    id = "2",
                    name = "MacBook Pro",
                    mac = "AA:BB:CC:DD:EE:02",
                    ip = "192.168.2.8",
                    vendor = "Apple",
                    status = DeviceStatus.BLOCKED,
                    currentRxMbps = 0.0,
                    currentTxMbps = 0.0,
                    totalRxGb = 45.8,
                    totalTxGb = 12.3
                ),
                Device(
                    id = "3",
                    name = "Smart TV",
                    mac = "AA:BB:CC:DD:EE:03",
                    ip = "192.168.2.12",
                    vendor = "Samsung",
                    status = DeviceStatus.LIMITED,
                    speedLimit = 2,
                    currentRxMbps = 0.5,
                    currentTxMbps = 0.1,
                    totalRxGb = 32.1,
                    totalTxGb = 5.7
                ),
                Device(
                    id = "4",
                    name = "iPhone 13",
                    mac = "AA:BB:CC:DD:EE:04",
                    ip = "192.168.2.15",
                    vendor = "Apple",
                    status = DeviceStatus.ACTIVE,
                    currentRxMbps = 2.1,
                    currentTxMbps = 0.8,
                    totalRxGb = 28.4,
                    totalTxGb = 8.9
                ),
                Device(
                    id = "5",
                    name = "iPad",
                    mac = "AA:BB:CC:DD:EE:05",
                    ip = "192.168.2.20",
                    vendor = "Apple",
                    status = DeviceStatus.ACTIVE,
                    currentRxMbps = 0.8,
                    currentTxMbps = 0.2,
                    totalRxGb = 52.6,
                    totalTxGb = 15.3
                ),
                Device(
                    id = "6",
                    name = "Робот-пылесос",
                    mac = "AA:BB:CC:DD:EE:06",
                    ip = "192.168.2.25",
                    vendor = "Xiaomi",
                    status = DeviceStatus.OFFLINE,
                    currentRxMbps = 0.0,
                    currentTxMbps = 0.0,
                    totalRxGb = 0.5,
                    totalTxGb = 0.2
                )
            )
        )

        Log.d(TAG, "loadDemoDevices completed, loaded ${devices.size} devices")
        // Логируем начальные статусы
        devices.forEach { device ->
            Log.d(TAG, "Initial device: ${device.name} - ${device.status}")
        }
    }

    private fun refreshDeviceList() {
        Toast.makeText(this, "Обновление списка устройств...", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            updateStatistics()
            applyFilter()
            Toast.makeText(this, "Список обновлен", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    private fun applyFilter() {
        val searchText = etSearch.text.toString().lowercase()

        // Логируем текущие статусы перед фильтрацией
        Log.d(TAG, "=== APPLY FILTER ===")
        Log.d(TAG, "Current filter: $currentFilter")
        Log.d(TAG, "Devices in original list: ${devices.size}")
        devices.forEach { device ->
            Log.d(TAG, "  ${device.name}: ${device.status}")
        }

        // Фильтруем по статусу
        val filteredByStatus = when (currentFilter) {
            "active" -> devices.filter { it.status == DeviceStatus.ACTIVE }
            "blocked" -> devices.filter { it.status == DeviceStatus.BLOCKED }
            "limited" -> devices.filter { it.status == DeviceStatus.LIMITED }
            else -> devices.filter { it.status != DeviceStatus.OFFLINE } // "all" - показываем все кроме OFFLINE
        }

        Log.d(TAG, "After status filter: ${filteredByStatus.size} devices")

        // Фильтруем по поиску
        val finalFiltered = if (searchText.isNotEmpty()) {
            filteredByStatus.filter { device ->
                device.name.lowercase().contains(searchText) ||
                        device.ip.contains(searchText) ||
                        device.mac.lowercase().contains(searchText) ||
                        (device.vendor?.lowercase()?.contains(searchText) == true)
            }
        } else {
            filteredByStatus
        }

        Log.d(TAG, "After search filter: ${finalFiltered.size} devices")

        // Обновляем адаптер
        deviceAdapter?.updateDevices(finalFiltered)

        // Показываем/скрываем пустое состояние
        if (finalFiltered.isEmpty()) {
            listViewDevices.visibility = android.view.View.GONE
            layoutEmpty.visibility = android.view.View.VISIBLE

            val message = when (currentFilter) {
                "active" -> "Нет активных устройств"
                "blocked" -> "Нет заблокированных устройств"
                "limited" -> "Нет устройств с ограничением"
                else -> "Нет устройств в сети"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            listViewDevices.visibility = android.view.View.VISIBLE
            layoutEmpty.visibility = android.view.View.GONE
        }
    }

    private fun updateStatistics() {
        val total = devices.size
        val active = devices.count { it.status == DeviceStatus.ACTIVE }
        val blocked = devices.count { it.status == DeviceStatus.BLOCKED }
        val limited = devices.count { it.status == DeviceStatus.LIMITED }
        val offline = devices.count { it.status == DeviceStatus.OFFLINE }

        Log.d(TAG, "=== STATISTICS ===")
        Log.d(TAG, "Total: $total, Active: $active, Blocked: $blocked, Limited: $limited, Offline: $offline")

        tvTotalCount.text = when (total) {
            1 -> "$total устройство"
            2, 3, 4 -> "$total устройства"
            else -> "$total устройств"
        }

        tvActiveCount.text = when (active) {
            1 -> "$active активное"
            2, 3, 4 -> "$active активных"
            else -> "$active активных"
        }

        tvBlockedCount.text = when (blocked) {
            1 -> "$blocked заблокировано"
            else -> "$blocked заблокировано"
        }
    }

    private fun blockDevice(device: Device) {
        AlertDialog.Builder(this)
            .setTitle("Блокировка устройства")
            .setMessage("Заблокировать ${device.name}? Устройство потеряет доступ к интернету.")
            .setPositiveButton("ЗАБЛОКИРОВАТЬ") { _, _ ->
                Log.d(TAG, "Blocking device: ${device.name}")

                // Находим устройство в оригинальном списке и обновляем статус
                val originalDevice = devices.find { it.id == device.id }
                originalDevice?.status = DeviceStatus.BLOCKED

                Log.d(TAG, "New status for ${originalDevice?.name}: ${originalDevice?.status}")

                // Обновляем статистику и фильтр
                updateStatistics()
                applyFilter()

                Toast.makeText(this, "${device.name} заблокирован", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun unblockDevice(device: Device) {
        AlertDialog.Builder(this)
            .setTitle("Разблокировка устройства")
            .setMessage("Разблокировать ${device.name}? Доступ к интернету будет восстановлен.")
            .setPositiveButton("РАЗБЛОКИРОВАТЬ") { _, _ ->
                Log.d(TAG, "Unblocking device: ${device.name}")

                // Находим устройство в оригинальном списке и обновляем статус
                val originalDevice = devices.find { it.id == device.id }
                originalDevice?.status = DeviceStatus.ACTIVE

                Log.d(TAG, "New status for ${originalDevice?.name}: ${originalDevice?.status}")

                // Обновляем статистику и фильтр
                updateStatistics()
                applyFilter()

                Toast.makeText(this, "${device.name} разблокирован", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun limitDevice(device: Device, limit: Int) {
        Log.d(TAG, "Limiting device: ${device.name} to $limit Mbps")

        // Находим устройство в оригинальном списке и обновляем статус
        val originalDevice = devices.find { it.id == device.id }
        originalDevice?.status = DeviceStatus.LIMITED
        originalDevice?.speedLimit = limit

        Log.d(TAG, "New status for ${originalDevice?.name}: ${originalDevice?.status}")

        // Обновляем статистику и фильтр
        updateStatistics()
        applyFilter()

        Toast.makeText(this, "Скорость ${device.name} ограничена до $limit Мбит/с", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_DEVICE_DETAIL && resultCode == RESULT_OK) {
            val deviceId = data?.getStringExtra("DEVICE_ID")
            val newStatus = data?.getStringExtra("DEVICE_STATUS")
            val speedLimit = data?.getIntExtra("SPEED_LIMIT", 0)

            Log.d(TAG, "onActivityResult: deviceId=$deviceId, newStatus=$newStatus, speedLimit=$speedLimit")

            deviceId?.let { id ->
                // Обновляем статус в оригинальном списке
                val device = devices.find { it.id == id }
                device?.let {
                    val oldStatus = it.status
                    it.status = when (newStatus) {
                        "BLOCKED" -> DeviceStatus.BLOCKED
                        "LIMITED" -> DeviceStatus.LIMITED
                        else -> DeviceStatus.ACTIVE
                    }
                    if (newStatus == "LIMITED" && speedLimit != null) {
                        it.speedLimit = speedLimit
                    }

                    Log.d(TAG, "Device ${it.name} status changed: $oldStatus -> ${it.status}")

                    // Обновляем отображение
                    updateStatistics()
                    applyFilter()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - reapplying filter")
        applyFilter()
        updateStatistics()
    }
}