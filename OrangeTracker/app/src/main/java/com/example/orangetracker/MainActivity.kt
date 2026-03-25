package com.example.orangetracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import com.example.orangetracker.ui.TrafficChartView

class MainActivity : AppCompatActivity() {
    private lateinit var panelOverview: ScrollView
    private lateinit var panelDevices: ScrollView
    private lateinit var panelAttacks: ScrollView
    private lateinit var panelSettings: ScrollView

    private lateinit var deviceSpinner: Spinner
    private lateinit var trafficChartView: TrafficChartView
    private var selectedDeviceIndex = 0

    private lateinit var connDot: View
    private lateinit var connText: TextView

    private lateinit var inPackets: TextView
    private lateinit var outPackets: TextView
    private lateinit var inPacketsHint: TextView
    private lateinit var outPacketsHint: TextView
    private lateinit var activeAlerts: TextView

    private lateinit var natMode: TextView
    private lateinit var natPublicAddr: TextView
    private lateinit var natPool: TextView

    private lateinit var eventsContainer: LinearLayout
    private lateinit var devicesContainer: LinearLayout
    private lateinit var alertsContainer: LinearLayout
    private lateinit var flowsContainer: LinearLayout

    private lateinit var detectState: TextView
    private lateinit var thresholdInput: EditText
    private lateinit var btnStartDetect: Button
    private lateinit var btnStopDetect: Button
    private lateinit var btnApplyThreshold: Button

    private lateinit var baseUrlInput: EditText
    private lateinit var refreshMsInput: EditText
    private lateinit var logBox: TextView
    private lateinit var btnSaveBaseUrl: Button
    private lateinit var btnSaveRefresh: Button

    private lateinit var btnOverview: Button
    private lateinit var btnDevices: Button
    private lateinit var btnAttacks: Button
    private lateinit var btnSettings: Button

    private val handler = Handler(Looper.getMainLooper())
    private var cycle = 0
    private var refreshMs = 1500L
    private var threshold = 75
    private var runningDetection = false

    private val pointsCount = 12
    private val devices = listOf(
        Device(
            name = "Клиент ПК-1",
            ip = "192.168.0.10",
            mac = "a0:b1:c2:d3:e4:f0",
            role = "Клиент",
            status = "Активен"
        ),
        Device(
            name = "Точка NAT",
            ip = "192.168.0.1",
            mac = "00:11:22:33:44:55",
            role = "NAT",
            status = "Активен"
        ),
        Device(
            name = "Модуль ПАК",
            ip = "192.168.0.25",
            mac = "d0:ad:be:ef:00:25",
            role = "ПАК",
            status = "Активен"
        ),
        Device(
            name = "Клиент ПК-2",
            ip = "192.168.0.33",
            mac = "aa:bb:cc:dd:ee:ff",
            role = "Клиент",
            status = "Ограничен"
        )
    )

