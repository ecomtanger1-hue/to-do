package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val squadId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val createdAt: Long,
    val priority: String,
    val category: String,
    val dueDate: String,
    val proofNote: String?,
    val isPact: Boolean,
    val cheerCount: Int
) {
    fun toDomain(): TaskItem = TaskItem(
        id = id,
        squadId = squadId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        title = title,
        description = description,
        isCompleted = isCompleted,
        completedAt = completedAt,
        createdAt = createdAt,
        priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.MEDIUM),
        category = runCatching { TaskCategory.valueOf(category) }.getOrDefault(TaskCategory.WORK),
        dueDate = dueDate,
        proofNote = proofNote,
        isPact = isPact,
        cheerCount = cheerCount
    )

    companion object {
        fun fromDomain(item: TaskItem): TaskEntity = TaskEntity(
            id = item.id,
            squadId = item.squadId,
            userId = item.userId,
            userName = item.userName,
            userAvatar = item.userAvatar,
            title = item.title,
            description = item.description,
            isCompleted = item.isCompleted,
            completedAt = item.completedAt,
            createdAt = item.createdAt,
            priority = item.priority.name,
            category = item.category.name,
            dueDate = item.dueDate,
            proofNote = item.proofNote,
            isPact = item.isPact,
            cheerCount = item.cheerCount
        )
    }
}
