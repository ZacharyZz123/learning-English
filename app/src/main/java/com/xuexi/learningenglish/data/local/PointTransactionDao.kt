package com.xuexi.learningenglish.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PointTransactionDao {
    @Query("SELECT * FROM point_transaction ORDER BY createdAt DESC, id DESC")
    suspend fun getAll(): List<PointTransactionEntity>

    @Query("SELECT COALESCE(SUM(delta), 0) FROM point_transaction")
    suspend fun getBalance(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: PointTransactionEntity)
}
