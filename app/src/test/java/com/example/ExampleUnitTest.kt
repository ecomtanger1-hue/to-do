package com.example

import com.example.data.model.SquadMember
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMemberCompletionPercentage() {
    val member = SquadMember(
      id = "user_1",
      name = "Alex",
      tasksCount = 4,
      completedCount = 3
    )
    assertEquals(0.75f, member.completionPercentage, 0.001f)
  }

  @Test
  fun testTaskPactModel() {
    val task = TaskItem(
      id = "t1",
      userId = "u1",
      userName = "Alex",
      title = "Gym session",
      priority = TaskPriority.HIGH,
      category = TaskCategory.FITNESS,
      isPact = true
    )
    assertTrue(task.isPact)
    assertFalse(task.isCompleted)
    assertEquals(TaskPriority.HIGH, task.priority)
    assertEquals(TaskCategory.FITNESS, task.category)
  }

  @Test
  fun testUserProfileAndHandle() {
    val profile = com.example.data.model.UserProfile(
      id = "u_1",
      name = "Sarah Miller",
      handle = "@sarah_m",
      email = "sarah@example.com",
      avatar = "🚀"
    )
    assertEquals("@sarah_m", profile.handle)
    assertEquals("Sarah Miller", profile.name)
    assertEquals("🚀", profile.avatar)
  }

  @Test
  fun testUserSearchResult() {
    val search = com.example.data.model.UserSearchResult(
      id = "u_2",
      name = "Elena Rostova",
      handle = "@elena_fit",
      avatar = "🧗",
      focusArea = "Triathlon & Daily Habits",
      streakDays = 14,
      isAlreadyInSquad = false
    )
    assertEquals("@elena_fit", search.handle)
    assertFalse(search.isAlreadyInSquad)
    assertEquals(14, search.streakDays)
  }
}
