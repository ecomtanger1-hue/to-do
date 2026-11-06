package com.example.data.remote

import android.util.Log
import com.example.data.model.SquadActivity
import com.example.data.model.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRestClient {
    private val tag = "SupabaseRestClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun upsertTask(configUrl: String, anonKey: String, task: TaskItem): Boolean = withContext(Dispatchers.IO) {
        if (configUrl.isBlank() || anonKey.isBlank()) return@withContext false
        try {
            val endpoint = "${configUrl.removeSuffix("/")}/rest/v1/tasks"
            val bodyJson = JSONObject().apply {
                put("id", task.id)
                put("squad_id", task.squadId)
                put("user_id", task.userId)
                put("user_name", task.userName)
                put("user_avatar", task.userAvatar)
                put("title", task.title)
                put("description", task.description)
                put("is_completed", task.isCompleted)
                put("completed_at", task.completedAt ?: JSONObject.NULL)
                put("created_at", task.createdAt)
                put("priority", task.priority.name)
                put("category", task.category.name)
                put("due_date", task.dueDate)
                put("proof_note", task.proofNote ?: JSONObject.NULL)
                put("is_pact", task.isPact)
                put("cheer_count", task.cheerCount)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            isSuccess
        } catch (e: Exception) {
            Log.e(tag, "Failed to upsert task to Supabase: ${e.message}")
            false
        }
    }

    suspend fun logActivity(configUrl: String, anonKey: String, activity: SquadActivity): Boolean = withContext(Dispatchers.IO) {
        if (configUrl.isBlank() || anonKey.isBlank()) return@withContext false
        try {
            val endpoint = "${configUrl.removeSuffix("/")}/rest/v1/squad_activities"
            val bodyJson = JSONObject().apply {
                put("id", activity.id)
                put("squad_id", activity.squadId)
                put("user_id", activity.userId)
                put("user_name", activity.userName)
                put("user_avatar", activity.userAvatar)
                put("action_type", activity.actionType.name)
                put("target_user_name", activity.targetUserName ?: JSONObject.NULL)
                put("message", activity.message)
                put("timestamp", activity.timestamp)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            isSuccess
        } catch (e: Exception) {
            Log.e(tag, "Failed to log activity to Supabase: ${e.message}")
            false
        }
    }

    suspend fun testConnection(configUrl: String, anonKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (configUrl.isBlank() || anonKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("URL and Anon Key cannot be empty"))
        }
        try {
            val endpoint = "${configUrl.removeSuffix("/")}/rest/v1/"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            response.close()
            if (code in 200..299 || code == 404) {
                // 200 or 404 on root OpenAPI means the Supabase server answered and the key is accepted
                Result.success("Connection successful! Server reached (HTTP $code)")
            } else {
                Result.failure(Exception("Supabase returned HTTP $code. Check your URL and Anon Key."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
