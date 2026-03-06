package com.example.orangetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var btnSettings: ImageButton
    private lateinit var btnViewDevices: Button
    private lateinit var btnViewStats: Button

    // TextView для отображения данных
    private lateinit var tvRxSpeed: TextView
    private lateinit var tvTxSpeed: TextView
    private lateinit var tvTotalRx: TextView
    private lateinit var tvTotalTx: TextView
    private lateinit var tvTotalClients: TextView
    private lateinit var tvActiveClients: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCpu: TextView
    private lateinit var tvUptime: TextView
    private lateinit var tvModel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()

        // Проверяем, откуда пришли
        checkLaunchSource()

        // Заполняем демо-данными
        populateDemoData()
    }

    private fun initViews() {
        btnSettings = findViewById(R.id.btnSettings)
        btnViewDevices = findViewById(R.id.btnViewDevices)
        btnViewStats = findViewById(R.id.btnViewStats)

        tvRxSpeed = findViewById(R.id.tvRxSpeed)
        tvTxSpeed = findViewById(R.id.tvTxSpeed)
        tvTotalRx = findViewById(R.id.tvTotalRx)
        tvTotalTx = findViewById(R.id.tvTotalTx)
        tvTotalClients = findViewById(R.id.tvTotalClients)
        tvActiveClients = findViewById(R.id.tvActiveClients)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCpu = findViewById(R.id.tvCpu)
        tvUptime = findViewById(R.id.tvUptime)
        tvModel = findViewById(R.id.tvModel)
    }

    private fun checkLaunchSource() {
        // Если приложение запущено впервые (не из ConnectionActivity)
        if (!intent.getBooleanExtra("DEVICE_FOUND", false)) {
            // Показываем приветственное сообщение
            Toast.makeText(this, "Добро пожаловать! Подключитесь к устройству", Toast.LENGTH_LONG).show()

            // Можно автоматически открыть экран подключения
            // Но лучше оставить это на усмотрение пользователя через кнопку в настройках
        } else {
            val deviceIp = intent.getStringExtra("DEVICE_IP")
            Toast.makeText(this, "Подключено к $deviceIp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            // Показываем сообщение, так как SettingsActivity еще не создана
            Toast.makeText(this, "Настройки будут доступны в следующей версии", Toast.LENGTH_SHORT).show()

            // TODO: В будущем:
            // val intent = Intent(this, SettingsActivity::class.java)
            // startActivity(intent)
        }

        btnViewDevices.setOnClickListener {
            // Переходим на экран устройств
            val intent = Intent(this, DevicesActivity::class.java)
            startActivity(intent)
        }

        btnViewStats.setOnClickListener {
            // Показываем сообщение, так как StatisticsActivity еще не создана
            Toast.makeText(this, "Статистика будет доступна в следующей версии", Toast.LENGTH_SHORT).show()

            // TODO: В будущем:
            // val intent = Intent(this, StatisticsActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun populateDemoData() {
        // Демо-данные для preview
        tvRxSpeed.text = "15.2"
        tvTxSpeed.text = "3.8"
        tvTotalRx.text = "45.2"
        tvTotalTx.text = "12.8"
        tvTotalClients.text = "7"
        tvActiveClients.text = "2 активных"
        tvTemperature.text = "52°C"
        tvCpu.text = "23%"
        tvUptime.text = "5д 12ч"
        tvModel.text = "Orange Pi Zero 3"
    }
}