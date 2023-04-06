package com.github.jing332.tts_server_android.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DataBaseMigration {
    val migrations: Array<Migration> by lazy {
        arrayOf(migration_15_16)
    }

    private val migration_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //language=RoomSql
            database.apply {
                execSQL("ALTER TABLE sysTts ADD COLUMN speechRule_tagRuleId TEXT NOT NULL DEFAULT ''")
                execSQL("ALTER TABLE sysTts ADD COLUMN speechRule_tag TEXT NOT NULL DEFAULT ''")
                // 移动到嵌套对象
                execSQL("ALTER TABLE sysTts RENAME readAloudTarget TO speechRule_target")
                execSQL("ALTER TABLE sysTts RENAME isStandby TO speechRule_isStandby")
            }
        }
    }
}