package com.github.jing332.tts_server_android.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.dao.PluginDao
import com.github.jing332.tts_server_android.data.dao.SpeechRuleDao
import com.github.jing332.tts_server_android.data.dao.ReplaceRuleDao
import com.github.jing332.tts_server_android.data.dao.SystemTtsDao
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

val appDb by lazy { AppDatabase.createDatabase(App.context) }

@Database(
    version = 24,
    entities = [
        SystemTts::class,
        SystemTtsGroup::class,
        ReplaceRule::class,
        ReplaceRuleGroup::class,
        Plugin::class,
        SpeechRule::class,
    ],
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13, AppDatabase.DeleteSystemTtsColumn::class),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        // 15-16
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract val replaceRuleDao: ReplaceRuleDao
    abstract val systemTtsDao: SystemTtsDao
    abstract val pluginDao: PluginDao
    abstract val speechRuleDao: SpeechRuleDao

    companion object {
        private const val DATABASE_NAME = "systts.db"

        fun createDatabase(context: Context) = Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(*DataBaseMigration.migrations)
            .build()
    }

    @DeleteColumn(tableName = "sysTts", columnName = "isBgm")
    class DeleteSystemTtsColumn : AutoMigrationSpec
}