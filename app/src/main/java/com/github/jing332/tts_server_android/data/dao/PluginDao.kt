package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import kotlinx.coroutines.flow.Flow

@Dao
interface PluginDao {
    @get:Query("SELECT * FROM plugin")
    val all: List<Plugin>

    @get:Query("SELECT * FROM plugin WHERE isEnabled = '1'")
    val allEnabled: List<Plugin>

    @Query("SELECT * FROM plugin  ")
    fun flowAll(): Flow<List<Plugin>>

    @get:Query("SELECT count(*) FROM plugin")
    val count: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: Plugin)

    @Delete
    fun delete(vararg data: Plugin)

    @Update
    fun update(vararg data: Plugin)

    @Query("SELECT * FROM plugin WHERE pluginId = :pluginId ")
    fun getByPluginId(pluginId: String): Plugin?
}