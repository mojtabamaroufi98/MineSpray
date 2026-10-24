package com.minespray.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minespray.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MineViewModel(private val repo: MineRepository = MockMineRepository()) : ViewModel() {
    val state: StateFlow<MineState> = repo.state
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init { viewModelScope.launch { while (true) { delay(_settings.value.refreshSec * 1000L); repo.tick() } } }

    fun updateSettings(f: (AppSettings) -> AppSettings) = _settings.update(f)
    fun setLang(l: String) = updateSettings { it.copy(lang = l) }
    fun setScenario(s: Scenario) = repo.setScenario(s)
    fun setAuto(on: Boolean) = repo.setAuto(on)
    fun manualSpray(zoneId: Int) = repo.manualSpray(zoneId)
    fun stopSpray(zoneId: Int) = repo.stopSpray(zoneId)
}
