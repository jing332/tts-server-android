package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplaceRuleDao {
    @get:Query("select * from ReplaceRule ORDER BY `order` ASC")
    val all: List<ReplaceRule>

    @get:Query("select * from ReplaceRule WHERE isEnabled = '1' ORDER BY `order` ASC")
    val allEnabled: List<ReplaceRule>

    @Query("select * from ReplaceRule ORDER BY `order` ASC")
    fun flowAll(): Flow<List<ReplaceRule>>

    @get:Query("select count(*) from ReplaceRule")
    val count: Int

    @Query("select * from ReplaceRule where id = :id")
    fun get(id: Long): ReplaceRule?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: ReplaceRule)

    @Delete
    fun delete(vararg data: ReplaceRule)

    @Update
    fun update(vararg data: ReplaceRule)
}