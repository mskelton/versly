package dev.mskelton.versly.persistence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = SettingsViewModel.Factory::class)
class SettingsViewModel @AssistedInject constructor(
    private val bibleDatabase: BibleDatabase,
    @Assisted val navKey: Settings,
) : ViewModel() {
    private val _translations = MutableStateFlow<List<Translation>>(emptyList())
    val translations: StateFlow<List<Translation>> = _translations.asStateFlow()

    private val _loadingTranslation = MutableStateFlow<String?>(null)
    val loadingTranslation: StateFlow<String?> = _loadingTranslation.asStateFlow()

    init {
        loadTranslations()
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            _translations.value =
                withContext(Dispatchers.IO) { bibleDatabase.getAvailableTranslations() }
        }
    }

    fun downloadTranslation(translationId: String) {
        viewModelScope.launch {
            _loadingTranslation.value = translationId
            withContext(Dispatchers.IO) { bibleDatabase.downloadTranslation(translationId) }
            _loadingTranslation.value = null
            loadTranslations()
        }
    }

    fun deleteTranslation(translationId: String) {
        viewModelScope.launch {
            _loadingTranslation.value = translationId
            withContext(Dispatchers.IO) { bibleDatabase.deleteTranslation(translationId) }
            _loadingTranslation.value = null
            loadTranslations()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: Settings): SettingsViewModel
    }
}
