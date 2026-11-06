package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SquadMember

@Entity(tableName = "squad_members")
data class SquadMemberEntity(
    @PrimaryKey val id: String,
    val squadId: String,
    val name: String,
    val avatar: String,
    val role: String,
    val status: String,
    val isOnline: Boolean,
    val streakDays: Int,
    val tasksCount: Int,
    val completedCount: Int
) {
    fun toDomain(): SquadMember = SquadMember(
        id = id,
        squadId = squadId,
        name = name,
        avatar = avatar,
        role = role,
        status = status,
        isOnline = isOnline,
        streakDays = streakDays,
        tasksCount = tasksCount,
        completedCount = completedCount
    )

    companion object {
        fun fromDomain(member: SquadMember): SquadMemberEntity = SquadMemberEntity(
            id = member.id,
            squadId = member.squadId,
            name = member.name,
            avatar = member.avatar,
            role = member.role,
            status = member.status,
            isOnline = member.isOnline,
            streakDays = member.streakDays,
            tasksCount = member.tasksCount,
            completedCount = member.completedCount
        )
    }
}
