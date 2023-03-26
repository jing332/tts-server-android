package com.github.jing332.tts_server_android.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.dao.PluginDao
import com.github.jing332.tts_server_android.data.dao.ReplaceRuleDao
import com.github.jing332.tts_server_android.data.dao.SystemTtsDao
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

val appDb by lazy { AppDatabase.createDatabase(App.context) }

@Database(
    version = 12,
    entities = [
        SystemTts::class, SystemTtsGroup::class,
        ReplaceRule::class, ReplaceRuleGroup::class,
        Plugin::class,
    ],
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract val replaceRuleDao: ReplaceRuleDao
    abstract val systemTtsDao: SystemTtsDao
    abstract val pluginDao: PluginDao

    companion object {
        private const val DATABASE_NAME = "systts.db"

        fun createDatabase(context: Context) = Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
    }
}