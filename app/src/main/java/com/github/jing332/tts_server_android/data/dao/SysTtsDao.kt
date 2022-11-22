package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.SysTts
import kotlinx.coroutines.flow.Flow

@Dao
interface SysTtsDao {
    @get:Query("select * from SysTts ORDER BY read_aloud_target ASC")
    val all: List<SysTts>

    @Query("select * from SysTts ORDER BY read_aloud_target ASC")
    fun flowAll(): Flow<List<SysTts>>

    @get:Query("select count(*) from SysTts")
    val count: Int

    @Query("select * from SysTts where id = :id")
    fun get(id: Long): SysTts?

    @Query("select * from SysTts where read_aloud_target = :target and is_enabled = '1'")
    fun getByReadAloudTarget(target: Int = ReadAloudTarget.DEFAULT): SysTts?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg httpTTS: SysTts)

    @Delete
    fun delete(vararg httpTTS: SysTts)

    @Update
    fun update(vararg httpTTS: SysTts)

    @Query("delete from SysTts where id < 0")
    fun deleteDefault()
}