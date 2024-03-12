package uz.beko404.sosapp

import android.content.Context
import android.content.SharedPreferences

object Pref {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pref: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        pref = sharedPreferences
    }

    fun getPreferences(): SharedPreferences {
        return sharedPreferences
    }

    fun setNumber(number: String) {
        pref.edit().apply {
            putString("number", number)
            apply()
        }
    }

    fun getNumber(): String {
        return pref.getString("number", "+998995006123")!!
    }

    fun setSMSNumber(number: String) {
        pref.edit().apply {
            putString("sms", number)
            apply()
        }
    }

    fun getSMSNumber(): String {
        return pref.getString("sms", "+998995006123")!!
    }

    fun setLocationEnabled(state: Boolean) {
        pref.edit().apply {
            putBoolean("location", state)
            apply()
        }
    }

    fun isLocationEnabled(): Boolean {
        return pref.getBoolean("location", false)
    }
}