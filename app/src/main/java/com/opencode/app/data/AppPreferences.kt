package com.opencode.app.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("opencode", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString("url", "http://10.0.2.2:4096") ?: "http://10.0.2.2:4096"
        set(v) = prefs.edit().putString("url", v).apply()

    var password: String
        get() = prefs.getString("pass", "") ?: ""
        set(v) = prefs.edit().putString("pass", v).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark", false)
        set(v) = prefs.edit().putBoolean("dark", v).apply()

    var activeModel: String
        get() = prefs.getString("model", "claude-sonnet") ?: "claude-sonnet"
        set(v) = prefs.edit().putString("model", v).apply()
}
