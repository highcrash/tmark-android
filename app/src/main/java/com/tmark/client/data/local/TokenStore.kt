package com.tmark.client.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tmark_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_CLIENT_ID     = stringPreferencesKey("client_id")
        private val KEY_CLIENT_NAME   = stringPreferencesKey("client_name")
        private val KEY_CLIENT_PHONE  = stringPreferencesKey("client_phone")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val clientName: Flow<String?>  = context.dataStore.data.map { it[KEY_CLIENT_NAME] }
    val clientId: Flow<String?>    = context.dataStore.data.map { it[KEY_CLIENT_ID] }
    val clientPhone: Flow<String?> = context.dataStore.data.map { it[KEY_CLIENT_PHONE] }

    suspend fun saveAuth(token: String, clientId: String, clientName: String, phone: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
            prefs[KEY_CLIENT_ID]    = clientId
            prefs[KEY_CLIENT_NAME]  = clientName
            if (phone != null) prefs[KEY_CLIENT_PHONE] = phone
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_CLIENT_ID)
            prefs.remove(KEY_CLIENT_NAME)
            prefs.remove(KEY_CLIENT_PHONE)
        }
    }

    suspend fun getToken(): String? {
        var token: String? = null
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }.collect { token = it; }
        return token
    }
}
