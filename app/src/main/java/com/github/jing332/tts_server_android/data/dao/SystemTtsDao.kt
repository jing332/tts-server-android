package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup.Companion.DEFAULT_GROUP_ID
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
    @get:Query("SELECT * FROM SystemTtsGroup ORDER BY `order` ASC")
    val flowAllGroupWithTts: Flow<List<GroupWithTtsItem>>

    @Query("SELECT * FROM SystemTtsGroup WHERE groupId = :id")
    fun getGroupById(id: Long = DEFAULT_GROUP_ID): SystemTtsGroup?

    @Query("SELECT * FROM sysTts WHERE groupId = :groupId")
    fun getTtsListByGroupId(groupId: Long): List<SystemTts>

    /**
     * 所有TTS 是否启用
     */
    @Query("UPDATE sysTts SET isEnabled = :isEnabled")
    fun setAllTtsEnabled(isEnabled: Boolean)

    /**
     * 设置某个组中的所有TTS 是否启用
     */
    @Query("UPDATE sysTts SET isEnabled = :isEnabled WHERE groupId = :groupId")
    fun setTtsEnabledInGroup(groupId: Long, isEnabled: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTts(vararg items: SystemTts)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTts(vararg items: SystemTts)

    @Delete
    fun deleteTts(vararg items: SystemTts)

    @Query("DELETE from sysTts WHERE groupId = :groupId")
    fun deleteTtsByGroup(groupId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(vararg group: SystemTtsGroup)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateGroup(group: SystemTtsGroup)

    @Delete
    fun deleteGroup(group: SystemTtsGroup)

    @get:Transaction
    @get:Query("SELECT * FROM SystemTtsGroup ORDER BY `order` ASC")
    val sysTtsWithGroups: List<GroupWithTtsItem>

    /**
     * 删除组以及TTS
     */
    fun deleteGroupAndTts(group: SystemTtsGroup) {
        deleteTtsByGroup(group.id)
        deleteGroup(group)
    }
}