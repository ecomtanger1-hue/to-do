package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SupabaseConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "squaddo_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_AVATAR = stringPreferencesKey("user_avatar")
        private val KEY_SQUAD_ID = stringPreferencesKey("squad_id")
        private val KEY_SUPABASE_URL = stringPreferencesKey("supabase_url")
        private val KEY_SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        private val KEY_SUPABASE_CHANNEL = stringPreferencesKey("supabase_channel")
        private val KEY_SUPABASE_ENABLED = booleanPreferencesKey("supabase_enabled")
        private val KEY_SIMULATOR_ENABLED = booleanPreferencesKey("simulator_enabled")
    }

    val userIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: "user_me"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "You"
    }

    val userAvatarFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_AVATAR] ?: "🚀"
    }

    val squadIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SQUAD_ID] ?: "squad_general"
    }

    val supabaseConfigFlow: Flow<SupabaseConfig> = context.dataStore.data.map { prefs ->
        SupabaseConfig(
            url = prefs[KEY_SUPABASE_URL] ?: "",
            anonKey = prefs[KEY_SUPABASE_ANON_KEY] ?: "",
            channelName = prefs[KEY_SUPABASE_CHANNEL] ?: "squad_accountability",
            isEnabled = prefs[KEY_SUPABASE_ENABLED] ?: false
        )
    }

    val simulatorEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SIMULATOR_ENABLED] ?: true // Enabled by default so the user immediately sees active peers!
    }

    suspend fun saveUserProfile(userId: String, name: String, avatar: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_AVATAR] = avatar
        }
    }

    suspend fun saveSupabaseConfig(url: String, anonKey: String, channel: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SUPABASE_URL] = url.trim()
            prefs[KEY_SUPABASE_ANON_KEY] = anonKey.trim()
            prefs[KEY_SUPABASE_CHANNEL] = channel.trim().ifEmpty { "squad_accountability" }
            prefs[KEY_SUPABASE_ENABLED] = enabled
        }
    }

    suspend fun setSimulatorEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SIMULATOR_ENABLED] = enabled
        }
    }
}
