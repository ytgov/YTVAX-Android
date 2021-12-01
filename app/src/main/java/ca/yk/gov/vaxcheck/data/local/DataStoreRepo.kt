package ca.yk.gov.vaxcheck.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.yk.gov.vaxcheck.BuildConfig
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [DataStoreRepo]
 *
 * @author amit metri
 */
private val Context.dataStore by preferencesDataStore(BuildConfig.APPLICATION_ID + "_preferences")

class DataStoreRepo @Inject constructor(
    private val context: Context
) {

    companion object {
        val ON_BOARDING_SHOWN = booleanPreferencesKey("ON_BOARDING_SHOWN")
        val SET_SELECTED_LANGUAGE = stringPreferencesKey("SET_SELECTED_LANGUAGE")

    }

    val isOnBoardingShown: Flow<Boolean> = context.dataStore.data.map { preference ->
        preference[ON_BOARDING_SHOWN] ?: false
    }

    suspend fun setOnBoardingShown(shown: Boolean = true) = context.dataStore.edit { preference ->
        preference[ON_BOARDING_SHOWN] = shown
    }

    val getSelectedLanguage: Flow<String> = context.dataStore.data.map { preference ->
        preference[SET_SELECTED_LANGUAGE] ?: ""
    }

    suspend fun setSelectedLanguage(languageCode: String) = context.dataStore.edit { preference ->
        preference[SET_SELECTED_LANGUAGE] = languageCode
    }

}
