package com.auto.click.appcomponents.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.auto.click.R
import java.io.IOException
import java.security.GeneralSecurityException

object PreferenceHelper {
    private lateinit var masterKeyAlias: String
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        masterKeyAlias = createGetMasterKey()
        sharedPreferences = createEncryptedSharedPreferences(context)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun createGetMasterKey(): String {
        return MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        val appName: String = context.getString(R.string.app_name)
        return EncryptedSharedPreferences.create(
            appName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Các phương thức để đọc và ghi dữ liệu vào SharedPreferences
    fun putString(key: String?, value: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String?, defaultValue: String?): String {
        return sharedPreferences.getString(key, defaultValue)!!
    }

    fun putBoolean(key: String?, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putFloat(key: String?, value: Float) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getFloat(key: String?, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putInt(key: String?, value: Int) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putLong(key: String?, value: Long) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun putStringSet(key: String?, value: Set<String?>?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    fun getStringSet(key: String?, defaultValue: Set<String?>?): MutableSet<String>? {
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    fun removeKey(key: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }
}
