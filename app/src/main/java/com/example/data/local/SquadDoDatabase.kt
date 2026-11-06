package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        SquadMemberEntity::class,
        SquadActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SquadDoDatabase : RoomDatabase() {
    abstract fun dao(): SquadDoDao

    companion object {
        @Volatile
        private var INSTANCE: SquadDoDatabase? = null

        fun getDatabase(context: Context): SquadDoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SquadDoDatabase::class.java,
                    "squaddo_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
