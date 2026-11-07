package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SupabaseConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "squaddo_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_HANDLE = stringPreferencesKey("user_handle")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_AVATAR = stringPreferencesKey("user_avatar")
        private val KEY_USER_BIO = stringPreferencesKey("user_bio")
        private val KEY_SQUAD_ID = stringPreferencesKey("squad_id")
        private val KEY_SUPABASE_URL = stringPreferencesKey("supabase_url")
        private val KEY_SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        private val KEY_SUPABASE_CHANNEL = stringPreferencesKey("supabase_channel")
        private val KEY_SUPABASE_ENABLED = booleanPreferencesKey("supabase_enabled")
        private val KEY_SIMULATOR_ENABLED = booleanPreferencesKey("simulator_enabled")
    }

    private val safePrefsFlow = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    val isLoggedInFlow: Flow<Boolean> = safePrefsFlow.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userIdFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_ID] ?: "user_me"
    }

    val userNameFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_NAME] ?: "You"
    }

    val userHandleFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_HANDLE] ?: "@pact_hero"
    }

    val userEmailFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userAvatarFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_AVATAR] ?: "🚀"
    }

    val userBioFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_USER_BIO] ?: "Daily Accountability Partner"
    }

    val squadIdFlow: Flow<String> = safePrefsFlow.map { prefs ->
        prefs[KEY_SQUAD_ID] ?: "squad_general"
    }

    val supabaseConfigFlow: Flow<SupabaseConfig> = safePrefsFlow.map { prefs ->
        SupabaseConfig(
            url = prefs[KEY_SUPABASE_URL] ?: "",
            anonKey = prefs[KEY_SUPABASE_ANON_KEY] ?: "",
            channelName = prefs[KEY_SUPABASE_CHANNEL] ?: "squad_accountability",
            isEnabled = prefs[KEY_SUPABASE_ENABLED] ?: false
        )
    }

    val simulatorEnabledFlow: Flow<Boolean> = safePrefsFlow.map { prefs ->
        prefs[KEY_SIMULATOR_ENABLED] ?: true
    }

    suspend fun saveUserAccount(
        userId: String,
        name: String,
        handle: String,
        email: String,
        avatar: String,
        bio: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_HANDLE] = if (handle.startsWith("@")) handle else "@$handle"
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_AVATAR] = avatar
            prefs[KEY_USER_BIO] = bio
        }
    }

    suspend fun saveUserProfile(userId: String, name: String, avatar: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_AVATAR] = avatar
        }
    }

    suspend fun logOut() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
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
