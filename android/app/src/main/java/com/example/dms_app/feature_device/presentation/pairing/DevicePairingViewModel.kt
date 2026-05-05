package com.example.dms_app.feature_device.presentation.pairing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ConnectionType { WIFI, BLUETOOTH }
enum class DeviceType { CAMERA, OBD }

data class PairedDevice(
    val id: Int,
    val name: String,
    val type: DeviceType,
    val signalBars: Int,
    val isConnected: Boolean
)

class DevicePairingViewModel : ViewModel() {
    private val _devices = MutableStateFlow(listOf(
        PairedDevice(1, "DriveCam Pro X1", DeviceType.CAMERA, 4, false),
        PairedDevice(2, "OBD Assistant",   DeviceType.OBD,    3, false),
        PairedDevice(3, "DashCam Ultra",   DeviceType.CAMERA, 2, true)
    ))
    val devices: StateFlow<List<PairedDevice>> = _devices

    private val _selectedId = MutableStateFlow<Int?>(null)
    val selectedId: StateFlow<Int?> = _selectedId

    private val _connectionType = MutableStateFlow(ConnectionType.WIFI)
    val connectionType: StateFlow<ConnectionType> = _connectionType

    fun selectDevice(id: Int) {
        _selectedId.value = if (_selectedId.value == id) null else id
    }

    fun setConnectionType(type: ConnectionType) {
        _connectionType.value = type
    }

    fun connectSelected() {
        // Logic to connect
    }
}
