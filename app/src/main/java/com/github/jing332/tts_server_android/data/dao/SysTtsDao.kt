/*
package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.SysTts
import kotlinx.coroutines.flow.Flow

@Dao
interface SysTtsDao {
    @get:Query("select * from SysTts ORDER BY readAloudTarget ASC")
    val all: List<SysTts>

    @Query("select * from SysTts ORDER BY readAloudTarget ASC")
    fun flowAll(): Flow<List<SysTts>>

    @get:Query("select count(*) from SysTts")
    val count: Int

    @Query("select * from SysTts where id = :id")
    fun get(id: Long): SysTts?

    @Query("select * from SysTts where readAloudTarget = :target and isEnabled = '1'")
    fun getByReadAloudTarget(target: Int = ReadAloudTarget.ALL): SysTts?

    @Query("select * from sysTts where readAloudTarget = :target and isEnabled = '1'")
    fun getAllByReadAloudTarget(target: Int = ReadAloudTarget.ALL): List<SysTts>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg tts: SysTts)

    @Delete
    fun delete(vararg tts: SysTts)

    @Update
    fun update(vararg tts: SysTts)

    @Query("delete from SysTts where id < 0")
    fun deleteDefault()
}*/
