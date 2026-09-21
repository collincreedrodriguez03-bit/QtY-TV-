package com.example.qty.ledger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrendLedgerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QtyDatabase : RoomDatabase() {

    abstract fun trendLedgerDao(): TrendLedgerDao

    companion object {
        @Volatile
        private var INSTANCE: QtyDatabase? = null

        fun getInstance(context: Context): QtyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QtyDatabase::class.java,
                    "qty_tv_evaluation_ledger.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
