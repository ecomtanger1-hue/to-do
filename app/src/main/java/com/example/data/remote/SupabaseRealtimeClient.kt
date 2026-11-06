package com.example.data.remote

import android.util.Log
import com.example.data.model.RealtimeStatus
import com.example.data.model.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

sealed class RealtimeEvent {
    data class TaskUpdated(
        val taskId: String,
        val userId: String,
        val isCompleted: Boolean,
        val completedAt: Long?,
        val cheerCount: Int
    ) : RealtimeEvent()

    data class TaskCreated(val task: TaskItem) : RealtimeEvent()
    data class NudgeReceived(val senderName: String, val message: String, val timestamp: Long) : RealtimeEvent()
    data class CheerReceived(val senderName: String, val taskId: String, val taskTitle: String, val timestamp: Long) : RealtimeEvent()
    data class PresenceUpdated(val onlineUserIds: Set<String>) : RealtimeEvent()
}

class SupabaseRealtimeClient(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "SupabaseRealtime"

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val refCounter = AtomicInteger(1)

    private val _status = MutableStateFlow(RealtimeStatus.DISCONNECTED)
    val status: StateFlow<RealtimeStatus> = _status.asStateFlow()

    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private var currentUrl: String = ""
    private var currentKey: String = ""
    private var currentChannel: String = "squad_accountability"
    private var isExplicitlyDisconnected = false

    fun connect(supabaseUrl: String, anonKey: String, channel: String = "squad_accountability") {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            _status.value = RealtimeStatus.DISCONNECTED
            return
        }

        isExplicitlyDisconnected = false
        currentUrl = supabaseUrl
        currentKey = anonKey
        currentChannel = channel

        disconnect()
        _status.value = RealtimeStatus.CONNECTING

        val wsUrl = buildWsUrl(supabaseUrl, anonKey)
        Log.d(tag, "Connecting to Supabase Realtime: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("apikey", anonKey)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "WebSocket connection opened!")
                _status.value = RealtimeStatus.CONNECTED
                joinChannel(currentChannel)
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closing: $code $reason")
                _status.value = RealtimeStatus.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(tag, "WebSocket failure: ${t.message}", t)
                _status.value = RealtimeStatus.ERROR
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closed: $code $reason")
                _status.value = RealtimeStatus.DISCONNECTED
                if (!isExplicitlyDisconnected) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun buildWsUrl(supabaseUrl: String, anonKey: String): String {
        var base = supabaseUrl.trim().removeSuffix("/")
        base = if (base.startsWith("https://")) {
            base.replace("https://", "wss://")
        } else if (base.startsWith("http://")) {
            base.replace("http://", "ws://")
        } else {
            "wss://$base"
        }
        return "$base/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
    }

    private fun joinChannel(channel: String) {
        val topic = "realtime:$channel"
        val ref = refCounter.incrementAndGet().toString()

        val payload = JSONObject().apply {
            val config = JSONObject().apply {
                val broadcast = JSONObject().apply {
                    put("ack", false)
                    put("self", true)
                }
                val presence = JSONObject().apply {
                    put("key", "user_client")
                }
                val postgresChanges = JSONArray().apply {
                    val changeConfig = JSONObject().apply {
                        put("event", "*")
                        put("schema", "public")
                        put("table", "tasks")
                    }
                    put(changeConfig)
                }
                put("broadcast", broadcast)
                put("presence", presence)
                put("postgres_changes", postgresChanges)
            }
            put("config", config)
        }

        val joinMsg = JSONObject().apply {
            put("topic", topic)
            put("event", "phx_join")
            put("payload", payload)
            put("ref", ref)
        }

        Log.d(tag, "Joining channel: $topic")
        webSocket?.send(joinMsg.toString())
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(25_000)
                try {
                    val hbMsg = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", "hb_${System.currentTimeMillis()}")
                    }
                    webSocket?.send(hbMsg.toString())
                } catch (e: Exception) {
                    Log.w(tag, "Heartbeat failed", e)
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (isExplicitlyDisconnected) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(5_000)
            if (isActive && !isExplicitlyDisconnected && currentUrl.isNotBlank() && currentKey.isNotBlank()) {
                Log.d(tag, "Attempting automatic reconnection...")
                connect(currentUrl, currentKey, currentChannel)
            }
        }
    }

    fun broadcastTaskUpdate(taskId: String, userId: String, isCompleted: Boolean, completedAt: Long?, cheerCount: Int) {
        val topic = "realtime:$currentChannel"
        val ref = refCounter.incrementAndGet().toString()

        val innerPayload = JSONObject().apply {
            put("taskId", taskId)
            put("userId", userId)
            put("isCompleted", isCompleted)
            put("completedAt", completedAt ?: JSONObject.NULL)
            put("cheerCount", cheerCount)
        }

        val broadcastPayload = JSONObject().apply {
            put("type", "broadcast")
            put("event", "TASK_UPDATED")
            put("payload", innerPayload)
        }

        val msg = JSONObject().apply {
            put("topic", topic)
            put("event", "broadcast")
            put("payload", broadcastPayload)
            put("ref", ref)
        }

        webSocket?.send(msg.toString())
    }

    fun broadcastNudge(senderName: String, targetUserName: String, message: String) {
        val topic = "realtime:$currentChannel"
        val ref = refCounter.incrementAndGet().toString()

        val innerPayload = JSONObject().apply {
            put("senderName", senderName)
            put("targetUserName", targetUserName)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }

        val broadcastPayload = JSONObject().apply {
            put("type", "broadcast")
            put("event", "NUDGE_SENT")
            put("payload", innerPayload)
        }

        val msg = JSONObject().apply {
            put("topic", topic)
            put("event", "broadcast")
            put("payload", broadcastPayload)
            put("ref", ref)
        }

        webSocket?.send(msg.toString())
    }

    fun broadcastCheer(senderName: String, taskId: String, taskTitle: String) {
        val topic = "realtime:$currentChannel"
        val ref = refCounter.incrementAndGet().toString()

        val innerPayload = JSONObject().apply {
            put("senderName", senderName)
            put("taskId", taskId)
            put("taskTitle", taskTitle)
            put("timestamp", System.currentTimeMillis())
        }

        val broadcastPayload = JSONObject().apply {
            put("type", "broadcast")
            put("event", "CHEER_SENT")
            put("payload", innerPayload)
        }

        val msg = JSONObject().apply {
            put("topic", topic)
            put("event", "broadcast")
            put("payload", broadcastPayload)
            put("ref", ref)
        }

        webSocket?.send(msg.toString())
    }

    // Handles incoming messages from Supabase Realtime (both Postgres CDC changes & broadcast events)
    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            val event = json.optString("event")
            val payload = json.optJSONObject("payload") ?: return

            when (event) {
                "broadcast" -> {
                    val subEvent = payload.optString("event")
                    val data = payload.optJSONObject("payload") ?: return
                    when (subEvent) {
                        "TASK_UPDATED" -> {
                            val taskId = data.optString("taskId")
                            val userId = data.optString("userId")
                            val isCompleted = data.optBoolean("isCompleted")
                            val completedAt = if (data.has("completedAt") && !data.isNull("completedAt")) data.optLong("completedAt") else null
                            val cheerCount = data.optInt("cheerCount", 0)
                            if (taskId.isNotEmpty()) {
                                scope.launch {
                                    _events.emit(RealtimeEvent.TaskUpdated(taskId, userId, isCompleted, completedAt, cheerCount))
                                }
                            }
                        }
                        "NUDGE_SENT" -> {
                            val senderName = data.optString("senderName", "Friend")
                            val message = data.optString("message", "Keep going!")
                            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
                            scope.launch {
                                _events.emit(RealtimeEvent.NudgeReceived(senderName, message, timestamp))
                            }
                        }
                        "CHEER_SENT" -> {
                            val senderName = data.optString("senderName", "Friend")
                            val taskId = data.optString("taskId")
                            val taskTitle = data.optString("taskTitle", "Task")
                            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
                            scope.launch {
                                _events.emit(RealtimeEvent.CheerReceived(senderName, taskId, taskTitle, timestamp))
                            }
                        }
                    }
                }
                "postgres_changes" -> {
                    val data = payload.optJSONObject("data") ?: return
                    val record = data.optJSONObject("record")
                    if (record != null) {
                        val taskId = record.optString("id")
                        val userId = record.optString("user_id")
                        val isCompleted = record.optBoolean("is_completed")
                        val completedAt = if (record.has("completed_at") && !record.isNull("completed_at")) record.optLong("completed_at") else null
                        val cheerCount = record.optInt("cheer_count", 0)
                        if (taskId.isNotEmpty()) {
                            scope.launch {
                                _events.emit(RealtimeEvent.TaskUpdated(taskId, userId, isCompleted, completedAt, cheerCount))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing incoming realtime message: ${e.message}", e)
        }
    }

    // Injected events (e.g. from simulator or test suite)
    fun emitLocalEvent(event: RealtimeEvent) {
        scope.launch {
            _events.emit(event)
        }
    }

    fun disconnect() {
        isExplicitlyDisconnected = true
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        webSocket = null
        _status.value = RealtimeStatus.DISCONNECTED
    }
}
