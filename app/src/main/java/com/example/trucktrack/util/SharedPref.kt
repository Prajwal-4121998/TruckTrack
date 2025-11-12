package com.example.trucktrack.util

import android.content.Context
import android.content.SharedPreferences
import com.example.trucktrack.R
import com.google.gson.Gson

class SharedPref(context: Context) {

    private var sharedPref: SharedPreferences = context.getSharedPreferences(
        context.resources.getString(R.string.app_pref_name),
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    fun put(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun put(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun put(key: String, data: Any) {
        return put(key, Gson().toJson(data))
    }

    fun getInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun getBooleanTrue(key: String): Boolean {
        return sharedPref.getBoolean(key, true)
    }

    fun getString(key: String): String {
        return sharedPref.getString(key, "")!!
    }

    fun getObject(key: String, classObject: Class<*>): Any {
        return Gson().fromJson(getString(key), classObject)
    }

//    fun clear() {
//        editor.remove(KEY_LOGGED_IN).apply()
//    }
}