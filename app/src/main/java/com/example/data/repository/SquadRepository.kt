package com.example.data.repository

import com.example.data.local.PreferencesManager
import com.example.data.local.SquadActivityEntity
import com.example.data.local.SquadDoDao
import com.example.data.local.SquadMemberEntity
import com.example.data.local.TaskEntity
import com.example.data.model.ActivityType
import com.example.data.model.RealtimeStatus
import com.example.data.model.SquadActivity
import com.example.data.model.SquadMember
import com.example.data.model.SupabaseConfig
import com.example.data.model.TaskItem
import com.example.data.remote.PeerSimulatorEngine
import com.example.data.remote.RealtimeEvent
import com.example.data.remote.SupabaseRealtimeClient
import com.example.data.remote.SupabaseRestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class SquadRepository(
    private val dao: SquadDoDao,
    val preferencesManager: PreferencesManager,
    val realtimeClient: SupabaseRealtimeClient,
    private val restClient: SupabaseRestClient,
    val simulatorEngine: PeerSimulatorEngine,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val realtimeStatus: StateFlow<RealtimeStatus> = realtimeClient.status

    val tasksFlow: Flow<List<TaskItem>> = dao.getTasksForSquadFlow("squad_general").map { entities ->
        entities.map { it.toDomain() }
    }

    val membersFlow: Flow<List<SquadMember>> = dao.getMembersForSquadFlow("squad_general").map { entities ->
        entities.map { it.toDomain() }
    }

    val activitiesFlow: Flow<List<SquadActivity>> = dao.getActivitiesForSquadFlow("squad_general").map { entities ->
        entities.map { it.toDomain() }
    }

    val supabaseConfigFlow: Flow<SupabaseConfig> = preferencesManager.supabaseConfigFlow
    val simulatorEnabledFlow: Flow<Boolean> = preferencesManager.simulatorEnabledFlow
    val userIdFlow: Flow<String> = preferencesManager.userIdFlow
    val userNameFlow: Flow<String> = preferencesManager.userNameFlow
    val userAvatarFlow: Flow<String> = preferencesManager.userAvatarFlow

    init {
        // Start simulated peer interactions
        simulatorEngine.start()

        // Observe Supabase config changes and auto-connect/disconnect
        scope.launch {
            preferencesManager.supabaseConfigFlow.collect { config ->
                if (config.isEnabled && config.url.isNotBlank() && config.anonKey.isNotBlank()) {
                    realtimeClient.connect(config.url, config.anonKey, config.channelName)
                } else {
                    realtimeClient.disconnect()
                }
            }
        }

        // Observe simulator toggle
        scope.launch {
            preferencesManager.simulatorEnabledFlow.collect { enabled ->
                simulatorEngine.isEnabled = enabled
            }
        }

        // Listen to Realtime Events from Supabase WebSocket & apply them instantly to local state!
        scope.launch {
            realtimeClient.events.collect { event ->
                handleRealtimeEvent(event)
            }
        }
    }

    private suspend fun handleRealtimeEvent(event: RealtimeEvent) {
        when (event) {
            is RealtimeEvent.TaskUpdated -> {
                dao.setTaskCompleted(event.taskId, event.isCompleted, event.completedAt)
                // Recalculate member stats
                val allTasks = dao.getAllTasks()
                val userTasks = allTasks.filter { it.userId == event.userId }
                if (userTasks.isNotEmpty()) {
                    val completed = userTasks.count { it.isCompleted }
                    dao.updateMemberTaskCounts(event.userId, userTasks.size, completed)
                }
            }
            is RealtimeEvent.TaskCreated -> {
                dao.insertTask(TaskEntity.fromDomain(event.task))
            }
            is RealtimeEvent.NudgeReceived -> {
                val act = SquadActivity(
                    id = UUID.randomUUID().toString(),
                    squadId = "squad_general",
                    userId = "nudge_incoming",
                    userName = event.senderName,
                    userAvatar = "⚡",
                    actionType = ActivityType.NUDGE_SENT,
                    targetUserName = "You",
                    message = "${event.senderName}: \"${event.message}\"",
                    timestamp = event.timestamp
                )
                dao.insertActivity(SquadActivityEntity.fromDomain(act))
            }
            is RealtimeEvent.CheerReceived -> {
                val act = SquadActivity(
                    id = UUID.randomUUID().toString(),
                    squadId = "squad_general",
                    userId = "cheer_incoming",
                    userName = event.senderName,
                    userAvatar = "🎉",
                    actionType = ActivityType.CHEER_SENT,
                    targetUserName = "You",
                    message = "${event.senderName} cheered your task: '${event.taskTitle}'!",
                    timestamp = event.timestamp
                )
                dao.insertActivity(SquadActivityEntity.fromDomain(act))
            }
            is RealtimeEvent.PresenceUpdated -> {
                // Online users presence
            }
        }
    }

    suspend fun toggleTaskCompletion(task: TaskItem) {
        val newCompleted = !task.isCompleted
        val now = if (newCompleted) System.currentTimeMillis() else null

        // 1. Optimistic Local Update
        dao.setTaskCompleted(task.id, newCompleted, now)

        val allUserTasks = dao.getAllTasks().filter { it.userId == task.userId }
        val completedCount = allUserTasks.count { it.isCompleted }
        dao.updateMemberTaskCounts(task.userId, allUserTasks.size, completedCount)

        // 2. Log Activity
        val actType = if (newCompleted) ActivityType.TASK_COMPLETED else ActivityType.TASK_CREATED
        val actMsg = if (newCompleted) "Checked off: ${task.title} ✅" else "Reopened: ${task.title}"
        val activity = SquadActivity(
            id = UUID.randomUUID().toString(),
            squadId = task.squadId,
            userId = task.userId,
            userName = task.userName,
            userAvatar = task.userAvatar,
            actionType = actType,
            message = actMsg,
            timestamp = System.currentTimeMillis()
        )
        dao.insertActivity(SquadActivityEntity.fromDomain(activity))

        // 3. Instant Supabase Realtime Broadcast (Low Latency Peer Sync)
        realtimeClient.broadcastTaskUpdate(
            taskId = task.id,
            userId = task.userId,
            isCompleted = newCompleted,
            completedAt = now,
            cheerCount = task.cheerCount
        )

        // 4. Background Supabase REST Sync
        scope.launch {
            val config = preferencesManager.supabaseConfigFlow.first()
            if (config.isEnabled && config.url.isNotBlank()) {
                val updatedTask = task.copy(isCompleted = newCompleted, completedAt = now)
                restClient.upsertTask(config.url, config.anonKey, updatedTask)
                restClient.logActivity(config.url, config.anonKey, activity)
            }
        }

        // 5. Trigger peer celebrations if task was checked off
        if (newCompleted && task.userId == "user_me") {
            simulatorEngine.onUserCompletedTask(task.title)
        }
    }

    suspend fun addTask(
        title: String,
        description: String,
        priority: com.example.data.model.TaskPriority,
        category: com.example.data.model.TaskCategory,
        dueDate: String,
        isPact: Boolean
    ) {
        val userId = userIdFlow.first()
        val userName = userNameFlow.first()
        val userAvatar = userAvatarFlow.first()

        val newTask = TaskItem(
            id = "task_${UUID.randomUUID()}",
            squadId = "squad_general",
            userId = userId,
            userName = userName,
            userAvatar = userAvatar,
            title = title.trim(),
            description = description.trim(),
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            priority = priority,
            category = category,
            dueDate = dueDate.ifBlank { "Today" },
            isPact = isPact
        )

        // 1. Local insert
        dao.insertTask(TaskEntity.fromDomain(newTask))

        val allUserTasks = dao.getAllTasks().filter { it.userId == userId }
        val completedCount = allUserTasks.count { it.isCompleted }
        dao.updateMemberTaskCounts(userId, allUserTasks.size, completedCount)

        // 2. Activity log
        val activity = SquadActivity(
            id = UUID.randomUUID().toString(),
            squadId = "squad_general",
            userId = userId,
            userName = userName,
            userAvatar = userAvatar,
            actionType = ActivityType.TASK_CREATED,
            message = "Added task: '$title' ${if (isPact) "🔥 (Accountability Pact!)" else ""}",
            timestamp = System.currentTimeMillis()
        )
        dao.insertActivity(SquadActivityEntity.fromDomain(activity))

        // 3. Supabase REST & Realtime broadcast
        scope.launch {
            val config = preferencesManager.supabaseConfigFlow.first()
            if (config.isEnabled && config.url.isNotBlank()) {
                restClient.upsertTask(config.url, config.anonKey, newTask)
                restClient.logActivity(config.url, config.anonKey, activity)
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        val task = dao.getTaskById(taskId)
        dao.deleteTask(taskId)
        if (task != null) {
            val allUserTasks = dao.getAllTasks().filter { it.userId == task.userId }
            val completedCount = allUserTasks.count { it.isCompleted }
            dao.updateMemberTaskCounts(task.userId, allUserTasks.size, completedCount)
        }
    }

    suspend fun sendNudge(targetMember: SquadMember, message: String) {
        val senderName = userNameFlow.first()
        val senderAvatar = userAvatarFlow.first()

        val activity = SquadActivity(
            id = UUID.randomUUID().toString(),
            squadId = targetMember.squadId,
            userId = "user_me",
            userName = senderName,
            userAvatar = senderAvatar,
            actionType = ActivityType.NUDGE_SENT,
            targetUserName = targetMember.name,
            message = "Sent accountability nudge to ${targetMember.name}: \"$message\"",
            timestamp = System.currentTimeMillis()
        )
        dao.insertActivity(SquadActivityEntity.fromDomain(activity))

        // Broadcast over Realtime WebSocket
        realtimeClient.broadcastNudge(senderName, targetMember.name, message)

        // Trigger peer response
        simulatorEngine.onUserNudgedFriend(targetMember.name)

        scope.launch {
            val config = preferencesManager.supabaseConfigFlow.first()
            if (config.isEnabled && config.url.isNotBlank()) {
                restClient.logActivity(config.url, config.anonKey, activity)
            }
        }
    }

    suspend fun cheerTask(task: TaskItem) {
        val senderName = userNameFlow.first()
        val senderAvatar = userAvatarFlow.first()

        dao.incrementTaskCheer(task.id)

        val activity = SquadActivity(
            id = UUID.randomUUID().toString(),
            squadId = task.squadId,
            userId = "user_me",
            userName = senderName,
            userAvatar = senderAvatar,
            actionType = ActivityType.CHEER_SENT,
            targetUserName = task.userName,
            message = "Gave high-five & cheers to ${task.userName} for '${task.title}'! 👏",
            timestamp = System.currentTimeMillis()
        )
        dao.insertActivity(SquadActivityEntity.fromDomain(activity))

        realtimeClient.broadcastCheer(senderName, task.id, task.title)

        scope.launch {
            val config = preferencesManager.supabaseConfigFlow.first()
            if (config.isEnabled && config.url.isNotBlank()) {
                restClient.logActivity(config.url, config.anonKey, activity)
            }
        }
    }

    suspend fun saveSupabaseSettings(url: String, anonKey: String, channel: String, enabled: Boolean) {
        preferencesManager.saveSupabaseConfig(url, anonKey, channel, enabled)
    }

    suspend fun setSimulatorEnabled(enabled: Boolean) {
        preferencesManager.setSimulatorEnabled(enabled)
    }

    suspend fun testSupabaseConnection(url: String, anonKey: String): Result<String> {
        return restClient.testConnection(url, anonKey)
    }
}
