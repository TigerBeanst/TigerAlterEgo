package com.jakting.alterego

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
data class ClipboardSend(
    val type: String,
    val code: Int,
    val data: ClipboardData
)

@Keep
data class ClipboardData(
    val time: Long,
    val content: String
)

