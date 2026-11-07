package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SquadDoDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks WHERE squadId = :squadId ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksForSquadFlow(squadId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksForUserFlow(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: String, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET cheerCount = cheerCount + 1 WHERE id = :taskId")
    suspend fun incrementTaskCheer(taskId: String)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    // --- Squad Members ---
    @Query("SELECT * FROM squad_members WHERE squadId = :squadId ORDER BY completedCount DESC, streakDays DESC")
    fun getMembersForSquadFlow(squadId: String): Flow<List<SquadMemberEntity>>

    @Query("SELECT * FROM squad_members WHERE id = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: String): SquadMemberEntity?

    @Query("SELECT * FROM squad_members")
    suspend fun getAllMembers(): List<SquadMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SquadMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<SquadMemberEntity>)

    @Query("UPDATE squad_members SET tasksCount = :tasks, completedCount = :completed WHERE id = :memberId")
    suspend fun updateMemberTaskCounts(memberId: String, tasks: Int, completed: Int)

    @Query("UPDATE squad_members SET status = :status, isOnline = :isOnline WHERE id = :memberId")
    suspend fun updateMemberStatus(memberId: String, status: String, isOnline: Boolean)

    // --- Squad Activities ---
    @Query("SELECT * FROM squad_activities WHERE squadId = :squadId ORDER BY timestamp DESC LIMIT 50")
    fun getActivitiesForSquadFlow(squadId: String): Flow<List<SquadActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: SquadActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<SquadActivityEntity>)

    @Query("DELETE FROM squad_activities WHERE id NOT IN (SELECT id FROM squad_activities ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimOldActivities()

    // --- Friend Invitations ---
    @Query("SELECT * FROM friend_invitations WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingInvitationsFlow(): Flow<List<FriendInvitationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: FriendInvitationEntity)

    @Query("UPDATE friend_invitations SET status = :status WHERE id = :inviteId")
    suspend fun updateInvitationStatus(inviteId: String, status: String)
}
