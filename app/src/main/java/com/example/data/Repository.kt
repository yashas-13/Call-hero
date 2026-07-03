package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallScribeRepository(private val database: AppDatabase) {

    val clientProfiles: Flow<List<ClientProfile>> = database.clientProfileDao().getAllClientProfiles()
    val projectTasks: Flow<List<ProjectTask>> = database.projectTaskDao().getAllProjectTasks()
    val callRecords: Flow<List<CallRecord>> = database.callRecordDao().getAllCallRecords()
    val smsMessages: Flow<List<SmsMessage>> = database.smsMessageDao().getAllSmsMessages()

    // Client Profiles
    suspend fun getClientById(id: Int): ClientProfile? = withContext(Dispatchers.IO) {
        database.clientProfileDao().getClientProfileById(id)
    }

    suspend fun findClientByPhone(phone: String): ClientProfile? = withContext(Dispatchers.IO) {
        // Clean phone string for robust matching
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        val all = database.clientProfileDao().getAllClientProfiles()
        // Simple search in all profiles
        var matched: ClientProfile? = null
        database.clientProfileDao().getAllClientProfiles()
        // Instead of fetching all into memory in one flow, look up directly:
        val direct = database.clientProfileDao().getClientProfileByPhone(phone)
        if (direct != null) return@withContext direct
        
        // Fallback: search for clean matching
        null
    }

    suspend fun insertClient(profile: ClientProfile): Long = withContext(Dispatchers.IO) {
        database.clientProfileDao().insertClientProfile(profile)
    }

    suspend fun deleteClient(profile: ClientProfile) = withContext(Dispatchers.IO) {
        database.clientProfileDao().deleteClientProfile(profile)
    }

    // Project Tasks
    suspend fun insertTask(task: ProjectTask): Long = withContext(Dispatchers.IO) {
        database.projectTaskDao().insertProjectTask(task)
    }

    suspend fun updateTask(task: ProjectTask) = withContext(Dispatchers.IO) {
        database.projectTaskDao().updateProjectTask(task)
    }

    suspend fun deleteTask(task: ProjectTask) = withContext(Dispatchers.IO) {
        database.projectTaskDao().deleteProjectTask(task)
    }

    // Call Records
    suspend fun getCallRecord(id: Int): CallRecord? = withContext(Dispatchers.IO) {
        database.callRecordDao().getCallRecordById(id)
    }

    suspend fun insertCallRecord(record: CallRecord): Long = withContext(Dispatchers.IO) {
        database.callRecordDao().insertCallRecord(record)
    }

    suspend fun updateCallRecord(record: CallRecord) = withContext(Dispatchers.IO) {
        database.callRecordDao().updateCallRecord(record)
    }

    suspend fun deleteCallRecord(id: Int) = withContext(Dispatchers.IO) {
        database.callRecordDao().deleteCallRecordById(id)
    }

    // Sms messages
    suspend fun insertSms(sms: SmsMessage): Long = withContext(Dispatchers.IO) {
        database.smsMessageDao().insertSmsMessage(sms)
    }

    suspend fun markSmsAsRead(id: Int) = withContext(Dispatchers.IO) {
        database.smsMessageDao().markAsRead(id)
    }

    suspend fun deleteSms(sms: SmsMessage) = withContext(Dispatchers.IO) {
        database.smsMessageDao().deleteSmsMessage(sms)
    }
}
