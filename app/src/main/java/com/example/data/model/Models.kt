package com.example.data.model

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

enum class TaskCategory(val label: String, val emoji: String) {
    WORK("Work", "💼"),
    FITNESS("Fitness", "🏋️"),
    LEARNING("Learning", "📚"),
    HEALTH("Health", "🥗"),
    CHORES("Chores", "🧹"),
    OTHER("Other", "🎯")
}

enum class ActivityType {
    TASK_COMPLETED,
    TASK_CREATED,
    NUDGE_SENT,
    CHEER_SENT,
    STREAK_ACHIEVED,
    FRIEND_JOINED
}

enum class RealtimeStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    ERROR
}

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class TaskItem(
    val id: String,
    val squadId: String = "squad_general",
    val userId: String,
    val userName: String,
    val userAvatar: String = "👤",
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val dueDate: String = "Today",
    val proofNote: String? = null,
    val isPact: Boolean = false,
    val cheerCount: Int = 0
) {
    val isOverdue: Boolean
        get() = !isCompleted && dueDate.contains("Yesterday")
}

data class SquadMember(
    val id: String,
    val squadId: String = "squad_general",
    val name: String,
    val handle: String = "",
    val avatar: String = "👤",
    val role: String = "Member",
    val status: String = "Active",
    val isOnline: Boolean = true,
    val streakDays: Int = 1,
    val tasksCount: Int = 0,
    val completedCount: Int = 0
) {
    val completionPercentage: Float
        get() = if (tasksCount > 0) completedCount.toFloat() / tasksCount else 0f
}

data class Squad(
    val id: String = "squad_general",
    val name: String = "Focus Circle",
    val description: String = "Keep each other accountable & crush daily goals",
    val inviteCode: String = "SQUAD-7788",
    val streakDays: Int = 5
)

data class SquadActivity(
    val id: String,
    val squadId: String = "squad_general",
    val userId: String,
    val userName: String,
    val userAvatar: String = "👤",
    val actionType: ActivityType,
    val targetUserName: String? = null,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SupabaseConfig(
    val url: String = "",
    val anonKey: String = "",
    val channelName: String = "squad_accountability",
    val isEnabled: Boolean = false
)

data class UserProfile(
    val id: String,
    val name: String,
    val handle: String,
    val email: String = "",
    val avatar: String = "🚀",
    val bio: String = "Accountability Partner",
    val streakDays: Int = 1,
    val tasksCompleted: Int = 0
)

data class UserSearchResult(
    val id: String,
    val name: String,
    val handle: String,
    val avatar: String,
    val focusArea: String,
    val streakDays: Int,
    val isAlreadyInSquad: Boolean = false
)

data class FriendInvitation(
    val id: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromUserHandle: String,
    val fromUserAvatar: String,
    val toUserHandle: String,
    val squadId: String = "squad_general",
    val squadName: String = "Focus Circle",
    val status: InviteStatus = InviteStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)
