package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplaceRuleDao {
    @get:Query("select * from ReplaceRule ORDER BY isEnabled ASC")
    val all: List<ReplaceRule>

    @Query("select * from ReplaceRule ORDER BY isEnabled ASC")
    fun flowAll(): Flow<List<ReplaceRule>>

    @get:Query("select count(*) from ReplaceRule")
    val count: Int

    @Query("select * from ReplaceRule where id = :id")
    fun get(id: Long): ReplaceRule?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg httpTTS: ReplaceRule)

    @Delete
    fun delete(vararg httpTTS: ReplaceRule)

    @Update
    fun update(vararg httpTTS: ReplaceRule)

    @Query("delete from ReplaceRule where id < 0")
    fun deleteDefault()

}