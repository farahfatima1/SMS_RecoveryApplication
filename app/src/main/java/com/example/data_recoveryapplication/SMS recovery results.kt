package com.example.data_recoveryapplication

data class SmsRecoveryResult(
    val sender: String,
    val message: String,
    val dateTime: String,
    val type: String,
    val recoveryStatus: String,
    val confidence: String,
    val source: SmsRecoverySource
)