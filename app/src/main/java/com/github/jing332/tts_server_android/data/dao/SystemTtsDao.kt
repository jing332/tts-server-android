package com.github.jing332.tts_server_android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemTtsDao {
    @get:Query("SELECT * FROM sysTts")
    val allTts: List<SystemTts>

    @get:Query("SELECT * FROM sysTts")
    val flowAllTts: Flow<List<SystemTts>>

    @Query("SELECT * FROM sysTts WHERE readAloudTarget = :target AND isEnabled = '1'")
    fun getAllByReadAloudTarget(target: Int = ReadAloudTarget.ALL): List<SystemTts>?

    @get:Query("SELECT * FROM SystemTtsGroup")
    val allGroup: List<SystemTtsGroup>

    @get:Transaction
    @get:Query("SELECT * FROM SystemTtsGroup")
    val flowAllGroupWithTts: Flow<List<GroupWithTtsItem>>

    @Query("SELECT * FROM SystemTtsGroup WHERE groupId = :id")
    fun getGroupById(id: Long = DEFAULT_GROUP_ID): SystemTtsGroup?

    @Insert
    fun insertTts(vararg items: SystemTts)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTts(vararg items: SystemTts)

    @Delete
    fun deleteTts(vararg items: SystemTts)

    @Insert
    fun insertGroup(group: SystemTtsGroup)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateGroup(group: SystemTtsGroup)

    @Delete
    fun deleteGroup(group: SystemTtsGroup)

    @Transaction
    @Query("SELECT * FROM SystemTtsGroup")
    fun getSysTtsWithGroups(): List<GroupWithTtsItem>

    /**
     * 插入TTS 如果不存在默认组则插入一个
     */
    fun insertTtsIfDefault(tts: SystemTts) {
        insertTts(tts)
    }
}