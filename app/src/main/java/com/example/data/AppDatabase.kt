package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClientProfile::class,
        ProjectTask::class,
        CallRecord::class,
        SmsMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientProfileDao(): ClientProfileDao
    abstract fun projectTaskDao(): ProjectTaskDao
    abstract fun callRecordDao(): CallRecordDao
    abstract fun smsMessageDao(): SmsMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "callscribe_secure_db"
                )
                .fallbackToDestructiveMigration() // safe for local prototype databases
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
