package com.jakting.alterego

data class ClipboardSend(
    val type: String,
    val code: Int,
    val data: ClipboardData
)

data class ClipboardData(
    val time: Long,
    val content: String
)

