package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400

@Composable
fun AuthScreen(
    onAccountCreated: (name: String, handle: String, email: String, avatar: String, bio: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val avatars = listOf("🚀", "🦊", "⚡", "🎯", "🏋️", "💡")

    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("🚀") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Minimalist Brand
            Surface(
                shape = CircleShape,
                color = Emerald400.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Emerald400),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Logo",
                        tint = Emerald400,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SquadDo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Accountability to-do with friends",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Minimalist Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .testTag("card_auth_form")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Avatar Picker Row
                    Text(
                        text = "Choose avatar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        avatars.forEach { av ->
                            val isSelected = av == selectedAvatar
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Emerald400.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) Emerald400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedAvatar = av }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = av, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your name") },
                        placeholder = { Text("e.g. Sarah") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Handle Field
                    OutlinedTextField(
                        value = handle,
                        onValueChange = {
                            handle = it.lowercase().replace(" ", "_")
                        },
                        label = { Text("Unique handle") },
                        placeholder = { Text("@sarah") },
                        singleLine = true,
                        supportingText = {
                            Text(
                                text = "Friends find you by this handle",
                                color = Cyan400,
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_handle")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Continue Button
                    Button(
                        onClick = {
                            val cleanName = if (name.isNotBlank()) name.trim() else "You"
                            val cleanHandle = if (handle.isNotBlank()) {
                                if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}"
                            } else "@friend"
                            onAccountCreated(cleanName, cleanHandle, "", selectedAvatar, "Daily focus")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("button_submit_account")
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF022C22)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Demo Button
                    TextButton(
                        onClick = {
                            onAccountCreated("Alex Chen", "@alex_chen", "alex@squaddo.io", "🚀", "Building & fitness")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_quick_demo_account")
                    ) {
                        Text(
                            text = "Quick Demo Access",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "⚡ Real-time sync with friends via Supabase",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
