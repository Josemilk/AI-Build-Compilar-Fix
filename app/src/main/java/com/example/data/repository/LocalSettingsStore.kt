package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Local persistence for settings that must work without Firebase login.
 */
object LocalSettingsStore {
    private const val PREFS = "ai_studio_settings"
    private const val KEY_GEMINI = "gemini_api_key"
    private const val KEY_OPENAI = "openai_api_key"
    private const val KEY_ANTHROPIC = "anthropic_api_key"
    private const val KEY_DEEPSEEK = "deepseek_api_key"
    private const val KEY_CUSTOM_ENDPOINT = "custom_llm_endpoint"
    private const val KEY_MODEL = "selected_model_id"
    private const val KEY_PROVIDER = "selected_llm_provider"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    private fun p(): SharedPreferences? = prefs

    fun getGeminiApiKey(): String = p()?.getString(KEY_GEMINI, "") ?: ""
    fun setGeminiApiKey(value: String) {
        p()?.edit()?.putString(KEY_GEMINI, value)?.apply()
    }

    fun getOpenAiKey(): String = p()?.getString(KEY_OPENAI, "") ?: ""
    fun setOpenAiKey(value: String) {
        p()?.edit()?.putString(KEY_OPENAI, value)?.apply()
    }

    fun getAnthropicKey(): String = p()?.getString(KEY_ANTHROPIC, "") ?: ""
    fun setAnthropicKey(value: String) {
        p()?.edit()?.putString(KEY_ANTHROPIC, value)?.apply()
    }

    fun getDeepseekKey(): String = p()?.getString(KEY_DEEPSEEK, "") ?: ""
    fun setDeepseekKey(value: String) {
        p()?.edit()?.putString(KEY_DEEPSEEK, value)?.apply()
    }

    fun getCustomEndpoint(): String = p()?.getString(KEY_CUSTOM_ENDPOINT, "") ?: ""
    fun setCustomEndpoint(value: String) {
        p()?.edit()?.putString(KEY_CUSTOM_ENDPOINT, value)?.apply()
    }

    fun getSelectedModelId(): String = p()?.getString(KEY_MODEL, "") ?: ""
    fun setSelectedModelId(value: String) {
        p()?.edit()?.putString(KEY_MODEL, value)?.apply()
    }

    fun getSelectedProvider(): String = p()?.getString(KEY_PROVIDER, "") ?: ""
    fun setSelectedProvider(value: String) {
        p()?.edit()?.putString(KEY_PROVIDER, value)?.apply()
    }
}
