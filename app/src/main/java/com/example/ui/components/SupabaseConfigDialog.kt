package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SupabaseConfig
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RoseNudge

@Composable
fun SupabaseConfigDialog(
    initialConfig: SupabaseConfig,
    testResult: String?,
    isTesting: Boolean,
    onTestConnection: (url: String, key: String) -> Unit,
    onSave: (url: String, anonKey: String, channel: String, enabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf(initialConfig.url) }
    var anonKey by remember { mutableStateOf(initialConfig.anonKey) }
    var channel by remember { mutableStateOf(initialConfig.channelName) }
    var isEnabled by remember { mutableStateOf(initialConfig.isEnabled) }
    var showSqlSchema by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val sqlSchemaText = """
-- 1. Create profiles table (for user accounts & finding friends by @handle)
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    handle TEXT UNIQUE NOT NULL,
    email TEXT,
    avatar TEXT DEFAULT '🚀',
    bio TEXT,
    streak_days INT DEFAULT 1,
    created_at BIGINT NOT NULL
);

-- 2. Create tasks table
CREATE TABLE IF NOT EXISTS public.tasks (
    id TEXT PRIMARY KEY,
    squad_id TEXT NOT NULL DEFAULT 'squad_general',
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    user_avatar TEXT DEFAULT '👤',
    title TEXT NOT NULL,
    description TEXT,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    completed_at BIGINT,
    created_at BIGINT NOT NULL,
    priority TEXT DEFAULT 'MEDIUM',
    category TEXT DEFAULT 'WORK',
    due_date TEXT,
    proof_note TEXT,
    is_pact BOOLEAN DEFAULT false,
    cheer_count INT DEFAULT 0
);

-- 3. Create squad_activities table
CREATE TABLE IF NOT EXISTS public.squad_activities (
    id TEXT PRIMARY KEY,
    squad_id TEXT NOT NULL DEFAULT 'squad_general',
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    user_avatar TEXT DEFAULT '👤',
    action_type TEXT NOT NULL,
    target_user_name TEXT,
    message TEXT NOT NULL,
    timestamp BIGINT NOT NULL
);

-- 4. ENABLE SUPABASE REALTIME (Crucial for low-latency sync)
ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks;
ALTER PUBLICATION supabase_realtime ADD TABLE public.squad_activities;
ALTER PUBLICATION supabase_realtime ADD TABLE public.profiles;
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("supabase_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Supabase Realtime Sync",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Low-latency WebSocket sync for accountability",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan400,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Realtime Sync Enable Switch
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Supabase Sync",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Connect to your Supabase Postgres & Realtime channel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Emerald400,
                                checkedTrackColor = Emerald400.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("switch_supabase_enable")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Project URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supabase_url")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Anon Key
                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it },
                    label = { Text("Supabase Anon Public API Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supabase_key")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Channel Name
                OutlinedTextField(
                    value = channel,
                    onValueChange = { channel = it },
                    label = { Text("Realtime Channel Name") },
                    placeholder = { Text("squad_accountability") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection Button & Result
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onTestConnection(url, anonKey) },
                        enabled = !isTesting && url.isNotBlank() && anonKey.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("button_test_supabase")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...")
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Connection")
                        }
                    }
                }

                if (testResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (testResult.startsWith("✅")) OnlineGreen.copy(alpha = 0.15f) else RoseNudge.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (testResult.startsWith("✅")) OnlineGreen.copy(alpha = 0.5f) else RoseNudge.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Collapsible SQL Schema Helper
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSqlSchema = !showSqlSchema }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📋 Supabase SQL Schema & Publication",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (showSqlSchema) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }

                if (showSqlSchema) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (copiedNotice) "Copied to clipboard! ✅" else "Run in Supabase SQL Editor:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (copiedNotice) Emerald400 else Cyan400
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sqlSchemaText))
                                        copiedNotice = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy SQL",
                                        tint = Cyan400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sqlSchemaText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(url, anonKey, channel, isEnabled) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("button_save_supabase")
                    ) {
                        Text(
                            text = "Save & Connect ⚡",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF022C22)
                        )
                    }
                }
            }
        }
    }
}
