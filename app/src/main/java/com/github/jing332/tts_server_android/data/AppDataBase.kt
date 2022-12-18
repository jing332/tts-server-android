package com.github.jing332.tts_server_android.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.dao.ReplaceRuleDao
import com.github.jing332.tts_server_android.data.dao.SystemTtsDao
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

val appDb by lazy { AppDatabase.createDatabase(App.context) }

@Database(
    version = 5,
    entities = [
        SystemTts::class, SystemTtsGroup::class,
        ReplaceRule::class
    ],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract val replaceRuleDao: ReplaceRuleDao
    abstract val systemTtsDao: SystemTtsDao

    companion object {
        private const val DATABASE_NAME = "systts.db"

        fun createDatabase(context: Context) = Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
    }
}