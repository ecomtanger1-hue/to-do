package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.Emerald400

@Composable
fun TaskCard(
    task: TaskItem,
    isCurrentUser: Boolean,
    onToggle: (TaskItem) -> Unit,
    onCheer: (TaskItem) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkboxBg by animateColorAsState(
        targetValue = if (task.isCompleted) Emerald400 else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxBg"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .border(
                width = 1.dp,
                color = if (task.isPact && !task.isCompleted) {
                    AmberFlame.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Minimalist Circular Checkbox
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(checkboxBg)
                    .border(
                        width = 1.5.dp,
                        color = if (task.isCompleted) Emerald400 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onToggle(task) }
                    .testTag("task_checkbox_${task.id}")
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Minimal Pact indicator
                    if (task.isPact && !task.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberFlame.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔥 Pact",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberFlame,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // If friend's task, show their name subtly
                if (!isCurrentUser) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${task.userAvatar} ${task.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side actions: Cheer counter + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Cheer Button (always available to encourage)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCheer(task) }
                        .testTag("cheer_button_${task.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(text = "👏", fontSize = 12.sp)
                        if (task.cheerCount > 0) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${task.cheerCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Delete button for owner
                if (isCurrentUser) {
                    IconButton(
                        onClick = { onDelete(task.id) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
