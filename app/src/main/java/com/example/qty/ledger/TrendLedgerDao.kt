package com.example.qty.ledger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendLedgerDao {

    @Query("SELECT * FROM trend_evaluation_ledger ORDER BY timestampMs DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 100): Flow<List<TrendLedgerEntity>>

    @Query("SELECT COUNT(*) FROM trend_evaluation_ledger")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trend_evaluation_ledger")
    suspend fun getCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TrendLedgerEntity): Long

    @Query("DELETE FROM trend_evaluation_ledger")
    suspend fun clearLedger()
}
