package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientProfileDao {
    @Query("SELECT * FROM client_profiles ORDER BY name ASC")
    fun getAllClientProfiles(): Flow<List<ClientProfile>>

    @Query("SELECT * FROM client_profiles WHERE id = :id LIMIT 1")
    suspend fun getClientProfileById(id: Int): ClientProfile?

    @Query("SELECT * FROM client_profiles WHERE phone = :phone LIMIT 1")
    suspend fun getClientProfileByPhone(phone: String): ClientProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientProfile(profile: ClientProfile): Long

    @Delete
    suspend fun deleteClientProfile(profile: ClientProfile)
}

@Dao
interface ProjectTaskDao {
    @Query("SELECT * FROM project_tasks ORDER BY priority DESC, title ASC")
    fun getAllProjectTasks(): Flow<List<ProjectTask>>

    @Query("SELECT * FROM project_tasks WHERE clientProfileId = :clientId")
    fun getTasksByClient(clientId: Int): Flow<List<ProjectTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTask(task: ProjectTask): Long

    @Update
    suspend fun updateProjectTask(task: ProjectTask)

    @Delete
    suspend fun deleteProjectTask(task: ProjectTask)
}

@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCallRecords(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE id = :id LIMIT 1")
    suspend fun getCallRecordById(id: Int): CallRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(record: CallRecord): Long

    @Update
    suspend fun updateCallRecord(record: CallRecord)

    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteCallRecordById(id: Int)
}

@Dao
interface SmsMessageDao {
    @Query("SELECT * FROM sms_messages ORDER BY urgencyScore DESC, timestamp DESC")
    fun getAllSmsMessages(): Flow<List<SmsMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessage(sms: SmsMessage): Long

    @Query("UPDATE sms_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Delete
    suspend fun deleteSmsMessage(sms: SmsMessage)
}
