package com.example.data.remote

import com.example.data.local.SquadDoDao
import com.example.data.local.SquadMemberEntity
import com.example.data.model.ActivityType
import com.example.data.model.SquadActivity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class PeerSimulatorEngine(
    private val dao: SquadDoDao,
    private val realtimeClient: SupabaseRealtimeClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var simulationJob: Job? = null
    var isEnabled: Boolean = true

    fun start() {
        if (simulationJob?.isActive == true) return
        simulationJob = scope.launch {
            // Seed initial peers & initial tasks if empty
            seedInitialDataIfNecessary()

            while (isActive) {
                delay(22_000) // Every ~22 seconds, a peer interacts
                if (!isEnabled) continue

                try {
                    performRandomPeerAction()
                } catch (e: Exception) {
                    // Ignore simulation error
                }
            }
        }
    }

    fun stop() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private suspend fun seedInitialDataIfNecessary() {
        val existingTasks = dao.getAllTasks()
        if (existingTasks.isNotEmpty()) return

        val members = listOf(
            SquadMemberEntity(
                id = "user_me",
                squadId = "squad_general",
                name = "You",
                avatar = "🚀",
                role = "Squad Member",
                status = "Focus Mode 🔥",
                isOnline = true,
                streakDays = 5,
                tasksCount = 3,
                completedCount = 1
            ),
            SquadMemberEntity(
                id = "user_alex",
                squadId = "squad_general",
                name = "Alex Rivera",
                avatar = "🦊",
                role = "Accountability Captain",
                status = "Crushing sprints 💻",
                isOnline = true,
                streakDays = 12,
                tasksCount = 4,
                completedCount = 2
            ),
            SquadMemberEntity(
                id = "user_maya",
                squadId = "squad_general",
                name = "Maya Chen",
                avatar = "⚡",
                role = "Partner",
                status = "Gym session 🏋️",
                isOnline = true,
                streakDays = 8,
                tasksCount = 3,
                completedCount = 2
            ),
            SquadMemberEntity(
                id = "user_jordan",
                squadId = "squad_general",
                name = "Jordan Blake",
                avatar = "🦁",
                role = "Partner",
                status = "Deep reading 📚",
                isOnline = false,
                streakDays = 3,
                tasksCount = 3,
                completedCount = 1
            )
        )
        dao.insertMembers(members)

        val initialTasks = listOf(
            // User tasks
            TaskItem(
                id = "task_user_1",
                squadId = "squad_general",
                userId = "user_me",
                userName = "You",
                userAvatar = "🚀",
                title = "Ship API architecture & Realtime sync",
                description = "Accountability pact with Alex: finish before 5 PM",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 45,
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                dueDate = "Today 5:00 PM",
                isPact = true,
                cheerCount = 2
            ),
            TaskItem(
                id = "task_user_2",
                squadId = "squad_general",
                userId = "user_me",
                userName = "You",
                userAvatar = "🚀",
                title = "45 min Zone 2 Cardio & Stretching",
                description = "Maya is checking in at 7 PM!",
                isCompleted = false,
                priority = TaskPriority.HIGH,
                category = TaskCategory.FITNESS,
                dueDate = "Today 7:00 PM",
                isPact = true
            ),
            TaskItem(
                id = "task_user_3",
                squadId = "squad_general",
                userId = "user_me",
                userName = "You",
                userAvatar = "🚀",
                title = "Read 20 pages of Systems Architecture",
                description = "Daily learning commitment",
                isCompleted = false,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.LEARNING,
                dueDate = "Tonight 10:00 PM"
            ),
            // Alex's tasks
            TaskItem(
                id = "task_alex_1",
                squadId = "squad_general",
                userId = "user_alex",
                userName = "Alex Rivera",
                userAvatar = "🦊",
                title = "LeetCode 2 Medium problems",
                description = "Daily algorithms pact",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 120,
                priority = TaskPriority.HIGH,
                category = TaskCategory.LEARNING,
                dueDate = "Today 12:00 PM",
                cheerCount = 3
            ),
            TaskItem(
                id = "task_alex_2",
                squadId = "squad_general",
                userId = "user_alex",
                userName = "Alex Rivera",
                userAvatar = "🦊",
                title = "Review PR #419 & Code Refactoring",
                description = "Team sprint commitment",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 30,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.WORK,
                dueDate = "Today 3:00 PM",
                cheerCount = 1
            ),
            TaskItem(
                id = "task_alex_3",
                squadId = "squad_general",
                userId = "user_alex",
                userName = "Alex Rivera",
                userAvatar = "🦊",
                title = "6:00 PM 5km Evening Run",
                description = "Holding Jordan accountable to join!",
                isCompleted = false,
                priority = TaskPriority.HIGH,
                category = TaskCategory.FITNESS,
                dueDate = "Today 6:00 PM",
                isPact = true
            ),
            TaskItem(
                id = "task_alex_4",
                squadId = "squad_general",
                userId = "user_alex",
                userName = "Alex Rivera",
                userAvatar = "🦊",
                title = "Zero inbox clearing",
                description = "Respond to client emails",
                isCompleted = false,
                priority = TaskPriority.LOW,
                category = TaskCategory.CHORES,
                dueDate = "Today 8:00 PM"
            ),
            // Maya's tasks
            TaskItem(
                id = "task_maya_1",
                squadId = "squad_general",
                userId = "user_maya",
                userName = "Maya Chen",
                userAvatar = "⚡",
                title = "Morning High-Intensity Workout",
                description = "Done before 8 AM!",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 180,
                priority = TaskPriority.HIGH,
                category = TaskCategory.FITNESS,
                dueDate = "Today 8:00 AM",
                cheerCount = 4
            ),
            TaskItem(
                id = "task_maya_2",
                squadId = "squad_general",
                userId = "user_maya",
                userName = "Maya Chen",
                userAvatar = "⚡",
                title = "Finalize UI Design System tokens",
                description = "Figma review with squad",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 60,
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                dueDate = "Today 2:00 PM",
                cheerCount = 2
            ),
            TaskItem(
                id = "task_maya_3",
                squadId = "squad_general",
                userId = "user_maya",
                userName = "Maya Chen",
                userAvatar = "⚡",
                title = "30 min Spanish Conversation Practice",
                description = "Duolingo + audio call",
                isCompleted = false,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.LEARNING,
                dueDate = "Today 9:00 PM"
            ),
            // Jordan's tasks
            TaskItem(
                id = "task_jordan_1",
                squadId = "squad_general",
                userId = "user_jordan",
                userName = "Jordan Blake",
                userAvatar = "🦁",
                title = "Draft Chapter 3 of Tech Blog",
                description = "Publishing by Friday",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 90,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.WORK,
                dueDate = "Today 1:00 PM"
            ),
            TaskItem(
                id = "task_jordan_2",
                squadId = "squad_general",
                userId = "user_jordan",
                userName = "Jordan Blake",
                userAvatar = "🦁",
                title = "Strength Training (Leg Day)",
                description = "Accountability pact: 100 squats",
                isCompleted = false,
                priority = TaskPriority.HIGH,
                category = TaskCategory.FITNESS,
                dueDate = "Today 6:30 PM",
                isPact = true
            ),
            TaskItem(
                id = "task_jordan_3",
                squadId = "squad_general",
                userId = "user_jordan",
                userName = "Jordan Blake",
                userAvatar = "🦁",
                title = "Drink 3 Liters of Water",
                description = "Hydration tracking",
                isCompleted = false,
                priority = TaskPriority.LOW,
                category = TaskCategory.HEALTH,
                dueDate = "Today All Day"
            )
        )
        dao.insertTasks(initialTasks.map { com.example.data.local.TaskEntity.fromDomain(it) })

        val initialActivities = listOf(
            SquadActivity(
                id = "act_1",
                squadId = "squad_general",
                userId = "user_alex",
                userName = "Alex Rivera",
                userAvatar = "🦊",
                actionType = ActivityType.TASK_COMPLETED,
                message = "Checked off: Review PR #419 & Code Refactoring 🚀",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            SquadActivity(
                id = "act_2",
                squadId = "squad_general",
                userId = "user_maya",
                userName = "Maya Chen",
                userAvatar = "⚡",
                actionType = ActivityType.CHEER_SENT,
                targetUserName = "You",
                message = "Cheered your task: Ship API architecture! 🎉",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 40
            ),
            SquadActivity(
                id = "act_3",
                squadId = "squad_general",
                userId = "user_jordan",
                userName = "Jordan Blake",
                userAvatar = "🦁",
                actionType = ActivityType.STREAK_ACHIEVED,
                message = "Hit a 3-day consistency streak! 🔥",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 80
            )
        )
        dao.insertActivities(initialActivities.map { com.example.data.local.SquadActivityEntity.fromDomain(it) })
    }

    private suspend fun performRandomPeerAction() {
        val peerIds = listOf("user_alex", "user_maya", "user_jordan")
        val randomPeer = peerIds.random()

        // Get uncompleted tasks of this peer
        val tasks = dao.getAllTasks().filter { it.userId == randomPeer }
        val uncompleted = tasks.filter { !it.isCompleted }

        if (uncompleted.isNotEmpty()) {
            val taskToComplete = uncompleted.random()
            val now = System.currentTimeMillis()
            dao.setTaskCompleted(taskToComplete.id, true, now)

            // Update member counts
            val currentTasks = dao.getAllTasks().filter { it.userId == randomPeer }
            val completedCount = currentTasks.count { it.isCompleted }
            dao.updateMemberTaskCounts(randomPeer, currentTasks.size, completedCount)

            // Log activity
            val act = SquadActivity(
                id = UUID.randomUUID().toString(),
                squadId = "squad_general",
                userId = randomPeer,
                userName = taskToComplete.userName,
                userAvatar = taskToComplete.userAvatar,
                actionType = ActivityType.TASK_COMPLETED,
                message = "Checked off: ${taskToComplete.title} ✅",
                timestamp = now
            )
            dao.insertActivity(com.example.data.local.SquadActivityEntity.fromDomain(act))

            // Emit realtime event via client
            realtimeClient.emitLocalEvent(
                RealtimeEvent.TaskUpdated(
                    taskId = taskToComplete.id,
                    userId = randomPeer,
                    isCompleted = true,
                    completedAt = now,
                    cheerCount = taskToComplete.cheerCount
                )
            )
        }
    }

    // Called when the user checks off a task: peers react with cheers!
    fun onUserCompletedTask(taskTitle: String) {
        if (!isEnabled) return
        scope.launch {
            delay(2_500) // Realistic 2.5s delay for friend reaction
            val cheeringPeer = listOf(
                Pair("Alex Rivera", "🦊"),
                Pair("Maya Chen", "⚡")
            ).random()

            val cheerMessage = listOf(
                "Way to crush it! Keeping the squad streak alive! 🔥",
                "Boom! Saw you check that off live. Massive respect! 🚀",
                "High five! That was a tough one to check off! 👏"
            ).random()

            val act = SquadActivity(
                id = UUID.randomUUID().toString(),
                squadId = "squad_general",
                userId = "user_peer",
                userName = cheeringPeer.first,
                userAvatar = cheeringPeer.second,
                actionType = ActivityType.CHEER_SENT,
                targetUserName = "You",
                message = "${cheeringPeer.first} cheered '$taskTitle': $cheerMessage",
                timestamp = System.currentTimeMillis()
            )
            dao.insertActivity(com.example.data.local.SquadActivityEntity.fromDomain(act))

            realtimeClient.emitLocalEvent(
                RealtimeEvent.CheerReceived(
                    senderName = cheeringPeer.first,
                    taskId = "user_task",
                    taskTitle = taskTitle,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Called when the user nudges a friend: friend answers back!
    fun onUserNudgedFriend(friendName: String) {
        if (!isEnabled) return
        scope.launch {
            delay(3_500) // Friend response time
            val responses = listOf(
                "Got your nudge! Putting my phone on Do Not Disturb right now 💪",
                "Thanks for holding me accountable! Closing Slack and finishing it now ⚡",
                "Ouch, caught me slacking! Starting on that task immediately 🚀",
                "Appreciate the check-in! Almost done with it! ⏱️"
            )
            val answer = responses.random()

            val act = SquadActivity(
                id = UUID.randomUUID().toString(),
                squadId = "squad_general",
                userId = "peer_reply",
                userName = friendName,
                userAvatar = "💬",
                actionType = ActivityType.NUDGE_SENT,
                targetUserName = "You",
                message = "$friendName replied to your nudge: \"$answer\"",
                timestamp = System.currentTimeMillis()
            )
            dao.insertActivity(com.example.data.local.SquadActivityEntity.fromDomain(act))

            realtimeClient.emitLocalEvent(
                RealtimeEvent.NudgeReceived(
                    senderName = friendName,
                    message = answer,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
