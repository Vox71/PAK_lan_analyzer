package com.example.orangetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper

class ConnectionActivity : AppCompatActivity() {

    private lateinit var btnConnect: Button
    private lateinit var btnManualConnect: Button
    private lateinit var etManualIp: EditText
    private lateinit var layoutSearching: LinearLayout
    private lateinit var layoutFound: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var btnRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        initViews()
        setupClickListeners()

        // Имитация поиска устройства (для демо)
        simulateDeviceSearch()
    }

    private fun initViews() {
        btnConnect = findViewById(R.id.btnConnect)
        btnManualConnect = findViewById(R.id.btnManualConnect)
        etManualIp = findViewById(R.id.etManualIp)
        layoutSearching = findViewById(R.id.layoutSearching)
        layoutFound = findViewById(R.id.layoutFound)
        layoutError = findViewById(R.id.layoutError)
        btnRetry = findViewById(R.id.btnRetry)
    }

    private fun setupClickListeners() {
        btnConnect.setOnClickListener {
            // Переход на главный экран
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("DEVICE_IP", "192.168.2.1")
            intent.putExtra("DEVICE_FOUND", true)
            startActivity(intent)
            finish()
        }

        btnManualConnect.setOnClickListener {
            val ip = etManualIp.text.toString()
            if (ip.isNotEmpty()) {
                // Сохраняем IP и переходим на главный экран
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("DEVICE_IP", ip)
                intent.putExtra("DEVICE_FOUND", true)
                startActivity(intent)
                finish()
            } else {
                etManualIp.error = "Введите IP-адрес"
            }
        }

        btnRetry.setOnClickListener {
            // Повторный поиск
            layoutError.visibility = android.view.View.GONE
            layoutSearching.visibility = android.view.View.VISIBLE
            simulateDeviceSearch()
        }
    }

    private fun simulateDeviceSearch() {
        // Имитация поиска устройства (через 2 секунды "находим" устройство)
        Handler(Looper.getMainLooper()).postDelayed({
            layoutSearching.visibility = android.view.View.GONE
            layoutFound.visibility = android.view.View.VISIBLE
        }, 2000)
    }
}