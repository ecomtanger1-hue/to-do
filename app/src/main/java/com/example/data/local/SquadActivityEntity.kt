package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ActivityType
import com.example.data.model.SquadActivity

@Entity(tableName = "squad_activities")
data class SquadActivityEntity(
    @PrimaryKey val id: String,
    val squadId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val actionType: String,
    val targetUserName: String?,
    val message: String,
    val timestamp: Long
) {
    fun toDomain(): SquadActivity = SquadActivity(
        id = id,
        squadId = squadId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        actionType = runCatching { ActivityType.valueOf(actionType) }.getOrDefault(ActivityType.TASK_COMPLETED),
        targetUserName = targetUserName,
        message = message,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(act: SquadActivity): SquadActivityEntity = SquadActivityEntity(
            id = act.id,
            squadId = act.squadId,
            userId = act.userId,
            userName = act.userName,
            userAvatar = act.userAvatar,
            actionType = act.actionType.name,
            targetUserName = act.targetUserName,
            message = act.message,
            timestamp = act.timestamp
        )
    }
}
