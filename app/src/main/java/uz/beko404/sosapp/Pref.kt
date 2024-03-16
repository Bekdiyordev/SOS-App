package uz.beko404.sosapp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Pref {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pref: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        pref = sharedPreferences
    }

    var smsNumber: String
        get() = pref.getString("sms", "+998995006123")!!
        set(value) = pref.edit { putString("sms", value) }

    var isLocationEnabled: Boolean
        get() = pref.getBoolean("location", false)
        set(value) = pref.edit { putBoolean("location", value) }

    var latitude: Float
        get() = pref.getFloat("latitude", 41.841812f)
        set(value) = pref.edit { putFloat("latitude", value) }

    var longitude: Float
        get() = pref.getFloat("longitude", 60.391438f)
        set(value) = pref.edit { putFloat("longitude", value) }

    var locationPermissionFine: Boolean
        get() = pref.getBoolean("locationPermissionFine", false)
        set(value) = pref.edit { putBoolean("locationPermissionFine", value) }

    var isFind: Boolean
        get() = pref.getBoolean("isFind", false)
        set(value) = pref.edit { putBoolean("isFind", value) }

    var time: Long
        get() = pref.getLong("time", 1)
        set(value) = pref.edit { putLong("time", value) }

    var distance: Int
        get() = pref.getInt("distance", 50)
        set(value) = pref.edit { putInt("distance", value) }
}