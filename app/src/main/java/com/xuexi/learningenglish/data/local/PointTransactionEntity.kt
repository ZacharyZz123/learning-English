package com.xuexi.learningenglish.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_transaction")
data class PointTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val delta: Int,
    val type: String,
    val title: String,
    val word: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PointTransactionType {
    PRACTICE_CORRECT,
    PRACTICE_WRONG,
    WRONG_BOOK_CLEARED,
    REDEEM_CASH,
    REDEEM_CUSTOM
}
