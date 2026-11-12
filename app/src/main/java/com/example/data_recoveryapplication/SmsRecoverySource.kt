package com.example.data_recoveryapplication

enum class SmsRecoverySource {
    LIVE_SMS_PROVIDER,
    BACKUP_DATABASE,
    FORENSIC_DATABASE,
    WAL_FILE,
    UNKNOWN
}