    private val updateRunnable: Runnable = object : Runnable {
        override fun run() {
            updateMockTrafficUI()
            handler.postDelayed(this, refreshMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupDeviceSpinner()
        setupNavigation()
        setupControls()

        // Initial render.
        connDot.setBackgroundColor(0xFF6B7A99.toInt())
        connText.text = "Подключение..."
        refreshMockStateUI()

        // Start periodic mock updates (backend wiring can replace this later).
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, refreshMs)
    }

    private fun bindViews() {
        panelOverview = findViewById(R.id.panelOverview)
        panelDevices = findViewById(R.id.panelDevices)
        panelAttacks = findViewById(R.id.panelAttacks)
        panelSettings = findViewById(R.id.panelSettings)

        deviceSpinner = findViewById(R.id.deviceSpinner)
        trafficChartView = findViewById(R.id.trafficChartView)

        connDot = findViewById(R.id.connDot)
        connText = findViewById(R.id.connText)

        inPackets = findViewById(R.id.inPackets)
        outPackets = findViewById(R.id.outPackets)
        inPacketsHint = findViewById(R.id.inPacketsHint)
        outPacketsHint = findViewById(R.id.outPacketsHint)
        activeAlerts = findViewById(R.id.activeAlerts)

        natMode = findViewById(R.id.natMode)
        natPublicAddr = findViewById(R.id.natPublicAddr)
        natPool = findViewById(R.id.natPool)

        eventsContainer = findViewById(R.id.eventsContainer)
        devicesContainer = findViewById(R.id.devicesContainer)
        alertsContainer = findViewById(R.id.alertsContainer)
        flowsContainer = findViewById(R.id.flowsContainer)

        detectState = findViewById(R.id.detectState)
        thresholdInput = findViewById(R.id.thresholdInput)
        btnStartDetect = findViewById(R.id.btnStartDetect)
        btnStopDetect = findViewById(R.id.btnStopDetect)
        btnApplyThreshold = findViewById(R.id.btnApplyThreshold)

        baseUrlInput = findViewById(R.id.baseUrlInput)
        refreshMsInput = findViewById(R.id.refreshMsInput)
        logBox = findViewById(R.id.logBox)
        btnSaveBaseUrl = findViewById(R.id.btnSaveBaseUrl)
        btnSaveRefresh = findViewById(R.id.btnSaveRefresh)

        btnOverview = findViewById(R.id.btnOverview)
        btnDevices = findViewById(R.id.btnDevices)
        btnAttacks = findViewById(R.id.btnAttacks)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupDeviceSpinner() {
        val names = devices.map { d -> "${d.name} (${d.ip})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter

        deviceSpinner.setSelection(selectedDeviceIndex)
        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDeviceIndex = position
                renderTrafficSnapshot()
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun setupNavigation() {
        btnOverview.setOnClickListener { showPanel(Panel.OVERVIEW) }
        btnDevices.setOnClickListener { showPanel(Panel.DEVICES) }
        btnAttacks.setOnClickListener { showPanel(Panel.ATTACKS) }
        btnSettings.setOnClickListener { showPanel(Panel.SETTINGS) }
    }

    private fun setupControls() {
        btnStartDetect.setOnClickListener {
            runningDetection = true
            detectState.text = "Состояние: детекция запущена"
            log("Запуск детекции (UI-заглушка)")
            refreshAlertsUI()
            refreshFlowsUI()
        }
        btnStopDetect.setOnClickListener {
            runningDetection = false
            detectState.text = "Состояние: остановлена"
            log("Остановка детекции (UI-заглушка)")
            alertsContainer.removeAllViews()
            flowsContainer.removeAllViews()
        }
        btnApplyThreshold.setOnClickListener {
            threshold = parseIntOrDefault(thresholdInput.text?.toString(), 75)
            val detectionStateText = if (runningDetection) "детекция запущена" else "остановлена"
            detectState.text = "Состояние: $detectionStateText (порог=$threshold)"
            log("Порог применён: $threshold (UI-заглушка)")
            refreshAlertsUI()
            refreshFlowsUI()
        }
        btnSaveBaseUrl.setOnClickListener {
            val url = baseUrlInput.text?.toString()?.trim().orEmpty()
            val safeUrl = if (url.isBlank()) "-" else url
            log("Base URL сохранён: $safeUrl (UI-заглушка)")
        }
        btnSaveRefresh.setOnClickListener {
            refreshMs = parseLongOrDefault(refreshMsInput.text?.toString(), 1500L).coerceIn(500L, 60000L)
            log("Период обновления: ${refreshMs}мс (UI-заглушка)")
            handler.removeCallbacks(updateRunnable)
            handler.postDelayed(updateRunnable, refreshMs)
        }
    }

    private fun showPanel(panel: Panel) {
        val (ov, dev, att, set) = when (panel) {
            Panel.OVERVIEW -> arrayOf(true, false, false, false)
            Panel.DEVICES -> arrayOf(false, true, false, false)
            Panel.ATTACKS -> arrayOf(false, false, true, false)
            Panel.SETTINGS -> arrayOf(false, false, false, true)
        }
        panelOverview.visibility = if (ov) View.VISIBLE else View.GONE
        panelDevices.visibility = if (dev) View.VISIBLE else View.GONE
        panelAttacks.visibility = if (att) View.VISIBLE else View.GONE
        panelSettings.visibility = if (set) View.VISIBLE else View.GONE
    }

    private fun refreshMockStateUI() {
        // NAT placeholders
        natMode.text = "Режим: -"
        natPublicAddr.text = "Публичный адрес: -"
        natPool.text = "Пул: -"

        // Empty containers start.
        eventsContainer.removeAllViews()
        devicesContainer.removeAllViews()
        alertsContainer.removeAllViews()
        flowsContainer.removeAllViews()

        refreshDevicesUI()
        refreshAlertsUI()
        refreshFlowsUI()

        renderTrafficSnapshot()

        detectState.text = "Состояние: -"
        thresholdInput.setText(threshold.toString())
        refreshMsInput.setText(refreshMs.toString())
        baseUrlInput.setText("http://192.168.0.1:8080")
        log("UI загружен. Используйте вкладки для просмотра (мок-данные).")
    }

    private fun refreshDevicesUI() {
        devicesContainer.removeAllViews()
        devices.forEach { d ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "${d.name}\n${d.ip}\n${d.role} / ${d.status}"
            }
            val btn = Button(this).apply {
                val isBlocked = d.status.lowercase().contains("огранич")
                text = if (isBlocked) "Разблокировать" else "Заблокировать"
                setOnClickListener { log("UI-заглушка: действие для ${d.ip}") }
            }
            row.addView(tv)
            row.addView(btn)
            devicesContainer.addView(row)
        }
    }

    private fun renderTrafficSnapshot() {
        val device = devices.getOrNull(selectedDeviceIndex) ?: devices.first()
        val (inSeries, outSeries) = buildTrafficSeries(device, cycle)

        trafficChartView.setSeries(inSeries, outSeries)
        inPackets.text = formatInt(inSeries.lastOrNull() ?: 0)
        outPackets.text = formatInt(outSeries.lastOrNull() ?: 0)
        inPacketsHint.text = "за цикл #$cycle"
        outPacketsHint.text = "за цикл #$cycle"
    }

    private fun buildTrafficSeries(device: Device, cycle: Int): Pair<IntArray, IntArray> {
        val ipHash = kotlin.math.abs(device.ip.hashCode())
        val baseIn = 900 + (ipHash % 220)
        val baseOut = 980 + (ipHash % 200)
        val ampIn = 160 + (ipHash % 140)
        val ampOut = 190 + (ipHash % 160)
        val phase = (ipHash % 100) / 12.0

        val inSeries = IntArray(pointsCount)
        val outSeries = IntArray(pointsCount)

        for (i in 0 until pointsCount) {
            val t = cycle + i
            val inVal = (baseIn + ampIn * kotlin.math.sin((t / 2.0) + phase) + ((t * 37) % 180)).toInt()
            val outVal = (baseOut + ampOut * kotlin.math.cos((t / 3.0) + phase) + ((t * 29) % 160)).toInt()
            inSeries[i] = maxOf(0, inVal)
            outSeries[i] = maxOf(0, outVal)
        }

        return Pair(inSeries, outSeries)
    }

    private fun refreshAlertsUI() {
        alertsContainer.removeAllViews()
        if (!runningDetection) return

        val count = (cycle % 3) + 1
        for (i in 0 until count) {
            val score = (40 + (cycle * 7 + i * 13) % 60).coerceIn(1, 99)
            val severity = when {
                score >= 85 -> Severity.DANGER
                score >= 60 -> Severity.WARN
                else -> Severity.OK
            }
            val title = when (i) {
                0 -> "сканирование"
                1 -> "аномальный поток"
                else -> "необычная активность"
            }

            val tv = TextView(this).apply {
                setPadding(12, 12, 12, 12)
                setBackgroundColor(
                    when (severity) {
                        Severity.OK -> 0xFF1B5E20.toInt()
                        Severity.WARN -> 0xFF8D6E00.toInt()
                        Severity.DANGER -> 0xFFB71C1C.toInt()
                    }
                )
                setTextColor(0xFFFFFFFF.toInt())
                text = "Предупреждение: $title\nscore=$score"
            }
            alertsContainer.addView(tv)
        }
    }

    private fun refreshFlowsUI() {
        flowsContainer.removeAllViews()
        if (!runningDetection) return

        val flows = listOf(
            Flow("192.168.0.33", "192.168.0.25", "TCP", 92),
            Flow("192.168.0.10", "192.168.0.1", "UDP", 74),
            Flow("192.168.0.62", "192.168.0.25", "TCP", 58),
            Flow("192.168.0.44", "192.168.0.1", "ICMP", 81),
        )
        flows.take(3 + (cycle % 2)).forEach { f ->
            val tv = TextView(this).apply {
                setPadding(12, 10, 12, 10)
                text = "${f.src} -> ${f.dst}  | ${f.proto}  | уверенность ${f.conf}%"
            }
            flowsContainer.addView(tv)
        }
    }

    private fun updateMockTrafficUI() {
        cycle++

        renderTrafficSnapshot()

        val alerts = (cycle % 3) + 1
        activeAlerts.text = alerts.toString()

        // NAT mock: change mode slightly.
        val mode = if (cycle % 2 == 0) "симметричный" else "конусообразный"
        natMode.text = "Режим: $mode"
        natPublicAddr.text = "Публичный адрес: 203.0.113.${(10 + cycle) % 200}"
        natPool.text = "Пул: 203.0.113.${(40 + cycle * 2) % 200} - 203.0.113.${(120 + cycle * 3) % 200}"

        val ok = cycle % 10 != 0
        connText.text = if (ok) "Подключено" else "Нет связи (мок-данные)"
        connDot.setBackgroundColor(if (ok) 0xFF35D07F.toInt() else 0xFFFF4D4D.toInt())

        // Append one event.
        addEventRow(cycle)

        // Keep devices panel occasionally refreshed.
        if (cycle % 5 == 0) {
            if (panelDevices.visibility == View.VISIBLE) refreshDevicesUI()
        }

        // Update attacks lists while running.
        if (runningDetection && panelAttacks.visibility == View.VISIBLE) {
            refreshAlertsUI()
            refreshFlowsUI()
        }
    }

    private fun addEventRow(cycle: Int) {
        if (eventsContainer.childCount > 20) {
            eventsContainer.removeViewAt(0)
        }

        val time = java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())
        val item = listOf(
            EventItem("NAT", "Таблица трансляций обновлена"),
            EventItem("Трафик", "Профиль активности изменился"),
            EventItem("Безопасность", "Подозрительная активность замечена"),
            EventItem("Сеть", "Добавлено/обновлено устройство"),
        )[cycle % 4]

        val severity = when (cycle % 3) {
            0 -> Severity.OK
            1 -> Severity.WARN
            else -> Severity.DANGER
        }

        val tv = TextView(this).apply {
            setPadding(12, 10, 12, 10)
            setBackgroundColor(
                when (severity) {
                    Severity.OK -> 0xFF123A1D.toInt()
                    Severity.WARN -> 0xFF3F3400.toInt()
                    Severity.DANGER -> 0xFF3F0B0B.toInt()
                }
            )
            setTextColor(0xFFFFFFFF.toInt())
            text = "$time\n${item.type}: ${item.desc}"
        }
        eventsContainer.addView(tv, 0)

        // If panel is hidden, keep it light (still add row, but UI updates are periodic anyway).
    }

    private fun log(message: String) {
        val ts = java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())
        val prev = logBox.text?.toString().orEmpty()
        val next = if (prev.isBlank() || prev == "-") "$ts [info] $message\n" else "$prev$ts [info] $message\n"
        logBox.text = next.takeLast(3500)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    private fun formatInt(x: Int): String = java.text.NumberFormat.getInstance(java.util.Locale("ru-RU")).format(x)

    private fun parseIntOrDefault(s: String?, def: Int): Int = s?.toIntOrNull() ?: def
    private fun parseLongOrDefault(s: String?, def: Long): Long = s?.toLongOrNull() ?: def

    private enum class Panel { OVERVIEW, DEVICES, ATTACKS, SETTINGS }
    private enum class Severity { OK, WARN, DANGER }

    private data class Device(
        val name: String,
        val ip: String,
        val mac: String,
        val role: String,
        val status: String
    )
    private data class Flow(val src: String, val dst: String, val proto: String, val conf: Int)
    private data class EventItem(val type: String, val desc: String)
}