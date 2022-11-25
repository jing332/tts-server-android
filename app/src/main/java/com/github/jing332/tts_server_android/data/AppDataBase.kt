package com.github.jing332.tts_server_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.dao.ReplaceRuleDao
import com.github.jing332.tts_server_android.data.dao.SysTtsDao
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.SysTts

val appDb by lazy {
    AppDatabase.createDatabase(App.context)
}

@Database(version = 3, entities = [SysTts::class, ReplaceRule::class])
abstract class AppDatabase : RoomDatabase() {
    abstract val sysTtsDao: SysTtsDao
    abstract val replaceRuleDao: ReplaceRuleDao

    companion object {
        private const val DATABASE_NAME = "systts.db"

        fun createDatabase(context: Context) = Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
    }
}