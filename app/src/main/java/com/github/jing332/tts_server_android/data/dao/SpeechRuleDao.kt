package com.github.jing332.tts_server_android.data.dao

import androidx.room.*
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechRuleDao {
    @get:Query("SELECT * FROM speech_rules ORDER BY `order` ASC")
    val all: List<SpeechRule>

    @get:Query("SELECT * FROM speech_rules WHERE isEnabled = '1'")
    val allEnabled: List<SpeechRule>

    @Query("SELECT * FROM speech_rules ORDER BY `order` ASC")
    fun flowAll(): Flow<List<SpeechRule>>

    @get:Query("SELECT count(*) FROM speech_rules")
    val count: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg data: SpeechRule)

    @Delete
    fun delete(vararg data: SpeechRule)

    @Update
    fun update(vararg data: SpeechRule)

    @Query("SELECT * FROM speech_rules WHERE ruleId = :ruleId AND isEnabled = :isEnabled LIMIT 1")
    fun getByRuleId(ruleId: String, isEnabled: Boolean = true): SpeechRule?

//    @Query("SELECT * FROM speech_rules WHERE ruleId = :ruleId")
//    fun getByRuleId(ruleId: String): SpeechRule?

    fun insertOrUpdate(vararg args: SpeechRule) {
        for (v in args) {
            val old = getByRuleId(v.ruleId)
            if (old == null) {
                insert(v)
                continue
            }

            if (v.ruleId == old.ruleId && v.version > old.version)
                update(v.copy(id = old.id))
        }
    }
}