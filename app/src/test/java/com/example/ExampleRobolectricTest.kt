package com.example

import com.example.data.model.SquadMember
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify member domain calculations`() {
    val member = SquadMember(
      id = "user_1",
      name = "Alex",
      tasksCount = 4,
      completedCount = 4
    )
    assertEquals(1.0f, member.completionPercentage, 0.001f)
  }
}
