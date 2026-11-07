package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FriendInvitation
import com.example.data.model.InviteStatus

@Entity(tableName = "friend_invitations")
data class FriendInvitationEntity(
    @PrimaryKey val id: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromUserHandle: String,
    val fromUserAvatar: String,
    val toUserHandle: String,
    val squadId: String,
    val squadName: String,
    val status: String,
    val timestamp: Long
) {
    fun toDomain(): FriendInvitation = FriendInvitation(
        id = id,
        fromUserId = fromUserId,
        fromUserName = fromUserName,
        fromUserHandle = fromUserHandle,
        fromUserAvatar = fromUserAvatar,
        toUserHandle = toUserHandle,
        squadId = squadId,
        squadName = squadName,
        status = runCatching { InviteStatus.valueOf(status) }.getOrDefault(InviteStatus.PENDING),
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(invite: FriendInvitation): FriendInvitationEntity = FriendInvitationEntity(
            id = invite.id,
            fromUserId = invite.fromUserId,
            fromUserName = invite.fromUserName,
            fromUserHandle = invite.fromUserHandle,
            fromUserAvatar = invite.fromUserAvatar,
            toUserHandle = invite.toUserHandle,
            squadId = invite.squadId,
            squadName = invite.squadName,
            status = invite.status.name,
            timestamp = invite.timestamp
        )
    }
}
