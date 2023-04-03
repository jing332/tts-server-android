package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.ReadRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadRuleDao {
    @get:Query("SELECT * FROM readRules ORDER BY `order` ASC")
    val all: List<ReadRule>

    @get:Query("SELECT * FROM readRules WHERE isEnabled = '1'")
    val allEnabled: List<ReadRule>

    @Query("SELECT * FROM readRules ORDER BY `order` ASC")
    fun flowAll(): Flow<List<ReadRule>>

    @get:Query("SELECT count(*) FROM readRules")
    val count: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: ReadRule)

    @Delete
    fun delete(vararg data: ReadRule)

    @Update
    fun update(vararg data: ReadRule)

    @Query("SELECT * FROM readRules WHERE ruleId = :ruleId ")
    fun getByReadRuleId(ruleId: String): ReadRule?
}