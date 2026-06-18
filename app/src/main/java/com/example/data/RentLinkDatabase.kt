package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Property::class,
        Tenant::class,
        Lease::class,
        Transaction::class,
        RentLinkAlert::class,
        MaintenanceRequest::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RentLinkDatabase : RoomDatabase() {

    abstract fun rentLinkDao(): RentLinkDao

    companion object {
        @Volatile
        private var INSTANCE: RentLinkDatabase? = null

        fun getDatabase(context: Context): RentLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentLinkDatabase::class.java,
                    "rentlink_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
