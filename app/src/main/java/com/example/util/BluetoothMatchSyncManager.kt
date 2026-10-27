package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.BluetoothScorePacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

enum class BluetoothConnectionState {
    DISCONNECTED,
    LISTENING,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)

class BluetoothMatchSyncManager(private val context: Context) {

    private val tag = "BluetoothMatchSync"

    // Standard SPP UUID for Bluetooth RFCOMM communication
    private val appUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val appServiceName = "VolleyRefScoreboardSync"

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _connectionState = MutableStateFlow(BluetoothConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("Bluetooth Disconnected")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _latestReceivedPacket = MutableStateFlow<BluetoothScorePacket?>(null)
    val latestReceivedPacket: StateFlow<BluetoothScorePacket?> = _latestReceivedPacket.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _isServerRole = MutableStateFlow(false)
    val isServerRole: StateFlow<Boolean> = _isServerRole.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var activeSocket: BluetoothSocket? = null
    private var socketWriter: BufferedWriter? = null
    private var simulationJob: Job? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    init {
        refreshPairedDevices()
    }

    fun hasBluetoothSupport(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        try {
            if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
                _pairedDevices.value = emptyList()
                return
            }
            val bonded = bluetoothAdapter?.bondedDevices
            val list = bonded?.map { device ->
                BluetoothDeviceInfo(
                    name = device.name ?: "Unknown Device (${device.address})",
                    address = device.address
                )
            } ?: emptyList()
            _pairedDevices.value = list
        } catch (e: Exception) {
            Log.e(tag, "Error loading paired devices: ${e.message}")
        }
    }

    /**
     * Start Bluetooth RFCOMM Server (Listen for incoming scoreboard connections)
     * Typically run on the Referee Controller device.
     */
    @SuppressLint("MissingPermission")
    fun startServer() {
        disconnect()
        _isServerRole.value = true

        if (!hasPermissions()) {
            _connectionState.value = BluetoothConnectionState.ERROR
            _statusMessage.value = "Bluetooth permission not granted"
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            _connectionState.value = BluetoothConnectionState.ERROR
            _statusMessage.value = "Bluetooth is disabled on this device"
            return
        }

        scope.launch {
            try {
                _connectionState.value = BluetoothConnectionState.LISTENING
                _statusMessage.value = "Listening for remote scoreboard..."

                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appServiceName, appUuid)
                val socket = serverSocket?.accept() // Blocking call until client connects

                if (socket != null) {
                    serverSocket?.close() // Accept one scoreboard connection
                    serverSocket = null
                    setupConnectedSocket(socket, socket.remoteDevice?.name ?: "Remote Scoreboard")
                }
            } catch (e: IOException) {
                if (_connectionState.value == BluetoothConnectionState.LISTENING) {
                    _connectionState.value = BluetoothConnectionState.ERROR
                    _statusMessage.value = "Server listen error: ${e.message}"
                }
            }
        }
    }

    /**
     * Connect to a remote Referee device as a Scoreboard display
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String) {
        disconnect()
        _isServerRole.value = false

        if (!hasPermissions()) {
            _connectionState.value = BluetoothConnectionState.ERROR
            _statusMessage.value = "Bluetooth permission not granted"
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = BluetoothConnectionState.ERROR
            _statusMessage.value = "Bluetooth is disabled on this device"
            return
        }

        scope.launch {
            try {
                val targetDevice = adapter.getRemoteDevice(deviceAddress)
                _connectionState.value = BluetoothConnectionState.CONNECTING
                _statusMessage.value = "Connecting to ${targetDevice.name ?: deviceAddress}..."

                adapter.cancelDiscovery() // Always cancel discovery before connecting

                val socket = targetDevice.createRfcommSocketToServiceRecord(appUuid)
                socket.connect() // Blocking connection call

                setupConnectedSocket(socket, targetDevice.name ?: deviceAddress)
            } catch (e: IOException) {
                _connectionState.value = BluetoothConnectionState.ERROR
                _statusMessage.value = "Connection failed: ${e.message}"
            }
        }
    }

    private fun setupConnectedSocket(socket: BluetoothSocket, deviceName: String) {
        activeSocket = socket
        _connectedDeviceName.value = deviceName
        _connectionState.value = BluetoothConnectionState.CONNECTED
        _statusMessage.value = "Connected to $deviceName"

        try {
            socketWriter = BufferedWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8))
        } catch (e: Exception) {
            Log.e(tag, "Failed to get output stream: ${e.message}")
        }

        // Start listening for incoming JSON packets continuously on IO thread
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        val packet = BluetoothScorePacket.fromJsonString(line)
                        if (packet != null) {
                            _latestReceivedPacket.value = packet
                        }
                    }
                }
            } catch (e: IOException) {
                Log.d(tag, "Connection stream closed: ${e.message}")
            } finally {
                disconnect()
            }
        }
    }

    /**
     * Send current match score packet over Bluetooth to the connected scoreboard
     */
    fun sendScorePacket(packet: BluetoothScorePacket) {
        if (_isSimulationMode.value) {
            _latestReceivedPacket.value = packet
            return
        }

        if (_connectionState.value != BluetoothConnectionState.CONNECTED) return

        scope.launch {
            try {
                val json = packet.toJsonString()
                socketWriter?.apply {
                    write(json)
                    newLine()
                    flush()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to send packet over Bluetooth: ${e.message}")
            }
        }
    }

    /**
     * Disconnects active socket and resets state
     */
    fun disconnect() {
        stopSimulation()
        try {
            socketWriter?.close()
        } catch (_: Exception) {}
        socketWriter = null

        try {
            activeSocket?.close()
        } catch (_: Exception) {}
        activeSocket = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        _connectedDeviceName.value = null
        _connectionState.value = BluetoothConnectionState.DISCONNECTED
        _statusMessage.value = "Bluetooth Disconnected"
    }

    /**
     * Enables Demo / Simulation Mode so users can preview the Big Scoreboard
     * receiving live updates even on a single device without a second Bluetooth device!
     */
    fun toggleSimulationMode(initialPacket: BluetoothScorePacket) {
        if (_isSimulationMode.value) {
            stopSimulation()
            disconnect()
        } else {
            disconnect()
            _isSimulationMode.value = true
            _connectionState.value = BluetoothConnectionState.CONNECTED
            _connectedDeviceName.value = "Simulated Referee Device"
            _statusMessage.value = "Running in Demo Simulation Mode"
            _latestReceivedPacket.value = initialPacket

            simulationJob = scope.launch {
                var scoreA = initialPacket.scoreA
                var scoreB = initialPacket.scoreB
                var server = initialPacket.servingTeamId
                var sec = 30
                while (isActive) {
                    delay(3000L)
                    // Alternate scoring
                    if (Math.random() > 0.5) {
                        scoreA++
                        server = "team_a"
                    } else {
                        scoreB++
                        server = "team_b"
                    }
                    sec = (sec + 3) % 1800
                    val clockStr = String.format("%02d:%02d", sec / 60, sec % 60)

                    _latestReceivedPacket.value = initialPacket.copy(
                        scoreA = scoreA,
                        scoreB = scoreB,
                        servingTeamId = server,
                        clockFormatted = clockStr,
                        clockIsRunning = true,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulationMode.value = false
    }
}
