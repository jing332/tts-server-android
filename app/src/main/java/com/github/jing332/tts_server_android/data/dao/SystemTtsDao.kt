package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemTtsDao {
    @get:Query("SELECT * FROM sysTts")
    val allTts: List<SystemTts>

    @get:Query("SELECT count(*) FROM sysTts")
    val ttsCount: Int

    @get:Query("SELECT * FROM sysTts")
    val flowAllTts: Flow<List<SystemTts>>

    @get:Query("SELECT count(speechRule_isStandby = '1') FROM sysTts")
    val standbyTtsCount: Int

    @Query("SELECT * FROM sysTts WHERE isEnabled = '1' AND  speechRule_target = :target AND speechRule_isStandby = :isStandbyType")
    fun getEnabledList(
        target: Int = SpeechTarget.ALL,
        isStandbyType: Boolean = false
    ): List<SystemTts>

    @Query("SELECT * FROM sysTts WHERE isEnabled = '1' AND  speechRule_target = :target AND speechRule_isStandby = :isStandbyType AND groupId = :groupId")
    fun getEnabledListByGroupId(
        groupId: Long,
        target: Int = SpeechTarget.ALL,
        isStandbyType: Boolean = false,
    ): List<SystemTts>

    @Query("SELECT * FROM sysTts WHERE groupId = :groupId")
    fun getTtsByGroup(groupId: Long): List<SystemTts>

    @Query("SELECT * FROM sysTts WHERE id = :id")
    fun getTts(id: Long): SystemTts?

    @get:Query("SELECT * FROM sysTts WHERE isEnabled = '1'")
    val allEnabledTts: List<SystemTts>

    @get:Query("SELECT * FROM SystemTtsGroup ORDER by `order` ASC")
    val allGroup: List<SystemTtsGroup>

    @get:Query("SELECT count(*) FROM SystemTtsGroup")
    val groupCount: Int

    @Transaction
    @Query("SELECT * FROM SystemTtsGroup ORDER BY `order` ASC")
    fun getAllGroupWithTts(): List<GroupWithSystemTts>

    @Transaction
    @Query("SELECT * FROM SystemTtsGroup ORDER BY `order` ASC")
    fun getFlowAllGroupWithTts(): Flow<List<GroupWithSystemTts>>

    @Query("SELECT * FROM SystemTtsGroup WHERE groupId = :id")
    fun getGroup(id: Long = DEFAULT_GROUP_ID): SystemTtsGroup?

    @Query("SELECT * FROM sysTts WHERE groupId = :groupId ORDER BY `order` ASC")
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

    @Transaction
    @Query("SELECT * FROM SystemTtsGroup ORDER BY `order` ASC")
    fun getSysTtsWithGroups(): List<GroupWithSystemTts>

    /**
     * 删除组以及TTS
     */
    fun deleteGroupAndTts(group: SystemTtsGroup) {
        deleteTtsByGroup(group.id)
        deleteGroup(group)
    }

    fun insertGroupWithTts(vararg args: GroupWithSystemTts) {
        for (v in args) {
            insertGroup(v.group)
            insertTts(*v.list.toTypedArray())
        }
    }

    /**
     * 按照分组和分组内进行排序获取
     */
    fun getEnabledListForSort(target: Int, isStandbyType: Boolean = false): List<SystemTts> {
        val list = mutableListOf<SystemTts>()
        allGroup.forEach { group ->
            list.addAll(
                getEnabledListByGroupId(
                    group.id,
                    target,
                    isStandbyType
                ).sortedBy { it.order })
        }

        return list
    }

    fun updateAllOrder() {
        getAllGroupWithTts().forEachIndexed { index, groupWithSystemTts ->
            val g = groupWithSystemTts.group
            if (g.order != index) updateGroup(g.copy(order = index))

            groupWithSystemTts.list.sortedBy { it.order }.forEachIndexed { subIndex, systemTts ->
                if (systemTts.order != subIndex) updateTts(systemTts.copy(order = subIndex))
            }
        }
    }
}