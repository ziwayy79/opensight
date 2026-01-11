package com.opensight.sift.features.sensoryshield

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sensory Shield Manager - A feature designed for neurodiversity and urban stress relief.
 * 
 * This manager controls a frequency-selective audio filtering system that:
 * - Dampens chaotic high-frequency noises (train screeches, construction, etc.)
 * - Amplifies "safe" human voices for conversation
 * - Preserves emergency sirens and safety announcements
 */
class SensoryShieldManager(private val context: Context) {

    companion object {
        private const val TAG = "SensoryShieldManager"
        private const val PREFS_NAME = "sensory_shield_settings"
        
        // Frequency filtering presets
        const val PRESET_TRANSIT = "transit"
        const val PRESET_SHOPPING = "shopping"
        const val PRESET_OFFICE = "office"
        const val PRESET_CUSTOM = "custom"
        
        // Default damping levels
        const val DEFAULT_HIGH_FREQ_DAMPING = 0.3f
        const val DEFAULT_MID_FREQ_BOOST = 1.2f
        const val DEFAULT_LOW_FREQ_DAMPING = 0.7f
    }

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isEnabled = MutableStateFlow(sharedPreferences.getBoolean("enabled", false))
    val isEnabled: StateFlow<Boolean> = _isEnabled

    private val _currentPreset = MutableStateFlow(sharedPreferences.getString("preset", PRESET_TRANSIT) ?: PRESET_TRANSIT)
    val currentPreset: StateFlow<String> = _currentPreset

    private val _highFreqDamping = MutableStateFlow(sharedPreferences.getFloat("high_freq_damping", DEFAULT_HIGH_FREQ_DAMPING))
    val highFreqDamping: StateFlow<Float> = _highFreqDamping

    private val _midFreqBoost = MutableStateFlow(sharedPreferences.getFloat("mid_freq_boost", DEFAULT_MID_FREQ_BOOST))
    val midFreqBoost: StateFlow<Float> = _midFreqBoost

    private val _lowFreqDamping = MutableStateFlow(sharedPreferences.getFloat("low_freq_damping", DEFAULT_LOW_FREQ_DAMPING))
    val lowFreqDamping: StateFlow<Float> = _lowFreqDamping

    private val _voiceAmplification = MutableStateFlow(sharedPreferences.getFloat("voice_amplification", 1.0f))
    val voiceAmplification: StateFlow<Float> = _voiceAmplification

    private val _preserveEmergencySounds = MutableStateFlow(sharedPreferences.getBoolean("preserve_emergency", true))
    val preserveEmergencySounds: StateFlow<Boolean> = _preserveEmergencySounds

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        sharedPreferences.edit().putBoolean("enabled", enabled).apply()
    }

    fun setPreset(preset: String) {
        _currentPreset.value = preset
        sharedPreferences.edit().putString("preset", preset).apply()
        
        when (preset) {
            PRESET_TRANSIT -> applyTransitPreset()
            PRESET_SHOPPING -> applyShoppingPreset()
            PRESET_OFFICE -> applyOfficePreset()
        }
    }

    private fun applyTransitPreset() {
        _highFreqDamping.value = 0.2f
        _midFreqBoost.value = 1.3f
        _lowFreqDamping.value = 0.5f
        _voiceAmplification.value = 1.2f
        saveSettings()
    }

    private fun applyShoppingPreset() {
        _highFreqDamping.value = 0.4f
        _midFreqBoost.value = 1.1f
        _lowFreqDamping.value = 0.6f
        _voiceAmplification.value = 1.1f
        saveSettings()
    }

    private fun applyOfficePreset() {
        _highFreqDamping.value = 0.5f
        _midFreqBoost.value = 1.0f
        _lowFreqDamping.value = 0.4f
        _voiceAmplification.value = 1.0f
        saveSettings()
    }

    fun setCustomSettings(
        highFreqDamping: Float? = null,
        midFreqBoost: Float? = null,
        lowFreqDamping: Float? = null,
        voiceAmplification: Float? = null
    ) {
        highFreqDamping?.let { _highFreqDamping.value = it.coerceIn(0f, 1f) }
        midFreqBoost?.let { _midFreqBoost.value = it.coerceIn(0.5f, 2f) }
        lowFreqDamping?.let { _lowFreqDamping.value = it.coerceIn(0f, 1f) }
        voiceAmplification?.let { _voiceAmplification.value = it.coerceIn(0.5f, 2f) }
        _currentPreset.value = PRESET_CUSTOM
        saveSettings()
    }

    private fun saveSettings() {
        sharedPreferences.edit().apply {
            putFloat("high_freq_damping", _highFreqDamping.value)
            putFloat("mid_freq_boost", _midFreqBoost.value)
            putFloat("low_freq_damping", _lowFreqDamping.value)
            putFloat("voice_amplification", _voiceAmplification.value)
            apply()
        }
    }

    fun getPresetDescription(preset: String): String {
        return when (preset) {
            PRESET_TRANSIT -> "Optimized for subway and train environments. Reduces screeching brakes and metal sounds while keeping announcements clear."
            PRESET_SHOPPING -> "Designed for malls and crowded spaces. Reduces general noise while preserving conversations."
            PRESET_OFFICE -> "Tuned for open offices. Reduces keyboard sounds and HVAC noise while maintaining speech clarity."
            PRESET_CUSTOM -> "Your personalized settings for frequency filtering."
            else -> "Select a preset for your environment."
        }
    }
}
