package com.studyflowai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.studyflowai.data.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND isArchived = 0 ORDER BY timestamp ASC")
    fun getActiveMessages(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND isArchived = 1 ORDER BY timestamp DESC")
    fun getArchivedMessages(userId: String): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isArchived = 1 WHERE userId = :userId AND isArchived = 0")
    suspend fun archiveActiveChat(userId: String)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearAllHistory(userId: String)
}
