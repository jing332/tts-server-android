package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.ReplaceRule
import kotlinx.coroutines.flow.Flow
import kotlin.math.min

@Dao
interface ReplaceRuleDao {
    @get:Query("SELECT * FROM ReplaceRule ORDER BY `order` ASC")
    val all: List<ReplaceRule>

    @get:Query("SELECT * FROM ReplaceRule WHERE isEnabled = '1' ORDER BY `order` ASC")
    val allEnabled: List<ReplaceRule>

    @Query("SELECT * FROM ReplaceRule ORDER BY `order` ASC")
    fun flowAll(): Flow<List<ReplaceRule>>

    @get:Query("SELECT count(*) FROM ReplaceRule")
    val count: Int

    @Query("SELECT * FROM ReplaceRule WHERE id = :id")
    fun get(id: Long): ReplaceRule?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: ReplaceRule)

    @Delete
    fun delete(vararg data: ReplaceRule)

    @Update
    fun update(vararg data: ReplaceRule)

    /**
     * 更新数据并刷新排序索引
     */
    fun updateDataAndRefreshOrder(data: ReplaceRule) {
        val list = all.toMutableList()
        list.removeIf { it.id == data.id }
        list.add(min(data.order, list.size), data)
        list.forEachIndexed { index, value -> value.apply { order = index } }

        update(*list.toTypedArray())
    }
}