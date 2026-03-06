package com.example.orangetracker.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.orangetracker.R
import com.example.orangetracker.model.Device
import com.example.orangetracker.model.DeviceStatus

class DeviceAdapter(
    private val context: Context,
    private var devices: MutableList<Device>,
    private val onDeviceClickListener: (Device) -> Unit,
    private val onDeviceBlockListener: (Device) -> Unit,
    private val onDeviceUnblockListener: (Device) -> Unit,
    private val onDeviceLimitListener: (Device, Int) -> Unit
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = devices.size

    override fun getItem(position: Int): Device = devices[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("DeviceAdapter", "getView() called for position: $position")

        try {
            val view: View
            val viewHolder: ViewHolder

            if (convertView == null) {
                Log.d("DeviceAdapter", "Creating new view for position: $position")
                view = inflater.inflate(R.layout.item_device, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                Log.d("DeviceAdapter", "Reusing view for position: $position")
                view = convertView
                viewHolder = convertView.tag as ViewHolder
            }

            val device = getItem(position)
            Log.d("DeviceAdapter", "Rendering device: ${device.name} at position: $position")

            // Заполнение данных с проверкой на null
            viewHolder.tvName.text = device.displayName
            viewHolder.tvIp.text = device.ip

            // Форматирование скорости
            viewHolder.tvRx.text = String.format("↓ %.1f", device.currentRxMbps)
            viewHolder.tvTx.text = String.format("↑ %.1f", device.currentTxMbps)

            // Статус
            viewHolder.tvStatus.text = device.statusText
            viewHolder.tvStatus.setTextColor(ContextCompat.getColor(context, device.statusColor))
            viewHolder.tvStatus.setBackgroundResource(device.statusBackground)

            // Иконка
            viewHolder.ivIcon.setImageResource(R.drawable.ic_device_default)
            try {
                val tintColor = if (device.isBlocked)
                    R.color.status_blocked
                else
                    R.color.primary
                viewHolder.ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, tintColor)
                )
            } catch (e: Exception) {
                Log.e("DeviceAdapter", "Error setting icon tint", e)
            }

            // Клик по элементу с логированием
            view.setOnClickListener {
                Log.d("DeviceAdapter", "Item clicked: ${device.name} at position: $position")
                try {
                    onDeviceClickListener.invoke(device)
                    Log.d("DeviceAdapter", "onDeviceClickListener invoked successfully")
                } catch (e: Exception) {
                    Log.e("DeviceAdapter", "Error in onDeviceClickListener", e)
                    e.printStackTrace()
                }
            }

            // Меню действий с логированием
            viewHolder.btnMenu.setOnClickListener {
                Log.d("DeviceAdapter", "Menu clicked for: ${device.name}")
                try {
                    showDeviceMenu(device)
                } catch (e: Exception) {
                    Log.e("DeviceAdapter", "Error showing menu", e)
                }
            }

            return view

        } catch (e: Exception) {
            Log.e("DeviceAdapter", "CRITICAL ERROR in getView() for position: $position", e)
            e.printStackTrace()

            // Возвращаем пустое view в случае ошибки, чтобы приложение не крашилось
            return convertView ?: inflater.inflate(R.layout.item_device, parent, false)
        }
    }

    private fun showDeviceMenu(device: Device) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        if (device.isBlocked) {
            options.add("Разблокировать устройство")
            actions.add { onDeviceUnblockListener.invoke(device) }
        } else {
            options.add("Заблокировать устройство")
            actions.add { onDeviceBlockListener.invoke(device) }
        }

        options.add("Ограничить скорость")
        actions.add { showSpeedLimitDialog(device) }

        options.add("Отмена")
        actions.add { }

        AlertDialog.Builder(context)
            .setTitle(device.displayName)
            .setItems(options.toTypedArray()) { _, which ->
                try {
                    if (which < actions.size) {
                        actions[which].invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .show()
    }

    private fun showSpeedLimitDialog(device: Device) {
        val input = android.widget.EditText(context).apply {
            hint = "Скорость в Мбит/с"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(if (device.isLimited) device.speedLimit.toString() else "10")
        }

        AlertDialog.Builder(context)
            .setTitle("Ограничение скорости")
            .setMessage("Введите максимальную скорость для ${device.displayName}")
            .setView(input)
            .setPositiveButton("Применить") { _, _ ->
                try {
                    val limit = input.text.toString().toIntOrNull() ?: 10
                    onDeviceLimitListener.invoke(device, limit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    fun updateDevices(newDevices: List<Device>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    fun updateDevice(updatedDevice: Device) {
        val index = devices.indexOfFirst { it.id == updatedDevice.id }
        if (index != -1) {
            devices[index] = updatedDevice
            notifyDataSetChanged()
        }
    }

    fun getDevices(): List<Device> = devices.toList()

    // ViewHolder для оптимизации
    class ViewHolder(view: View) {
        val ivIcon: ImageView = view.findViewById(R.id.ivDeviceIcon)
        val tvName: TextView = view.findViewById(R.id.tvDeviceName)
        val tvIp: TextView = view.findViewById(R.id.tvDeviceIp)
        val tvRx: TextView = view.findViewById(R.id.tvDeviceRx)
        val tvTx: TextView = view.findViewById(R.id.tvDeviceTx)
        val tvStatus: TextView = view.findViewById(R.id.tvDeviceStatus)
        val btnMenu: ImageButton = view.findViewById(R.id.btnDeviceMenu)
    }
}