package com.shohan.khatago

import android.content.Context
import com.shohan.khatago.data.KhataGoRepository
import com.shohan.khatago.data.local.KhataGoDatabase
import com.shohan.khatago.data.preferences.AppPreferences

class AppContainer(context: Context) {
    val database: KhataGoDatabase = KhataGoDatabase.create(context)
    val dao = database.dao()
    val preferences = AppPreferences(context)
    val repository = KhataGoRepository(database, dao)
}
