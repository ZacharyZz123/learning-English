package com.xuexi.learningenglish.data.model

data class PointChangeResult(
    val delta: Int,
    val balance: Int,
    val title: String
)

data class RedeemResult(
    val success: Boolean,
    val message: String,
    val balance: Int
)
